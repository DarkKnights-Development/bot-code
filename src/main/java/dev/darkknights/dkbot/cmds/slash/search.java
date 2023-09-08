package dev.darkknights.dkbot.cmds.slash;

import dev.darkknights.config.Configs;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.awt.*;

public class search {

    public void searchCmd(SlashCommandInteractionEvent event) {
        var channel = event.getChannel();
        var phrase = event.getOption("phrase").getAsString();

        // Suche nach Phrase
        var result = new EmbedBuilder()
                .setColor(Color.green)
                .setTitle("Suche nach: `" + phrase + "` in den letzten 1000 Nachrichten")
                .setFooter(Configs.devs.eposs.cmdDevText(), Configs.devs.eposs.icon());

        var firstID = channel.getLatestMessageId();

        for (int i = 0; i < 10; i++) {
            var msgList = channel.getHistoryBefore(firstID,100).complete().getRetrievedHistory();

            msgList.forEach(msg -> {
                var content = msg.getContentRaw();
                if (content.contains(phrase)) {
                    content = content.replace(phrase, "`" + phrase + "`");

                    result.addField(msg.getAuthor().getEffectiveName() + " <t:" +
                                    msg.getTimeCreated().toEpochSecond() +
                                    ":R> (" + msg.getJumpUrl() + ")",
                            content, false);
                }
            });

            int count = msgList.size()-1;
            if (count < 0) break;
            firstID = msgList.get(count).getId();
        }

        if (result.getFields().size() == 0) {
            event.getHook().setEphemeral(true).sendMessage("Suche nach: `" + phrase + "` in den letzten 1000 Nachrichten..." +
                    "\n**Keine Nachricht gefunden.**").queue();
        } else {
            try {
                event.getHook().setEphemeral(true).sendMessageEmbeds(result.build()).queue();
            } catch (IllegalStateException e) {
                event.getHook().setEphemeral(true).sendMessage("Suche nach: `" + phrase + "` in den letzten 1000 Nachrichten..." +
                        "\n**Zu viele Nachrichten gefunden. Versuche eine spezifischere Suche**").queue();
            }
        }
    }
}
