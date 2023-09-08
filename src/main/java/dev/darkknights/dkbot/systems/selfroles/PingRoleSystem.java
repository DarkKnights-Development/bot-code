package dev.darkknights.dkbot.systems.selfroles;

import dev.darkknights.WrongGuildException;
import dev.darkknights.config.Configs;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;

import java.awt.*;

public class PingRoleSystem {

    public void pingRoleSetupCmd(SlashCommandInteractionEvent event) {
        var channel = event.getChannel();

        //Ping Rollen Auswahl
        var eb = new EmbedBuilder()
                .setColor(Color.YELLOW)
                .setTitle("Discord Ping Rollen")
                .setDescription("Wähle unten deine Ping Rolle aus."
                    + "\nUm alle Rollen zu entfernen klicke auf `Alle entfernen`.")
                .setFooter(Configs.devs.tillmen.cmdDevText(), Configs.devs.tillmen.icon());

        channel.sendMessageEmbeds(eb.build()).addActionRow(StringSelectMenu.create("Ping Rollen")

                        .addOption("News ᴾᴵᴺᴳ", "newsrolle")
                        .addOption("Bot-News ᴾᴵᴺᴳ", "botnewrolle")
                        .addOption("Umfragen ᴾᴵᴺᴳ", "umfragenrolle")
                        .addOption("Skyblock-Leaks ᴾᴵᴺᴳ", "skyleakrolle")
                        .addOption("Skyblock-Update ᴾᴵᴺᴳ", "skyupdaterolle")
                        .build())
                .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.danger("removeP", "Alle entfernen")).queue();
        event.getHook().sendMessage("Done").queue();
    }

    public void pingRoleStringSelect(StringSelectInteractionEvent event) throws WrongGuildException {
        event.deferReply().setEphemeral(true).queue();
        var selected = event.getValues();
        var user = event.getUser();
        var dk = event.getGuild();

        final var newsPing = dk.getRoleById(Configs.rollenIDs.pingRollen.news);
        final var botNewsPing = dk.getRoleById(Configs.rollenIDs.pingRollen.botNews);
        final var umfragenPing = dk.getRoleById(Configs.rollenIDs.pingRollen.umfragen);
        final var sbLeaksPing = dk.getRoleById(Configs.rollenIDs.pingRollen.sbLeaks);
        final var sbNewsPing = dk.getRoleById(Configs.rollenIDs.pingRollen.sbNews);

        if (selected.contains("newsrolle")) {
            dk.addRoleToMember(user, newsPing).queue();
            event.getHook().sendMessage("Du hast " + newsPing.getAsMention() + " erhalten.").setEphemeral(true).queue();
        }
        if (selected.contains("botnewrolle")) {
            dk.addRoleToMember(user, botNewsPing).queue();
            event.getHook().sendMessage("Du hast " + botNewsPing.getAsMention() + " erhalten.").setEphemeral(true).queue();
        }
        if (selected.contains("umfragenrolle")) {
            dk.addRoleToMember(user, umfragenPing).queue();
            event.getHook().sendMessage("Du hast " + umfragenPing.getAsMention() + " erhalten.").setEphemeral(true).queue();
        }
        if (selected.contains("skyleakrolle")) {
            dk.addRoleToMember(user, sbLeaksPing).queue();
            event.getHook().sendMessage("Du hast " + sbLeaksPing.getAsMention() + " erhalten.").setEphemeral(true).queue();
        }
        if (selected.contains("skyupdaterolle")) {
            dk.addRoleToMember(user, sbNewsPing).queue();
            event.getHook().sendMessage("Du hast " + sbNewsPing.getAsMention() + " erhalten.").setEphemeral(true).queue();
        }
    }

    public void pingRoleRemoveButton(ButtonInteractionEvent event) throws WrongGuildException {
        event.deferReply().setEphemeral(true).queue();
        var member = event.getMember();
        var dk = event.getGuild();

        final var newsPing = dk.getRoleById(Configs.rollenIDs.pingRollen.news);
        final var botNewsPing = dk.getRoleById(Configs.rollenIDs.pingRollen.botNews);
        final var umfragenPing = dk.getRoleById(Configs.rollenIDs.pingRollen.umfragen);
        final var sbLeaksPing = dk.getRoleById(Configs.rollenIDs.pingRollen.sbLeaks);
        final var sbNewsPing = dk.getRoleById(Configs.rollenIDs.pingRollen.sbNews);


        dk.removeRoleFromMember(member, newsPing).queue();
        dk.removeRoleFromMember(member, botNewsPing).queue();
        dk.removeRoleFromMember(member, umfragenPing).queue();
        dk.removeRoleFromMember(member, sbLeaksPing).queue();
        dk.removeRoleFromMember(member, sbNewsPing).queue();

        event.getHook().setEphemeral(true).sendMessage("Ping Rollen entfernt.").queue();
    }
}
