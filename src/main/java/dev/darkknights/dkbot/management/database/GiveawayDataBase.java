package dev.darkknights.dkbot.management.database;

import com.mongodb.client.MongoDatabase;
import dev.darkknights.Logging;
import org.bson.Document;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;

import static dev.darkknights.dkbot.management.database.DataBaseSettings.*;

public class GiveawayDataBase {
    private static final Logging log = new Logging();

    private static final MongoDatabase giveawayDB = DataBaseSettings.getGiveawayDatabase();


    public boolean hasData(String msgID) {
        if (isDbSetup()) {
            for (String name : giveawayDB.listCollectionNames()) {
                if (name.equals(msgID)) return true;
            }
        }
        return false;
    }

    public void saveToDatabase(JSONObject data) throws ParseException {
        if (isDbSetup()) {
            var msgID = data.get("msgID").toString();

            if (!hasData(msgID)) {
                giveawayDB.createCollection(msgID);
                giveawayDB.getCollection(msgID).insertOne(jsonObjToDocument(data));
                log.print("GDB: Saved Data for " + msgID);
            } else if (isNewData(data)) {
                giveawayDB.getCollection(msgID).drop();
                giveawayDB.createCollection(msgID);
                giveawayDB.getCollection(msgID).insertOne(jsonObjToDocument(data));
                log.print("GDB: Updated Data for " + msgID);
            }
        }
    }

    public JSONObject getFromDatabase(String msgID) throws ParseException {
        if (isDbSetup()) {
            if (hasData(msgID)) {
                var dataList = giveawayDB.getCollection(msgID).find();
                for (Document document : dataList) {
                    if (document != null) return documentToJsonObj(document);
                }
            } else {
                log.print("GDB: No Data for " + msgID);
                return null;
            }
        }
        return null;
    }

    public boolean isNewData(JSONObject newData) throws ParseException {
        var msgID = newData.get("msgID").toString();
        if (hasData(msgID)) {
            JSONObject oldData = getFromDatabase(msgID);
            return !oldData.equals(newData);
        }
        return true;
    }

    public ArrayList<JSONObject> getAllData() throws ParseException {
        if (isDbSetup()) {
            var allDataList = new ArrayList<JSONObject>();

            for (String colName : giveawayDB.listCollectionNames()) {
                allDataList.add(getFromDatabase(colName));
            }

            return allDataList;
        }
        return null;
    }

    public ArrayList<JSONObject> getAllActive() throws ParseException {
        var allData = getAllData();
        if (allData != null) {
            var activeData = new ArrayList<JSONObject>();

            allData.forEach(json -> {
                boolean active = (boolean) json.get("active");

                if (active) activeData.add(json);
            });

            return activeData;
        }
        return null;
    }
}
