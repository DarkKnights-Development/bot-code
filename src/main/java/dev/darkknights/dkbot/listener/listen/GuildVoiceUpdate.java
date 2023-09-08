package dev.darkknights.dkbot.listener.listen;

import dev.darkknights.config.Configs;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;

public class GuildVoiceUpdate {
    public void onGuildVoiceUpdate(GuildVoiceUpdateEvent event) {
        if (event.getEntity().getVoiceState().inAudioChannel()) {
            var acu = event.getChannelJoined();

            var channel = Configs.channelIDs.voiceChannelBlacklist;

            for (long ID : channel) {
                if (acu.getIdLong() == ID) {
                    for (Member member : acu.getMembers()) {
                        acu.getGuild().kickVoiceMember(member).queue();
                    }
                }
            }
        }
    }
}
