package dev.darkknights.dkbot.cmds;

import dev.darkknights.config.Configs;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class CmdUseCount {

    private final Path stats = Configs.filePaths.stats;

    private JSONObject getStatsJson() throws IOException, ParseException {
        var statsList = Files.readAllLines(stats);
        var counterString = new StringBuilder();
        for (String s: statsList) {
            counterString.append(s);
        }

        var parser = new JSONParser();
        return (JSONObject) parser.parse(counterString.toString());
    }

    public void cmdUse(String cmd) throws IOException, ParseException {
        var statsJson = getStatsJson();

        if (!statsJson.containsKey(cmd)) {
            statsJson.put(cmd, 1);
        } else {
            long count = (long) statsJson.get(cmd);
            statsJson.replace(cmd, count + 1);
        }

        var sum = (long) statsJson.get("sum");
        sum++;
        statsJson.put("sum", sum);

        Files.writeString(stats, statsJson.toJSONString());
    }

    public void statsCmd(SlashCommandInteractionEvent event) throws IOException, ParseException {
        var statsJson = getStatsJson();

        var eb = new EmbedBuilder()
                .setColor(Color.green)
                .setTitle("DarkKnights Bot Stats")
                .setFooter(Configs.devs.eposs.cmdDevText(), Configs.devs.eposs.icon())
                .setDescription("Aufzeichnung der Stats seit <t:1687348080:D>" +
                        "\nGenutzte Befehle: " + statsJson.get("sum"));

        statsJson.remove("sum");
        statsJson.keySet().forEach(cmd -> eb.addField("/" +  cmd.toString(), "Anzahl: " + statsJson.get(cmd), true));

        event.getHook().sendMessageEmbeds(eb.build()).queue();
    }
}
