package dev.darkknights.dkbot.management.database;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoException;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import dev.darkknights.Bots;
import dev.darkknights.Logging;
import dev.darkknights.dkbot.systems.GiveawaySystem;
import org.bson.Document;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class DataBaseSettings {
    private static final Logging log = new Logging();

    private static boolean dbSetup = false;

    private static final String URL = Bots.getConfig().get("DBURL");
    private static MongoDatabase sbProfileDatabase; // ID: UUID
    private static MongoDatabase memberDataBase;    // ID: discordID
    private static MongoDatabase giveawayDatabase;  // ID: msgID

    public static void setupDataBase() {
        if (!dbSetup) {
            log.print("setupDataBase()");

            var settings = MongoClientSettings.builder()
                    .applyConnectionString(new ConnectionString(URL))
                    .applyToClusterSettings(builder1 -> builder1.serverSelectionTimeout(120, TimeUnit.SECONDS))
                    .build();

            var client = MongoClients.create(settings);

            try {
                memberDataBase = client.getDatabase("MemberData");
                sbProfileDatabase = client.getDatabase("SbProfileData");
                giveawayDatabase = client.getDatabase("GiveawayData");

                memberDataBase.runCommand(new Document("ping", 1));
                sbProfileDatabase.runCommand(new Document("ping", 1));
                giveawayDatabase.runCommand(new Document("ping", 1));
            } catch (MongoException e) {
                e.printStackTrace();
                return;
            }
            dbSetup = true;

            log.print("GiveawayData");
            new GiveawaySystem().timerForActiveGiveaways();

            log.print("SbProfileData");
            SbProfileDatabase.updateDataBase();

            log.print("MemberData");
            new MemberDataBase().updateAllData();
        }
    }

    static MongoDatabase getSbProfileDatabase() {
        return sbProfileDatabase;
    }

    static MongoDatabase getMemberDataBase() {
        return memberDataBase;
    }

    static MongoDatabase getGiveawayDatabase() {
        return giveawayDatabase;
    }

    static boolean isDbSetup() {
        return dbSetup;
    }


    public void saveToDatabase(JSONObject data, MongoDatabase db, String collectionId) throws ParseException {
        if (dbSetup) {
            var id = data.get(collectionId).toString();
            var dbString = db.getName();

            if (!hasData(db, id)) {
                db.createCollection(id);
                db.getCollection(id).insertOne(jsonObjToDocument(data));
                log.print(dbString + ": Saved Data for " + id);
            } else if (isNewData(data, db, collectionId)) {
                db.getCollection(id).drop();
                db.createCollection(id);
                db.getCollection(id).insertOne(jsonObjToDocument(data));
                log.print(dbString + ": Updated Data for " + id);
            }
        }
    }

    public JSONObject getFromDatabase(MongoDatabase db, String collectionId) throws ParseException {
        if (dbSetup) {
            var dbString = db.getName();
            if (hasData(db, collectionId)) {
                var dataList = db.getCollection(collectionId).find();
                for (Document document : dataList) {
                    if (document != null) return documentToJsonObj(document);
                }
            } else {
                log.print(dbString + ": No Data for " + collectionId);
                return null;
            }
        }
        return null;
    }

    public boolean hasData(MongoDatabase db, String collectionId) {
        if (dbSetup) {
            for (String name : db.listCollectionNames()) {
                if (name.equals(collectionId)) return true;
            }
        }
        return false;
    }

    public boolean isNewData(JSONObject newData, MongoDatabase db, String collectionId) throws ParseException {
        var id = newData.get(collectionId).toString();
        if (hasData(db, id)) {
            JSONObject oldData = getFromDatabase(db, id);
            return !oldData.equals(newData);
        }
        return true;
    }

    public ArrayList<JSONObject> getAllData(MongoDatabase db) throws ParseException {
        if (dbSetup) {
            var allDataList = new ArrayList<JSONObject>();

            for (String colName : db.listCollectionNames()) {
                allDataList.add(getFromDatabase(db, colName));
            }

            return allDataList;
        }
        return null;
    }


    public static Document jsonArrToDocument(JSONArray jsonArray) {
        var res = "{\"profiles\":" + jsonArray.toJSONString() + "}";
        return Document.parse(res);
    }

    public static JSONArray documentToJsonArr(Document document) throws ParseException {
        var parser = new JSONParser();
        var jsonObject = (JSONObject) parser.parse(document.toJson());
        var arr = (JSONArray) jsonObject.get("profiles");
        return arr;
    }

    public static Document jsonObjToDocument(JSONObject jsonObject) {
        return Document.parse(jsonObject.toJSONString());
    }

    public static JSONObject documentToJsonObj(Document document) throws ParseException {
        return (JSONObject) new JSONParser().parse(document.toJson());
    }

}
