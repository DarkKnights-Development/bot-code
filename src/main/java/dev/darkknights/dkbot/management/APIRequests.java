package dev.darkknights.dkbot.management;

import dev.darkknights.Bots;
import dev.darkknights.Logging;
import dev.darkknights.WrongGuildException;
import dev.darkknights.config.Configs;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

import static dev.darkknights.Bots.formatTime;

//###########################################
//# Verify Process
//###########################################
public class APIRequests {
    private static final Logging log = new Logging();

    private static int scheduledApiTestCall = 0;

    private static boolean mojangAPIuse = false;

    public static boolean isMojangAPIuse() {
        return mojangAPIuse;
    }

    public static void setMojangAPIuse(boolean mojangAPIuse) {
        APIRequests.mojangAPIuse = mojangAPIuse;
        log.print("mojangAPIuse : " + mojangAPIuse);
    }

    //###########################################
    //# GET
    //#     https://api.mojang.com/users/profiles/minecraft/<username>?at=<timestamp>
    //#     https://api.mojang.com/user/profile/<UUID>
    //#     https://api.hypixel.net/player?key=KEY&uuid=UUID
    //#     https://api.hypixel.net/skyblock/profiles?key=API_Key&uuid=Player_UUID
    //###########################################

    public JSONArray request(URL requestUrl, boolean hypixelApi) throws IOException, ParseException, WrongGuildException {
        //System.err.println(requestUrl);

        var conn = (HttpURLConnection) requestUrl.openConnection();
        conn.setRequestMethod("GET");
        conn.connect();

        //###########################################
        //# Check if connect is made
        //###########################################
        var responseCode = conn.getResponseCode();

        if (hypixelApi) updateApiStatusDisplay(responseCode);

        if (responseCode == 200) {
            // Erfolgreicher Request
            var informationString = new StringBuilder();
            var scanner = new Scanner(requestUrl.openStream());

            while (scanner.hasNext()) informationString.append(scanner.nextLine());
            //###########################################
            //# Close the scanner
            //###########################################
            scanner.close();

            var parser = new JSONParser();
            var obj = parser.parse(String.valueOf(informationString));
            var array = new JSONArray();
            array.add(obj);

            return array;
        } else {
            log.printErr("HttpResponseCode: " + responseCode);
            return null;
        }
    }

    public static void test() {
        try {
            URL test = URI.create("http://portquiz.net:27017/").toURL();

            var con = (HttpURLConnection) test.openConnection();
            con.setRequestMethod("GET");
            con.connect();

            System.out.println(con.getResponseCode());

            System.out.println(con.getResponseMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public String getUsername(String UUID) {
        try {
            //###########################################
            //# UUID --> MC Name
            //# https://api.mojang.com/user/profile/<UUID>
            //###########################################
            var url = URI.create("https://api.mojang.com/user/profile/" + UUID).toURL();

            var jsonObject = (JSONObject) request(url, false).get(0);

            //###########################################
            //# Name aus API Antwort
            //###########################################
            return jsonObject.get("name").toString();

        } catch (Exception e) {
            //e.printStackTrace();
            log.printErr("getUsername()");
            return "Error Username";
        }
    }

    public String getUUID(String name) {
        try {
            //###########################################
            //# MC Name --> UUID
            //# https://api.mojang.com/users/profiles/minecraft/<username>?at=<timestamp>
            //###########################################
            var url = URI.create("https://api.mojang.com/users/profiles/minecraft/" + name).toURL();

            var jsonObject = (JSONObject) request(url, false).get(0);

            //###########################################
            //# UUID aus API Antwort
            //###########################################
            return jsonObject.get("id").toString();

        } catch (Exception e) {
            //e.printStackTrace();
            log.printErr("getUUID()");
            return "Error UUID";
        }
    }


    public void apiTest() {
        var key = Bots.getConfig().get("NEWKEY");
        var UUID = "a5809aca2c74412c9a1b14c42fd19371";

        try {
            var url = URI.create("https://api.hypixel.net/skyblock/profiles?key=" + key + "&uuid=" + UUID).toURL();

            request(url, true);
            log.print("apiTest()");

        } catch (IOException | ParseException | WrongGuildException e) {
            e.printStackTrace();
            log.printErr("apiTest()");
        }
    }


    public void updateApiStatusDisplay(int responseCode) {
        try {
            var botCmds = Configs.gildenIDs.getDkGuild().getTextChannelById(Configs.channelIDs.botCmdsID).getManager();

            switch (responseCode) {
                case 200 -> {
                    botCmds.setTopic(
                            ":robot: Befehle: </rank:971443830870126634> | </top:971443830870126636> | </help:1113182819695476778> :robot:" +
                                    "\n:white_check_mark: Hypixel API erreichbar :white_check_mark:"
                    ).queue();
                }
                case 429 -> {
                    botCmds.setTopic(
                            ":robot: Befehle: </rank:971443830870126634> | </top:971443830870126636> | </help:1113182819695476778> :robot:" +
                                    "\n:question: Hypixel API teilweise nicht erreichbar :question:"
                    ).queue();
                }
                case 502 -> {
                    botCmds.setTopic(
                            ":robot: Befehle: </rank:971443830870126634> | </top:971443830870126636> | </help:1113182819695476778> :robot:" +
                                    "\n:exclamation: Hypixel API nicht erreichbar :exclamation:"
                    ).queue();
                }
                case 503 -> {
                    botCmds.setTopic(
                            ":robot: Befehle: </rank:971443830870126634> | </top:971443830870126636> | </help:1113182819695476778> :robot:" +
                                    "\n:exclamation: Hypixel API disabled :exclamation:"
                    ).queue();
                }
            }
        } catch (WrongGuildException ignored) {
        }
    }


    public static void scheduledApiTest() {
        if (scheduledApiTestCall == 0) {
            var dateTime = ZonedDateTime.now(ZoneId.of("Europe/Berlin"));

            var hour = dateTime.getHour() + 1;
            if (hour == 24) hour = 0;
            var startDate = Date.from(dateTime.withHour(hour).withMinute(0).toInstant());

            var timer = new Timer();
            var task = new TimerTask() {
                public void run() {
                    new APIRequests().apiTest();
                }
            };

            // 30min between calls
            timer.schedule(task, startDate, 1800000);

            log.print("scheduledApiTest(), Timer starts at: " + formatTime(startDate));

            scheduledApiTestCall = 1;
        }
    }

    /*
        @Deprecated
        public static String getDC(String ID) {
            //###########################################
            //# Hypixel API Key from .env File
            //###########################################
            var key = Bots.getConfig().get("KEY");

            try {
                //###########################################
                //# UUID --> Dc Name
                //# https://api.hypixel.net/player?key=KEY&uuid=UUID
                //###########################################
                var url = URI.create("https://api.hypixel.net/player?key=" + key + "&uuid=" + ID).toURL();


                //###########################################
                //# player.socialMedia.links.DISCORD
                //###########################################

                var jsonObject = (JSONObject) request(url).get(0);

                var jsonObjectP = (JSONObject) jsonObject.get("player");

                var jsonObjectSM = (JSONObject) jsonObjectP.get("socialMedia");

                var jsonObjectL = (JSONObject) jsonObjectSM.get("links");

                //###########################################
                //# Discord Tag/Name aus Hypixel Social Media Verknüpfung
                //###########################################
                return jsonObjectL.get("DISCORD").toString();

            } catch (Exception e) {
                e.printStackTrace();
                printErr("getDc()");
                return "Error getDc()";
            }
        }
        @Deprecated
        public static JSONArray getSbProfiles(String UUID, Boolean ignoreData) {
            try {
                JSONArray returnedData;

                // check if Data in Database
                if (!ignoreData && hasData(UUID)) returnedData = getFromDatabase(UUID);
                else {
                    //###########################################
                    //# Hypixel API Key from .env File
                    //###########################################
                    var key = Bots.getConfig().get("KEY");

                    //###########################################
                    //# Skyblock Profiles Data
                    //# https://api.hypixel.net/skyblock/profiles?key=API_Key&uuid=Player_UUID
                    //###########################################
                    var url = URI.create("https://api.hypixel.net/skyblock/profiles?key=" + key + "&uuid=" + UUID).toURL();

                    var request = request(url);

                    returnedData = (JSONArray) request.get(0);

                    saveToDatabase(UUID, returnedData);
                }

                return returnedData;

            } catch (Exception e) {
                e.printStackTrace();
                printErr("SbProfileData");
                return null;
            }
        }
        @Deprecated
        private static JSONObject getSelectedProfile(String UUID) {
            try {

                var parser = new JSONParser();
                //###########################################
                //# profiles.ID.selected
                //###########################################

                var profiles = getSbProfiles(UUID, false);

                for (Object profile : profiles) {
                    var data = (JSONObject) parser.parse(profile.toString());

                    if ((Boolean) data.get("selected")) return (JSONObject) profile;
                }
            } catch (Exception e) {
                e.printStackTrace();
                printErr("getSelectedProfile");
                return null;
            }
            return null;
        }
        @Deprecated
        public static int getSbLvl(String UUID) {
            try {
                //###########################################
                //# profile.members.UUID.leveling.experience
                //###########################################

                var profile = getSelectedProfile(UUID);

                var members = (JSONObject) profile.get("members");

                var player = (JSONObject) members.get(UUID);

                var leveling = (JSONObject) player.get("leveling");

                var exp = (Long) leveling.get("experience");

                return exp.intValue() / 100;
            } catch (Exception e) {
                e.printStackTrace();
                printErr("SbLvl");
                return -1;
            }
        }
        @Deprecated
        public static int getCataLvl(String UUID) {
            try {
                //###########################################
                //# profile.members.UUID.dungeons.dungeon_types.catacombs.experience
                //###########################################

                var profile = getSelectedProfile(UUID);

                var members = (JSONObject) profile.get("members");

                var player = (JSONObject) members.get(UUID);

                var dungeons = (JSONObject) player.get("dungeons");

                var dungeon_types = (JSONObject) dungeons.get("dungeon_types");

                var catacombs = (JSONObject) dungeon_types.get("catacombs");

                var exp = (Double) catacombs.get("experience");

                var lvl = -1;

                if (exp < 4385) lvl = 0;                // Cata unter 10
                else if (exp < 135640) lvl = 1;         // Cata unter 20
                else if (exp < 3084640) lvl = 2;        // Cata unter 30
                else if (exp < 51559640) lvl = 3;       // Cata unter 40
                else if (exp < 569809640) lvl = 4;      // Cate unter 50
                else if (exp >= 569809640) lvl = 5;     // Cata über 50+

                return lvl;
            } catch (Exception e) {
                e.printStackTrace();
                printErr("CataLvl");
                return -1;
            }
        }
    */
}
