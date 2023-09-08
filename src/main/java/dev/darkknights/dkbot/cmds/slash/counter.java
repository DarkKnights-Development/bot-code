package dev.darkknights.dkbot.cmds.slash;

import dev.darkknights.Logging;
import dev.darkknights.config.Configs;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.awt.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class counter {
    private static final Logging log = new Logging();

    final private Path path = Configs.filePaths.counter;

    /**
     * Map < String jsonKey, String msgText >
     */
    private final Map<String, String> idNameMap = Map.ofEntries(
            Map.entry("xD", "`XD`"),
            Map.entry("._.", "`._.`"),
            Map.entry(".-.", "`.-.`"),
            Map.entry("amyPopcorn", "<:amylee3Popcorn:1074312205752872991>"),
            Map.entry("amyPieks", "<:amylee3Pieks:1083064423410843699>"),
            Map.entry("sitting_dog", "<:sitting_dog:1071543706680819813>"),
            Map.entry("kekw", "<:kekw:1068624193064812654>"),
            Map.entry("looking", "<:FeelsLookingMan:1068622760596750467>"),
            Map.entry("this", "<:this:1068627533144273016>"),
            Map.entry("gg", "`gg`"),
            Map.entry("gz", "`gz`"),
            Map.entry("^^", "`^^`")
    );


    private JSONObject getCounterJson() throws IOException, ParseException {
        var counterList = Files.readAllLines(path);

        var counterString = new StringBuilder();
        for (String s : counterList) {
            counterString.append(s);
        }

        var parser = new JSONParser();

        return (JSONObject) parser.parse(counterString.toString());
    }

    public void counterCmd(SlashCommandInteractionEvent event) {
        try {
            var eb = new EmbedBuilder();
            eb.setFooter(Configs.devs.eposs.cmdDevText() + " | Weitere Counter Vorschläge an Eposs per DM", Configs.devs.eposs.icon())
                    .setColor(Color.YELLOW)
                    .setTitle("Counter Liste")
                    .setDescription(sort().toString());

            event.getHook().sendMessageEmbeds(eb.build()).queue();
        } catch (IOException | ParseException e) {
            e.printStackTrace();
            log.printErr("newCounterCmd()");
            event.getHook().sendMessage("Fehler beim Lesen der Counter.").queue();
        }
    }

    public void countingMsgReceived(MessageReceivedEvent event) {
        var msg = event.getMessage().getContentRaw().toLowerCase();

        var flexing = event.getGuild().getTextChannelById(Configs.channelIDs.flexingID);

        if (msg.contains(":kekw")) {
            var countLong = updateCounterFile("kekw");
            if (countLong % 100 == 0) {
                flexing.sendMessage(event.getMember().getAsMention() + " hat einen neuen " + idNameMap.get("kekw") + " Counter Milestone erreicht: **" + countLong + "**!").queue();
            }
        }

        if (msg.contains("._.")) {
            var countLong = updateCounterFile("._.");
            if (countLong % 100 == 0) {
                flexing.sendMessage(event.getMember().getAsMention() + " hat einen neuen " + idNameMap.get("._.") + " Counter Milestone erreicht: **" + countLong + "**!").queue();
            }
        }

        if (msg.contains(":sitting_dog")) {
            var countLong = updateCounterFile("sitting_dog");
            if (countLong % 100 == 0) {
                flexing.sendMessage(event.getMember().getAsMention() + " hat einen neuen " + idNameMap.get("sitting_dog") + " Counter Milestone erreicht: **" + countLong + "**!").queue();
            }
        }

        if (msg.contains(":amylee3popcorn")) {
            var countLong = updateCounterFile("amyPopcorn");
            if (countLong % 100 == 0) {
                flexing.sendMessage(event.getMember().getAsMention() + " hat einen neuen " + idNameMap.get("amyPopcorn") + " Counter Milestone erreicht: **" + countLong + "**!").queue();
            }
        }

        if (msg.contains(":amylee3pieks")) {
            var countLong = updateCounterFile("amyPieks");
            if (countLong % 100 == 0) {
                flexing.sendMessage(event.getMember().getAsMention() + " hat einen neuen " + idNameMap.get("amyPieks") + " Counter Milestone erreicht: **" + countLong + "**!").queue();
            }
        }

        if (msg.contains("xd")) {
            var countLong = updateCounterFile("xD");
            if (countLong % 100 == 0) {
                flexing.sendMessage(event.getMember().getAsMention() + " hat einen neuen " + idNameMap.get("xD") + " Counter Milestone erreicht: **" + countLong + "**!").queue();
            }
        }

        if (msg.contains(":feelslookingman")) {
            var countLong = updateCounterFile("looking");
            if (countLong % 100 == 0) {
                flexing.sendMessage(event.getMember().getAsMention() + " hat einen neuen " + idNameMap.get("looking") + " Counter Milestone erreicht: **" + countLong + "**!").queue();
            }
        }

        if (msg.contains(".-.")) {
            var countLong = updateCounterFile(".-.");
            if (countLong % 100 == 0) {
                flexing.sendMessage(event.getMember().getAsMention() + " hat einen neuen " + idNameMap.get(".-.") + " Counter Milestone erreicht: **" + countLong + "**!").queue();
            }
        }

        if (msg.contains(":this")) {
            var countLong = updateCounterFile("this");
            if (countLong % 100 == 0) {
                flexing.sendMessage(event.getMember().getAsMention() + " hat einen neuen " + idNameMap.get("this") + " Counter Milestone erreicht: **" + countLong + "**!").queue();
            }
        }

        if (msg.contains("gz")) {
            var countLong = updateCounterFile("gz");
            if (countLong % 100 == 0) {
                flexing.sendMessage(event.getMember().getAsMention() + " hat einen neuen " + idNameMap.get("gz") + " Counter Milestone erreicht: **" + countLong + "**!").queue();
            }
        }
        if (msg.equals("gg") || msg.contains(" gg")) {
            var countLong = updateCounterFile("gg");
            if (countLong % 100 == 0) {
                flexing.sendMessage(event.getMember().getAsMention() + " hat einen neuen " + idNameMap.get("gg") + " Counter Milestone erreicht: **" + countLong + "**!").queue();
            }
        }
        if (msg.contains("^^")) {
            var countLong = updateCounterFile("^^");
            if (countLong % 100 == 0) {
                flexing.sendMessage(event.getMember().getAsMention() + " hat einen neuen " + idNameMap.get("^^") + " Counter Milestone erreicht: **" + countLong + "**!").queue();
            }
        }
    }

    private long updateCounterFile(String key) {
        try {
            var counterJson = getCounterJson();

            var count = (long) counterJson.get(key);
            count++;

            counterJson.replace(key, count);

            Files.writeString(path, counterJson.toJSONString());

            return count;
        } catch (FileNotFoundException e) {
            log.printErr("Counter File");
        } catch (IOException | ParseException e) {
            log.printErr("Counter IO/Pars");
        }
        return -1;
    }

    private StringBuilder sort() throws IOException, ParseException {
        var counterJson = getCounterJson();

        var keys = idNameMap.keySet().stream().toList();
        Map<String, Long> countWerteMap = new HashMap<>();

        for (String key : keys) {
            countWerteMap.put(key, (Long) counterJson.get(key));
        }

        var counterList = new ArrayList<>(countWerteMap.values());
        counterList.sort(null); // niedrig -> hoch

        var sorted = new StringBuilder();

        /*
            countWerteMap → enthält alle Werte aus counter File
            counterList → Werte aus File sortiert von niedrig nach hoch
            String sorted → Description des Embeds im /counter Cmd

            if (Wert A isMax in counterList und sorted enthält nicht den msgText zu Wert A)
                → Embed Text zu sorted hinzufügen
                → letzten / höchsten Wert aus counterList entfernen (entspricht Wert A)

            → solange wdh bis counterList leer ist
         */
        while (counterList.size() > 0) {

            // xD
            if (isMax(countWerteMap.get("xD"), counterList) && !sorted.toString().contains(idNameMap.get("xD"))) {
                sorted.append(idNameMap.get("xD")).append(" Counter: ").append(countWerteMap.get("xD")).append("\n");
                counterList.remove(counterList.size() - 1);
            }

            // kekw
            else if (isMax(countWerteMap.get("kekw"), counterList) && !sorted.toString().contains(idNameMap.get("kekw"))) {
                sorted.append(idNameMap.get("kekw")).append(" Counter: ").append(countWerteMap.get("kekw")).append("\n");
                counterList.remove(counterList.size() - 1);
            }

            // ._.
            else if (isMax(countWerteMap.get("._."), counterList) && !sorted.toString().contains(idNameMap.get("._."))) {
                sorted.append(idNameMap.get("._.")).append(" Counter: ").append(countWerteMap.get("._.")).append("\n");
                counterList.remove(counterList.size() - 1);
            }

            // sitting_dog
            else if (isMax(countWerteMap.get("sitting_dog"), counterList) && !sorted.toString().contains(idNameMap.get("sitting_dog"))) {
                sorted.append(idNameMap.get("sitting_dog")).append(" Counter: ").append(countWerteMap.get("sitting_dog")).append("\n");
                counterList.remove(counterList.size() - 1);
            }

            // amyPopcorn
            else if (isMax(countWerteMap.get("amyPopcorn"), counterList) && !sorted.toString().contains(idNameMap.get("amyPopcorn"))) {
                sorted.append(idNameMap.get("amyPopcorn")).append(" Counter: ").append(countWerteMap.get("amyPopcorn")).append("\n");
                counterList.remove(counterList.size() - 1);
            }

            // amyPieks
            else if (isMax(countWerteMap.get("amyPieks"), counterList) && !sorted.toString().contains(idNameMap.get("amyPieks"))) {
                sorted.append(idNameMap.get("amyPieks")).append(" Counter: ").append(countWerteMap.get("amyPieks")).append("\n");
                counterList.remove(counterList.size() - 1);
            }

            // looking
            else if (isMax(countWerteMap.get("looking"), counterList) && !sorted.toString().contains(idNameMap.get("looking"))) {
                sorted.append(idNameMap.get("looking")).append(" Counter: ").append(countWerteMap.get("looking")).append("\n");
                counterList.remove(counterList.size() - 1);
            }

            // .-.
            else if (isMax(countWerteMap.get(".-."), counterList) && !sorted.toString().contains(idNameMap.get(".-."))) {
                sorted.append(idNameMap.get(".-.")).append(" Counter: ").append(countWerteMap.get(".-.")).append("\n");
                counterList.remove(counterList.size() - 1);
            }

            // this
            else if (isMax(countWerteMap.get("this"), counterList) && !sorted.toString().contains(idNameMap.get("this"))) {
                sorted.append(idNameMap.get("this")).append(" Counter: ").append(countWerteMap.get("this")).append("\n");
                counterList.remove(counterList.size() - 1);
            }

            // gg
            else if (isMax(countWerteMap.get("gg"), counterList) && !sorted.toString().contains(idNameMap.get("gg"))) {
                sorted.append(idNameMap.get("gg")).append(" Counter: ").append(countWerteMap.get("gg")).append("\n");
                counterList.remove(counterList.size() - 1);
            }

            // gz
            else if (isMax(countWerteMap.get("gz"), counterList) && !sorted.toString().contains(idNameMap.get("gz"))) {
                sorted.append(idNameMap.get("gz")).append(" Counter: ").append(countWerteMap.get("gz")).append("\n");
                counterList.remove(counterList.size() - 1);
            }

            // ^^
            else if (isMax(countWerteMap.get("^^"), counterList) && !sorted.toString().contains(idNameMap.get("^^"))) {
                sorted.append(idNameMap.get("^^")).append(" Counter: ").append(countWerteMap.get("^^")).append("\n");
                counterList.remove(counterList.size() - 1);
            }
        }

        return sorted;
    }

    private boolean isMax(long value, ArrayList<Long> counterList) {
        for (Long c : counterList) {
            if (c > value) return false;
        }
        return true;
    }

    /**
     * Use counter.newCounterCmd(SlashCommandInteractionEvent event) instead
     */
    @Deprecated
    public void counterCmdOld(SlashCommandInteractionEvent event) {
        List<String> counter = null; //Counter aus File
        try (Stream<String> fileStream = Files.lines(Path.of("counter.txt"))) {
            counter = fileStream.toList();
        } catch (IOException e) {
            e.printStackTrace();
            event.getHook().sendMessage("Fehler beim Lesen der Counter.").queue();
        }
        var kekwCount = counter.get(0);
        var wCount = counter.get(1);
        var dogCount = counter.get(2);
        var popCornCount = counter.get(3);
        var pieksCount = counter.get(4);
        var xdCount = counter.get(5);
        var lookingCount = counter.get(6);
        var mCount = counter.get(7);
        var thisCount = counter.get(8);

        var eb = new EmbedBuilder()
                .setFooter(Configs.devs.eposs.cmdDevText() + " | Weitere Counter Vorschläge an Eposs per DM", Configs.devs.eposs.icon())
                .setColor(Color.YELLOW)
                .setTitle("Counter Liste")
                .setDescription(
                        "<:kekw:1068624193064812654> Counter: " + kekwCount +
                        "\n`._.` Counter: " + wCount +
                        "\n<:sitting_dog:1071543706680819813> Counter: " + dogCount +
                        "\n<:amylee3Popcorn:1074312205752872991> Counter: " + popCornCount +
                        "\n<:amylee3Pieks:1083064423410843699> Counter: " + pieksCount +
                        "\n`XD` Counter: " + xdCount +
                        "\n<:FeelsLookingMan:1068622760596750467> Counter: " + lookingCount +
                        "\n`.-.` Counter: " + mCount +
                        "\n<:this:1068627533144273016> Counter: " + thisCount
        );

        event.getHook().sendMessageEmbeds(eb.build()).queue();
    }

    /**
     * Use counter.newCounting(MessageReceivedEvent event) instead
     */
    @Deprecated
    public void countingOld(MessageReceivedEvent event) {
        var msg = event.getMessage().getContentRaw().toLowerCase();

        if (msg.contains(":kekw")) {
            long countLong = countOld(0);
            if (countLong % 100 == 0) {
                event.getChannel().sendMessage("Neuer <:kekw:1068624193064812654> Counter Milestone: **" + countLong + "**").queue();
            }
        }

        if (msg.contains("._.")) {
            long countLong = countOld(1);
            if (countLong % 100 == 0) {
                event.getChannel().sendMessage("Neuer `._.` Counter Milestone: **" + countLong + "**").queue();
            }
        }

        if (msg.contains(":sitting_dog")) {
            long countLong = countOld(2);
            if (countLong % 100 == 0) {
                event.getChannel().sendMessage("Neuer <:sitting_dog:1071543706680819813> Counter Milestone: **" + countLong + "**").queue();
            }
        }

        if (msg.contains(":amylee3popcorn")) {
            long countLong = countOld(3);
            if (countLong % 100 == 0) {
                event.getChannel().sendMessage("Neuer <:amylee3Popcorn:1074312205752872991> Counter Milestone: **" + countLong + "**").queue();
            }
        }

        if (msg.contains(":amylee3pieks")) {
            long countLong = countOld(4);
            if (countLong % 100 == 0) {
                event.getChannel().sendMessage("Neuer <:amylee3Pieks:1083064423410843699> Counter Milestone: **" + countLong + "**").queue();
            }
        }

        if (msg.contains("xd")) {
            long countLong = countOld(5);
            if (countLong % 100 == 0) {
                event.getChannel().sendMessage("Neuer `XD` Counter Milestone: **" + countLong + "**").queue();
            }
        }

        if (msg.contains(":feelslookingman")) {
            long countLong = countOld(6);
            if (countLong % 100 == 0) {
                event.getChannel().sendMessage("Neuer <:FeelsLookingMan:1068622760596750467> Counter Milestone: **" + countLong + "**").queue();
            }
        }

        if (msg.contains(".-.")) {
            long countLong = countOld(7);
            if (countLong % 100 == 0) {
                event.getChannel().sendMessage("Neuer `.-.` Counter Milestone: **" + countLong + "**").queue();
            }
        }

        if (msg.contains(":this")) {
            long countLong = countOld(8);
            if (countLong % 100 == 0) {
                event.getChannel().sendMessage("Neuer <:this:1068627533144273016> Counter Milestone: **" + countLong + "**").queue();
            }
        }
    }

    /**
     * Use counter.newCount(String phrase) instead
     */
    @Deprecated
    private long countOld(int line) {
        Path path = Configs.filePaths.counter;
        try {
            List<String> counter = Files.readAllLines(path); //Counter aus File

            long countLong = Long.parseLong(counter.get(line));
            countLong++;

            counter.set(line, Long.toString(countLong));

            Files.write(path, counter, Charset.defaultCharset()); //File schreiben
            return countLong;
        } catch (FileNotFoundException e) {
            log.printErr("Counter File at line " + line);
        } catch (IOException e) {
            log.printErr("Counter IO at line " + line);
        }
        return -1;
    }
}
