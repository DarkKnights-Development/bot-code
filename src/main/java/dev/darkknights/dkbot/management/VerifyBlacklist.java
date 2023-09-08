package dev.darkknights.dkbot.management;

import dev.darkknights.Logging;
import dev.darkknights.config.Configs;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.utils.FileUpload;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

import static dev.darkknights.Bots.checkStellen;
import static java.lang.Thread.sleep;
import static java.nio.file.Files.readAllLines;

public class VerifyBlacklist {
    private static final Logging log = new Logging();

    private static final Path blPath = Configs.filePaths.blacklist;

    public static ArrayList<String> getBlacklist() throws IOException {
        return (ArrayList<String>) readAllLines(blPath); //Liste aus File
    }


    public void cmdBlList(SlashCommandInteractionEvent event) throws IOException {
        ArrayList<String> uuidBlacklist;
        try {
            uuidBlacklist = getBlacklist();
        } catch (IOException e) {
            event.getHook().sendMessage("Error beim Lesen der Blacklist.").queue();
            return;
        }

        var msg = event.getHook().sendMessage("UUIDs zu Usernames konvertieren... \nDieser Prozess kann mehrere Minuten dauern.").complete();

        // Blacklist mit UUID | Username
        var blacklist = new HashMap<String, String>();

        var dateTime = ZonedDateTime.now(ZoneId.of("Europe/Berlin"));
        var day = checkStellen(dateTime.getDayOfMonth());
        var month = checkStellen(dateTime.getMonthValue());
        var year = checkStellen(dateTime.getYear());
        var time = year + "." + month + "." + day + "-";

        Path blFile = Path.of("blacklist",time + "blacklist.txt");

        if (Files.exists(blFile)) {
            msg.editMessage("Done...").queue();
            msg.editMessageAttachments(FileUpload.fromData(blFile)).queue();
        } else {
            Files.createFile(blFile);

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

                for (String uuid : uuidBlacklist) {
                    if (i > 20) {
                        try {
                            event.getHook().setEphemeral(true).sendMessage("...").queue();
                            sleep(60 * 1000);
                            i = 0;
                        } catch (InterruptedException ignored) {
                        }
                    }
                    var name = new APIRequests().getUsername(uuid);
                    i++;
                    if (!name.equals("Error Username")) blacklist.put(uuid, name);
                }

                try {
                    // blacklist Map -> File
                    for (String uuid : blacklist.keySet()) {
                        Files.writeString(blFile, "\nUUID: " + uuid + " - Username: " + blacklist.get(uuid), StandardOpenOption.APPEND);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    msg.editMessage("Es gab einen Fehler beim Ausführen des Befehls.").queue();
                    APIRequests.setMojangAPIuse(false);
                    return;
                }


                msg.editMessage("Done...").queue();
                msg.editMessageAttachments(FileUpload.fromData(blFile)).queue();

                APIRequests.setMojangAPIuse(false);
            };

            Thread thread = Thread.ofVirtual().start(runnable);
        }
    }


    public void cmdAddToBlacklist(SlashCommandInteractionEvent event) throws IOException {
        var ign = event.getOption("ign").getAsString();

        log.logCmd("blacklist", "add", event.getUser().getName(), Map.of("ign", ign));

        String UUID = new APIRequests().getUUID(ign);
        if (UUID.equals("Error UUID")) {
            log.printErr(ign + " existiert nicht.");
            event.getHook().setEphemeral(false).sendMessage("Der Account `" + ign + "` wurde umbenannt oder existiert nicht.").queue();
            return;
        }

        var blacklist = getBlacklist();

        if (!blacklist.contains(UUID)) {
            blacklist.add(UUID);         //Ign hinzufügen
            Files.write(blPath, blacklist, Charset.defaultCharset()); //File schreiben
            event.getHook().sendMessage("Der Account `" + ign + "` wurde gesperrt.").queue();
        } else event.getHook().sendMessage("Der Account `" + ign + "` ist bereits gesperrt.").queue();
    }

    public void cmdRemoveFromBlacklist(SlashCommandInteractionEvent event) throws IOException {
        var ign = event.getOption("ign").getAsString();
        var UUID = new APIRequests().getUUID(ign);

        log.logCmd("blacklist", "remove", event.getUser().getName(), Map.of("ign", ign));

        var blacklist = getBlacklist();

        if (blacklist.remove(UUID)) {
            Files.write(blPath, blacklist, Charset.defaultCharset()); //File schreiben
            event.getHook().sendMessage("Der Account `" + ign + "` wurde entsperrt.").queue();
        } else event.getHook().sendMessage("Der Account `" + ign + "` ist nicht auf der Blacklist.").queue();
    }

}
