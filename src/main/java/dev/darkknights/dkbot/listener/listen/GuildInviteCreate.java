package dev.darkknights.dkbot.listener.listen;

import dev.darkknights.config.Configs;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.guild.invite.GuildInviteCreateEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.awt.*;
import java.util.Objects;

public class GuildInviteCreate {
    public void onGuildInviteCreate(GuildInviteCreateEvent event) {
        var inviter = event.getInvite().getInviter();

        var created = event.getInvite().getTimeCreated().toEpochSecond();
        var age = event.getInvite().getMaxAge();
        var time = created + age;
        var timestamp = "Invalid <t:" + time + ":R>";
        if (age == 0) timestamp = "Infinite Duration";

        var uses = event.getInvite().getMaxUses();
        var maxUses = "Max Uses: " + uses;
        if (uses == 0) maxUses = "Infinite Uses";

        var eb = new EmbedBuilder()
                .setFooter(Configs.devs.eposs.cmdDevText(), Configs.devs.eposs.icon())
                .setColor(Color.YELLOW)
                .setTitle("Neuer Invite erstellt")
                .setDescription("__Von:__" +
                        "\n" + inviter.getAsMention() +
                        "\n" + inviter.getName() +
                        "\n" + inviter.getIdLong() +
                        "\n\n__Invite:__" +
                        "\n" + event.getInvite().getUrl() +
                        "\n" + timestamp +
                        "\n" + maxUses +
                        "\nChannel: <#" + event.getInvite().getChannel().getId() + "> (" + event.getInvite().getChannel().getName() + ")" +
                        "\nTemporary: " + event.getInvite().isTemporary()
                )
                .setThumbnail(inviter.getAvatarUrl());

        try {
            event.getGuild().getTextChannelById(Configs.channelIDs.logs.serverLogID).sendMessageEmbeds(eb.build())
                    .addActionRow(Button.danger("delInvite", "Invite löschen"))
                    .queue();
        } catch (NullPointerException e) {
            Objects.requireNonNull(event.getGuild().getDefaultChannel()).asTextChannel().sendMessage("Es gab einen Fehler beim Ausführen des Befehls.").queue();
        }
    }
}
