package dev.darkknights.dkbot.management.database;

import com.mongodb.client.MongoDatabase;
import dev.darkknights.Logging;
import dev.darkknights.dkbot.management.HypixelApiManagement;
import org.json.simple.JSONArray;
import org.json.simple.parser.ParseException;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

import static dev.darkknights.Bots.formatTime;
import static dev.darkknights.dkbot.management.database.DataBaseSettings.*;

public class SbProfileDatabase {

    private static final MongoDatabase sbProfileDatabase = getSbProfileDatabase();

    private static final Logging log = new Logging();


    public boolean hasData(String UUID) {
        if (isDbSetup()) {
            //print("hasData() " + UUID);

            for (String name : sbProfileDatabase.listCollectionNames()) {
                if (name.equals(UUID)) return true;
            }
        }
        return false;
    }

    public void saveToDatabase(String UUID, JSONArray data) throws ParseException {
        if (isDbSetup()) {
            if (!hasData(UUID)) {
                sbProfileDatabase.createCollection(UUID);
                sbProfileDatabase.getCollection(UUID).insertOne(jsonArrToDocument(data));
                log.print("SBDB: Saved Data for " + UUID);
            } else if (isNewData(UUID, data)) {
                sbProfileDatabase.getCollection(UUID).drop();
                sbProfileDatabase.createCollection(UUID);
                sbProfileDatabase.getCollection(UUID).insertOne(jsonArrToDocument(data));
                log.print("SBDB: Updated Data for " + UUID);
            }
        }
    }

    public JSONArray getFromDatabase(String UUID) throws ParseException {
        if (isDbSetup()) {
            if (hasData(UUID)) {
                var data = sbProfileDatabase.getCollection(UUID).find().first();
                return documentToJsonArr(data);
            } else {
                log.print("SBDB: No Data for " + UUID);
                return null;
            }
        }
        return null;
    }

    public boolean isNewData(String UUID, JSONArray newData) throws ParseException {
        if (hasData(UUID)) {
            var oldData = getFromDatabase(UUID);
            return !oldData.equals(newData);
        }
        return true;
    }


    /*
    @Deprecated
    void updateDatabase() {
        var dataCount = 0;

        var dateTime = ZonedDateTime.now(ZoneId.of("Europe/Berlin"));

        for (String UUID : sbProfileDatabase.listCollectionNames()) {
            var minute = dataCount % 30; // Wert zw. 0 und 29

            var startDate = Date.from(dateTime.withMinute(minute).toInstant().plusSeconds(3600));

            var timer = new Timer();
            var task = new TimerTask() {
                public void run() {
                    print("SBDB: Timer " + UUID);
                    try {
                        HypixelApiManagement.getSkyblockProfiles(UUID, true); //APIRequests.getSbProfiles(UUID, true);
                    } catch (Exception e) {
                        printErr("Timer " + UUID + " | " + e.getMessage());
                    }
                }
            };

            // 1h between calls
            timer.schedule(task, startDate, 3600000);

            print("SBDB: Timer for " + UUID + " | " + formatTime(startDate));

            dataCount++;
        }
    }
    */

    static void updateDataBase() {
        var dateTime = ZonedDateTime.now(ZoneId.of("Europe/Berlin"));
        var startDate = Date.from(dateTime.withMinute(5).toInstant().plusSeconds(3600));

        AtomicInteger counter = new AtomicInteger();
        AtomicInteger error = new AtomicInteger();

        var timer = new Timer();
        var task = new TimerTask() {
            public void run() {
                for (String UUID : sbProfileDatabase.listCollectionNames()) {
                    //print("SBDB: Timer " + UUID);
                    try {
                        new HypixelApiManagement().getSkyblockProfiles(UUID, true); //APIRequests.getSbProfiles(UUID, true);
                        counter.getAndIncrement();
                    } catch (Exception e) {
                        log.printErr("Timer " + UUID + " | " + e.getMessage());
                        error.getAndIncrement();
                    }
                }

                log.print("SBDB: " + counter + " Timer | " + error + " Error | " + (counter.get() + error.get()) + " gesamt");

                counter.set(0);
                error.set(0);

            }
        };

        // 6h between calls
        timer.schedule(task, startDate, 21600000);

        log.print("SBDB: Timer setup done | Start time: " + formatTime(startDate));
    }
}
