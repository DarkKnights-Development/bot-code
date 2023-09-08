package dev.darkknights.dkbot.cmds.slash;

import de.transcript.Transcript;
import dev.darkknights.Logging;
import dev.darkknights.config.Configs;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.*;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class ticket {
    public void cmd(SlashCommandInteractionEvent event) {
        String subCmd = event.getSubcommandName();

        var channel = event.getChannel();
        var user = event.getUser();

        if (user.getIdLong() == Configs.devs.eposs.discordID()) {
            if (channel.getName().contains("spende")) {
                switch (subCmd) {
                    case "close" -> {
                        var log = event.getGuild().getTextChannelById(Configs.channelIDs.logs.ticketLogID);

                        var eb = new EmbedBuilder()
                                .setFooter(Configs.devs.eposs.cmdDevText(), Configs.devs.eposs.icon())
                                .setTitle("Spenden-Ticket Log")
                                .setColor(Color.CYAN)
                                .setDescription("Das Spenden Ticket `#" + channel.getName() + "` wurde gelöscht.");

                        // https://github.com/GamingLPyt/JDA-HTML-Transcript
                        var transcript = new Transcript();
                        try {
                            transcript.createTranscript(channel, log);
                        } catch (IOException e) {
                            new Logging().printErr("Ticket Transcript");
                            event.getHook().sendMessage("Transcript konnte nicht erstellt werden.").queue();
                            break;
                        }
                        log.sendMessageEmbeds(eb.build()).queue();
                        event.getHook().sendMessage("Ticket wird gelöscht...").queue();
                        channel.delete().queueAfter(5L, TimeUnit.SECONDS);
                    }
                    case "delete" -> {
                        event.getHook().sendMessage("Ticket wird gelöscht...").queue();
                        channel.delete().queueAfter(5L, TimeUnit.SECONDS);
                    }
                }
            } else {
                event.getHook().sendMessage("Dies ist kein Ticket Channel und kann nicht über diesen Cmd gelöscht werden.").queue();
            }
        } else {
            event.getHook().sendMessage("Du hast nicht die Berechtigung, das Ticket zu schließen.").queue();
        }
    }
}
