package dev.darkknights.dkbot.management.database;

import com.mongodb.client.MongoDatabase;
import dev.darkknights.Logging;
import dev.darkknights.WrongGuildException;
import dev.darkknights.config.Configs;
import dev.darkknights.dkbot.management.APIRequests;
import dev.darkknights.dkbot.management.memberdata.ServerMember;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import org.bson.Document;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;
import java.util.List;

import static dev.darkknights.dkbot.management.database.DataBaseSettings.*;
import static java.lang.Thread.sleep;

public class MemberDataBase {
    private static final Logging log = new Logging();

    private static final MongoDatabase memberDataBase = getMemberDataBase();


    public boolean hasData(String discordID) {
        if (isDbSetup()) {
            for (String name : memberDataBase.listCollectionNames()) {
                if (name.equals(discordID)) return true;
            }
        }
        return false;
    }

    public void saveToDatabase(JSONObject data) throws ParseException {
        if (isDbSetup()) {
            var discordID = data.get("discordID").toString();

            if (!hasData(discordID)) {
                memberDataBase.createCollection(discordID);
                memberDataBase.getCollection(discordID).insertOne(jsonObjToDocument(data));
                log.print("MDB: Saved Data for " + discordID);
            } else if (isNewData(data)) {
                memberDataBase.getCollection(discordID).drop();
                memberDataBase.createCollection(discordID);
                memberDataBase.getCollection(discordID).insertOne(jsonObjToDocument(data));
                log.print("MDB: Updated Data for " + discordID);
            }
        }
    }

    public JSONObject getFromDatabase(String discordID) throws ParseException {
        if (isDbSetup()) {
            if (hasData(discordID)) {
                var dataList = memberDataBase.getCollection(discordID).find();
                for (Document document : dataList) {
                    if (document != null) return documentToJsonObj(document);
                }
            } else {
                log.print("MDB: No Data for " + discordID);
                return null;
            }
        }
        return null;
    }

    public boolean isNewData(JSONObject newData) throws ParseException {
        var discordID = newData.get("discordID").toString();
        if (hasData(discordID)) {
            JSONObject oldData = getFromDatabase(discordID);
            return !oldData.equals(newData);
        }
        return true;
    }

    public ArrayList<JSONObject> getAllData() throws ParseException {
        if (isDbSetup()) {
            var allDataList = new ArrayList<JSONObject>();

            for (String colName : memberDataBase.listCollectionNames()) {
                allDataList.add(getFromDatabase(colName));
            }

            return allDataList;
        }
        return null;
    }

    void updateAllData() {
        if (isDbSetup()) {
            var allDataList = new ArrayList<JSONObject>();
            try {
                allDataList = getAllData();
            } catch (ParseException e) {
                e.printStackTrace();
                log.printErr("MDB: updateAllData()");
            }

            if (allDataList == null) {
                log.printErr("MDB: updateAllData() -> allDataList = null");
            } else {
                ArrayList<JSONObject> finalAllDataList = allDataList;

                Runnable runnable = () -> {
                    while (APIRequests.isMojangAPIuse()) {
                        try {
                            sleep(0);
                        } catch (InterruptedException ignored) {
                        }
                    }

                    APIRequests.setMojangAPIuse(true);
                    try {
                        //1m warten -> rate limit reset
                        sleep(60 * 1000);
                    } catch (InterruptedException ignored) {
                    }

                    int i = 0; // Counter wegen rate limit (200/min)

                    for (JSONObject json : finalAllDataList) {
                        if (i > 20) {
                            try {
                                //1m warten
                                sleep(60 * 1000);
                                i = 0;
                            } catch (InterruptedException ignored) {
                            }
                        }

                        if (json != null) {
                            i++;

                            try {
                                var sm = new ServerMember(json);
                                var flags = sm.getDiscordFlags();

                                var member = Configs.gildenIDs.getDkGuild().getMemberById(sm.getDiscordID());

                                // Rollen feststellen
                                if (member != null) {
                                    var newRoles = detectRoles(member);

                                    if (!newRoles.equals(flags.getRoles())) {
                                        flags.setRoles(newRoles);
                                    }

                                    // Flags setzen
                                    sm.setDiscordFlags(flags);

                                    //Mc Ign updaten
                                    var uuid = sm.getMinecraftUUID();
                                    var ign = new APIRequests().getUsername(uuid);

                                    if (!sm.getMinecraftUsername().equals(ign)) sm.setMinecraftData(ign, uuid);

                                    // In Database speichern
                                    var smJson = sm.toJsonObject();
                                    saveToDatabase(smJson);
                                } else {
                                    // Keine Flags -> delete
                                    if (flags.getRoles().isEmpty() && flags.getSpendenBetrag() == 0) {
                                        memberDataBase.getCollection(sm.getDiscordIDString()).drop();
                                        log.print("member == null for " + sm.getDiscordID() + " - deleted from DB.");
                                    } else {
                                        log.print("member == null for " + sm.getDiscordID());
                                    }
                                }
                            } catch (WrongGuildException e) {
                                e.setLocation("dev.darkknights.dkbot.management.database.MemberDataBase.updateAllData");
                                APIRequests.setMojangAPIuse(false);
                            } catch (ParseException e) {
                                e.printStackTrace();
                                log.printErr("MDB: updateAllData()");
                                APIRequests.setMojangAPIuse(false);
                            }
                        }
                    }

                    APIRequests.setMojangAPIuse(false);
                };

                Thread thread = Thread.ofVirtual().start(runnable);
            }
        }
    }

    public List<String> detectRoles(Member member) {
        var roles = new ArrayList<String>();

        for (Role role : member.getRoles()) {
            var id = role.getId();

            switch (id) {
                case "1067177552487129148" -> roles.add("TeamM");
                case "1067177827700592731" -> roles.add("BotM");
                case "1067177758515531826" -> roles.add("DiscordM");
                case "1066469109501546576" -> roles.add("Admin");
                case "1066465742230405310" -> roles.add("Gründer");
                case "1117435898267697232" -> roles.add("Designer");
                case "1066487351855030363" -> roles.add("Leader");
                case "1066691916994977802" -> roles.add("Mod");
                //case "1088214591659589662" -> roles.add("YT");
                case "1104845108609814559" -> roles.add("noEvent");
            }
        }

        return roles;
    }
}
