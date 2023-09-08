package dev.darkknights;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;

import static dev.darkknights.Bots.checkStellen;

public class Logging {

    private static Path logFilePath;
    private static boolean isSetup = false;

    /**
     * normal print
     * @param value msg
     */
    synchronized public void print(String value) {
        if (isSetup) {
            try {
                String printValue = "\n" + timeString() + value;

                Files.writeString(logFilePath, printValue, StandardOpenOption.APPEND);
            } catch (IOException e) {
                System.err.println("IOException | " + value + Bots.getTime());
            }
        }
        else System.out.println(value);
    }

    /**
     * "ERROR | " + value
     * @param value error msg
     */
    public void printErr(String value) {
        print("ERROR | " + value);
    }

    /**
     * "/" + cmd + " " + subCmd + ": user=" + userName + "," + Map
     * @param cmd used cmd
     * @param subCmd used sub cmd
     * @param userName cmd user name
     * @param args Map.of cmd arguments
     */
    public void logCmd(String cmd, String subCmd, String userName, Map<String, String> args) {
        var sb = new StringBuilder();
        for (Map.Entry<String, String> entry : args.entrySet()) {
            sb.append(entry.getKey()).append("=").append(entry.getValue()).append(",");
        }
        sb.deleteCharAt(sb.length() - 1);

        print("/" + cmd + " " + subCmd + ": user=" + userName + "," + sb);
    }

    /**
     * "/" + cmd + " " + ": user=" + userName + "," + Map
     * @param cmd used cmd
     * @param userName cmd user name
     * @param args Map.of cmd arguments
     */
    public void logCmd(String cmd, String userName, Map<String, String> args) {
        var sb = new StringBuilder();
        for (Map.Entry<String, String> entry : args.entrySet()) {
            sb.append(entry.getKey()).append("=").append(entry.getValue()).append(",");
        }
        sb.deleteCharAt(sb.length() - 1);

        print("/" + cmd + " " + ": user=" + userName + "," + sb);
    }

    public static void logSetup() {
        var dateTime = ZonedDateTime.now(ZoneId.of("Europe/Berlin"));

        var day = checkStellen(dateTime.getDayOfMonth());
        var month = checkStellen(dateTime.getMonthValue());
        var year = checkStellen(dateTime.getYear());

        var time = "[" + year + "." + month + "." + day + "] ";
        logFilePath = Paths.get("logs",  time + "log.txt");

        if (Files.exists(logFilePath)) {
            isSetup = true;
        } else {
            try {
                Files.createFile(logFilePath);
                isSetup = true;
            } catch (IOException e) {
                System.err.println("Error while creating logfile" + Bots.getTime());
                isSetup = false;
            }
        }

        delLog14dAgo();
    }

    private static void delLog14dAgo() {
        final Logging log = new Logging();

        var dateTime = ZonedDateTime.now(ZoneId.of("Europe/Berlin")).minusDays(14);

        var day = checkStellen(dateTime.getDayOfMonth());
        var month = checkStellen(dateTime.getMonthValue());
        var year = checkStellen(dateTime.getYear());

        var time = "[" + year + "." + month + "." + day + "] ";
        try {
            if (Files.deleteIfExists(Paths.get("logs", time + "log.txt")))
                log.print("Deleted File " + time + "log.txt");
        } catch (IOException e) {
            log.printErr("delLog14dAgo()");
        }
    }

    private String timeString() {
        var dateTime = ZonedDateTime.now(ZoneId.of("Europe/Berlin"));

        var min = dateTime.getMinute();
        var hour = dateTime.getHour();
        var day = dateTime.getDayOfMonth();
        var month = dateTime.getMonthValue();
        var year = dateTime.getYear();

        return "[" + checkStellen(day) + "." + checkStellen(month) + "." + year + " | " + checkStellen(hour) + ":" + checkStellen(min) + "] ";
    }

    @Deprecated
    synchronized public void printOld(String value) {
        if (isSetup) {
            try {
                var sb = new StringBuilder();
                for (String line : Files.readAllLines(logFilePath)) {
                    sb.append(line).append("\n");
                }
                sb.append(timeString()).append(value);

                Files.writeString(logFilePath, sb);
            } catch (IOException e) {
                System.err.println("IOException | " + value + Bots.getTime());
            }
        }
        else System.out.println(value);
    }
}
