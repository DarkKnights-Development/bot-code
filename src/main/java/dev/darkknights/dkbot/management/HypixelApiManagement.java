package dev.darkknights.dkbot.management;

import dev.darkknights.Bots;
import dev.darkknights.Logging;
import dev.darkknights.dkbot.management.database.SbProfileDatabase;
import net.hypixel.api.HypixelAPI;
import net.hypixel.api.apache.ApacheHttpClient;
import net.hypixel.api.exceptions.BadStatusCodeException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.net.MalformedURLException;
import java.time.Instant;
import java.util.*;

public class HypixelApiManagement {
    private static final Logging log = new Logging();

    public static final HypixelAPI API;

    private static HashSet<String> uuidBlockList = new HashSet<>();

    static {
        var key = System.getProperty("apiKey", Bots.getConfig().get("NEWKEY"));
        API = new HypixelAPI(new ApacheHttpClient(UUID.fromString(key)));
    }


    public JSONObject getPlayerData(String uuid) throws ParseException {
        try {
            var raw = API.getPlayerByUuid(uuid).join().getPlayer().getRaw();
            return Bots.gsonToSimpleJson(raw);
        } catch (BadStatusCodeException e) {
            log.printErr("Hypixel API: " + e.getResponseCause());
            new APIRequests().updateApiStatusDisplay(e.getStatusCode());
        }
        return null;
    }

    public String getDiscord(String uuid) throws ParseException {
        //###########################################
        //# player.socialMedia.links.DISCORD
        //###########################################

        var playerData = getPlayerData(uuid);

        var socialMedia = (JSONObject) playerData.get("socialMedia");

        var links = (JSONObject) socialMedia.get("links");

        //###########################################
        //# Discord Tag/Name aus Hypixel Social Media Verknüpfung
        //###########################################
        return links.get("DISCORD").toString();
    }

    public JSONArray getSkyblockProfiles(String uuid, Boolean ignoreDataBase) throws ParseException {
        JSONArray returnedData = null;

        var sdb = new SbProfileDatabase();

        if (!ignoreDataBase && sdb.hasData(uuid) && uuidBlockList.contains(uuid)) {
            returnedData = sdb.getFromDatabase(uuid);
        } else {
            try {
                var raw = API.getSkyBlockProfiles(uuid).join().getProfiles();
                returnedData = Bots.gsonToSimpleJson(raw);
                sdb.saveToDatabase(uuid, returnedData);
                blockUuid(uuid);
            } catch (BadStatusCodeException e) {
                log.printErr("Hypixel API: " + e.getResponseCause());
                new APIRequests().updateApiStatusDisplay(e.getStatusCode());
            }
        }

        return returnedData;
    }

    private void blockUuid(String uuid) {
        uuidBlockList.add(uuid);
        //log.print("Added " + uuid + " to Blocklist.");

        var timer = new Timer();
        var task = new TimerTask() {
            @Override
            public void run() {
                uuidBlockList.remove(uuid);
                //log.print("Removed " + uuid + " from Blocklist.");
            }
        };

        // Nur 1-mal ausführen - nach 1h
        timer.schedule(task, Date.from(Instant.now().plusSeconds(3600)));
    }


    public JSONObject getSelectedProfile(String uuid) throws MalformedURLException, ParseException {
        JSONParser parser = new JSONParser();
        //###########################################
        //# profiles.ID.selected
        //###########################################

        var profiles = getSkyblockProfiles(uuid, false);

        for (Object profile : profiles) {
            var data = (JSONObject) parser.parse(profile.toString());

            if ((Boolean) data.get("selected")) return (JSONObject) profile;
        }

        return null;
    }


    public int getSbLvlExp(String uuid, JSONObject profile) {
        //###########################################
        //# profile.members.UUID.leveling.experience
        //###########################################

        var members = (JSONObject) profile.get("members");

        var player = (JSONObject) members.get(uuid);

        var leveling = (JSONObject) player.get("leveling");

        var exp = (Long) leveling.get("experience");

        return exp.intValue() / 100;
    }

    public int getCataLvl(String UUID, JSONObject profile) {
        //###########################################
        //# profile.members.UUID.dungeons.dungeon_types.catacombs.experience
        //###########################################

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
    }
}
