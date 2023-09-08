package dev.darkknights.dkbot.systems;

import dev.darkknights.config.Configs;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class UmfragenChannel {

    private static final long umfrageID = 1114933364336636056L;

    public void umfrageCreate(MessageReceivedEvent event) {
        if (event.getChannel().getIdLong() == umfrageID) {
            var checkTrue = Emoji.fromCustom(Emoji.fromCustom("checkTrue", 1115529855786819634L, true));
            var checkNeutral = Emoji.fromCustom(Emoji.fromCustom("checkNeutral", 1115529902419083314L, true));
            var checkFalse = Emoji.fromCustom(Emoji.fromCustom("checkFalse", 1115529930646765608L, true));

            var msg = event.getMessage();
            var author = msg.getAuthor();
            var channel = event.getChannel().asTextChannel();

            if (author.getIdLong() == Configs.botIDs.dkBotID) return;

            msg.addReaction(checkTrue).complete();
            msg.addReaction(checkNeutral).complete();
            msg.addReaction(checkFalse).complete();

            channel.createThreadChannel("Umfrage - " + author.getName(), msg.getId()).queue();
        }
    }
}
