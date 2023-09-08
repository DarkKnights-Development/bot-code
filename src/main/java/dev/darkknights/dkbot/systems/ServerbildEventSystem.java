package dev.darkknights.dkbot.systems;

import dev.darkknights.config.Configs;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.utils.FileUpload;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import static java.lang.Thread.sleep;

public class ServerbildEventSystem {
    private final static long einreichenID = 1102837595882393630L; // Einreichen Channel
    private final static long abstimmungID = 1102837696331788380L; // Abstimmung Channel

    public void bildEinreichen(MessageReceivedEvent event) {
        if (event.getChannel().getIdLong() == einreichenID) {
            var msg = event.getMessage();
            var author = msg.getAuthor();
            var channel = event.getChannel().asTextChannel();

            if (author.getIdLong() == Configs.botIDs.dkBotID) return;

            var attachmentList = msg.getAttachments();

            if (attachmentList.size() == 1 && attachmentList.get(0).isImage()) {
                var msgID = msg.reply("Bild von " + author.getAsMention() + " wird gespeichert...").complete().getIdLong();

                var eb = new EmbedBuilder()
                        .setFooter(Configs.devs.eposs.cmdDevText(), Configs.devs.eposs.icon())
                        .setColor(Color.yellow)
                        .setAuthor(author.getName(), null, author.getAvatarUrl())
                        .setDescription(msg.getContentRaw());

                var attachment = attachmentList.get(0);

                var bildName = author.getName() + "-" + attachment.getFileName();
                var bildNamePath = Paths.get("Serverbilder", bildName);
                Path bildPath = null;

                try {
                    Files.deleteIfExists(bildNamePath);
                    bildPath = Files.createFile(bildNamePath);
                } catch (IOException e) {
                    channel.editMessageById(msgID, author.getAsMention() + " Es gab einen Fehler. Eposs wird sich darum kümmern.")
                            .complete().delete().queueAfter(5, TimeUnit.SECONDS);
                    msg.delete().queue();
                    e.printStackTrace();
                }

                attachment.getProxy().downloadToPath(bildPath);

                try {
                    sleep(5000);
                } catch (InterruptedException e) {
                    msg.reply(author.getAsMention() + " Es gab einen Fehler. Eposs wird sich darum kümmern.").complete()
                            .delete().queueAfter(5, TimeUnit.SECONDS);
                    msg.delete().queue();
                    e.printStackTrace();
                }

                var eposs = event.getJDA().getUserById(Configs.userIDs.epossID);
                String bildUrl = eposs.openPrivateChannel().flatMap(privateChannel -> privateChannel.sendMessage("")
                        .addFiles(FileUpload.fromData(bildNamePath, bildName))).complete().getAttachments().get(0).getProxyUrl();

                eb.setImage(bildUrl);

                event.getGuild().getTextChannelById(abstimmungID).sendMessageEmbeds(eb.build()).complete()
                        .addReaction(Emoji.fromCustom("upvote", 1102850854857494599L, false)).queue();

                channel.editMessageById(msgID,"Bild von " + author.getAsMention() + " gespeichert.").complete()
                        .delete().queueAfter(10, TimeUnit.SECONDS);
                msg.delete().queue();
            } else {
                msg.reply(author.getAsMention() + " Jede Nachricht muss **genau 1 Bild** enthalten.").complete()
                        .delete().queueAfter(10, TimeUnit.SECONDS);
                msg.delete().queue();
            }
        }
    }

    public void cmdEventAuswertung(SlashCommandInteractionEvent event) {
        var channel = event.getGuild().getTextChannelById(abstimmungID);
        var up = Emoji.fromCustom(Emoji.fromCustom("upvote", 1102850854857494599L, false));

        var count = 0;

        var history = MessageHistory.getHistoryFromBeginning(channel).complete();
        var retrievedHistory = history.getRetrievedHistory();

        var eb = new EmbedBuilder()
                .setColor(Color.green)
                .setTitle("Event Auswertung")
                .setFooter(Configs.devs.eposs.cmdDevText(), Configs.devs.eposs.icon());

        for (Message message : retrievedHistory) {
            if (message.getReaction(up) != null) {
                var reaction = message.getReaction(up);
                if (reaction.hasCount()) {
                    var votes = reaction.getCount() - 1;
                    count = count + votes;

                    var author = message.getEmbeds().get(0).getAuthor().getName();
                    if (author != null) eb.addField(author, up.getFormatted() + " Votes: " + votes, true);
                }
            }
        }
        eb.setDescription(up.getFormatted() + " Count = " + count);
        event.getHook().sendMessageEmbeds(eb.build()).queue();
    }
}