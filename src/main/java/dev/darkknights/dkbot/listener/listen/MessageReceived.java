package dev.darkknights.dkbot.listener.listen;

import dev.darkknights.WrongGuildException;
import dev.darkknights.config.Configs;
import dev.darkknights.dkbot.cmds.slash.counter;
import dev.darkknights.dkbot.systems.UmfragenChannel;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.*;

public class MessageReceived {

    public void onMessageReceived(MessageReceivedEvent event) {
        var msg = event.getMessage();
        var channel = event.getChannel();
        //###########################################
        //# Nachrichten in Servern
        //###########################################
        if (event.isFromGuild()) {

            // Krill Issue Gif
            /*String msgContent = msg.getContentRaw().toLowerCase();
            if (msgContent.contains("skill issue") || msgContent.contains("krill issue")) {
                msg.reply("https://tenor.com/view/krill-issue-skill-issue-krill-issue-gif-25572478").queue();
            }*/

            // Auto Publish Nachrichten in News Channeln
            var apList = Configs.channelIDs.autoPublishList;
            for (long id : apList)
                if (channel.getIdLong() == id) channel.asNewsChannel().crosspostMessageById(msg.getIdLong()).queue();

            //ServerbildEventSystem.bildEinreichen(event);

            new UmfragenChannel().umfrageCreate(event);

            try {
                if (event.getGuild() == Configs.gildenIDs.getDkGuild()) {
                    // Counter
                    new counter().countingMsgReceived(event);
                }
            } catch (WrongGuildException e) {
                e.setLocation("dev.darkknights.listeners.EventListener.onMessageReceived");
            }
        }

        if (event.isFromType(ChannelType.PRIVATE) && event.getAuthor().getIdLong() != Configs.botIDs.dkBotID) {
            var eposs = event.getJDA().getUserById(Configs.userIDs.epossID);

            try {
                msg.getEmbeds().get(0);
            } catch (IndexOutOfBoundsException e) {
                var eb = new EmbedBuilder()
                        .setColor(Color.BLUE)
                        .setAuthor(event.getAuthor().getName(), event.getAuthor().getAvatarUrl(), event.getAuthor().getAvatarUrl())
                        .setDescription(msg.getContentRaw());

                eposs.openPrivateChannel().flatMap(privateChannel -> privateChannel.sendMessageEmbeds(eb.build())).queue();
            }
        }
    }
}
