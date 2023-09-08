package dev.darkknights.dkbot.systems.selfroles;

import dev.darkknights.WrongGuildException;
import dev.darkknights.config.Configs;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;

import java.awt.*;
import java.util.List;

public class GildenRoleSystem {

    public void gildenRoleSetupCmd(SlashCommandInteractionEvent event) {
        var channel = event.getChannel();

        // Gilden Rollen Auswahl Msg
        var eb = new EmbedBuilder()
                .setColor(Color.YELLOW)
                .setTitle("Gilden Rollen")
                .setDescription("Wähle unten deine Gilde aus." +
                    "\nUm deine Rolle zu entfernen klicke auf `Rolle entfernen`.")
                .setFooter(Configs.devs.eposs.cmdDevText(), Configs.devs.eposs.icon());

        channel.sendMessageEmbeds(eb.build())
                .addActionRow(StringSelectMenu.create("Gilden Rollen")
                        .addOption("🛡 DarkKnights1", "dk1")
                        .addOption("🛡 DarkKnights2", "dk2")
                        .addOption("🛡 DarkKnights4", "dk4")
                        .addOption("🛡 DarkKnights5", "dk5")
                        .addOption("🛡 DarkKnights6", "dk6")
                        .addOption("🛡 DarkKnights7", "dk7")
                        .addOption("🛡 DarkKnights8", "dk8")
                        .addOption("🛡 DarkKnightsDungeon", "dkd")
                        .build())
                .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.danger("removeG", "Rolle entfernen")).queue();
        event.getHook().sendMessage("Done").queue();
    }

    public void gildenRoleStringSelect(StringSelectInteractionEvent event) throws WrongGuildException {
        event.deferReply().setEphemeral(true).queue();
        var selected = event.getValues();
        var guild = event.getGuild();
        var member = event.getMember();

        final var dkd = guild.getRoleById(Configs.rollenIDs.gilden.dkd);
        final var dk1 = guild.getRoleById(Configs.rollenIDs.gilden.dk1);
        final var dk2 = guild.getRoleById(Configs.rollenIDs.gilden.dk2);
        final var dk4 = guild.getRoleById(Configs.rollenIDs.gilden.dk4);
        final var dk5 = guild.getRoleById(Configs.rollenIDs.gilden.dk5);
        final var dk6 = guild.getRoleById(Configs.rollenIDs.gilden.dk6);
        final var dk7 = guild.getRoleById(Configs.rollenIDs.gilden.dk7);
        final var dk8 = guild.getRoleById(Configs.rollenIDs.gilden.dk8);

        boolean hasGRoles = false;
        if (member.getRoles().contains(dk1) ||
                member.getRoles().contains(dk2) ||
                member.getRoles().contains(dk4) ||
                member.getRoles().contains(dk5) ||
                member.getRoles().contains(dk6) ||
                member.getRoles().contains(dk7) ||
                member.getRoles().contains(dk8) ||
                member.getRoles().contains(dkd)
        ) {
            hasGRoles = true;
            event.getHook().setEphemeral(true).sendMessage("Du kannst nur eine Gilden Rolle haben. \nKlicke `Rolle entfernen` und versuche es erneut.").queue();
        }

        if (selected.contains("dkd") && !hasGRoles) {
            guild.addRoleToMember(member, dkd).queue();
            event.getHook().setEphemeral(true).sendMessage(dkd.getAsMention() + " hinzugefügt.").queue();
        }
        if (selected.contains("dk1") && !hasGRoles) {
            guild.addRoleToMember(member, dk1).queue();
            event.getHook().setEphemeral(true).sendMessage(dk1.getAsMention() + " hinzugefügt.").queue();
        }
        if (selected.contains("dk2") && !hasGRoles) {
            guild.addRoleToMember(member, dk2).queue();
            event.getHook().setEphemeral(true).sendMessage(dk2.getAsMention() + " hinzugefügt.").queue();
        }
        if (selected.contains("dk4") && !hasGRoles) {
            guild.addRoleToMember(member, dk4).queue();
            event.getHook().setEphemeral(true).sendMessage(dk4.getAsMention() + " hinzugefügt.").queue();
        }
        if (selected.contains("dk5") && !hasGRoles) {
            guild.addRoleToMember(member, dk5).queue();
            event.getHook().setEphemeral(true).sendMessage(dk5.getAsMention() + " hinzugefügt.").queue();
        }
        if (selected.contains("dk6") && !hasGRoles) {
            guild.addRoleToMember(member, dk6).queue();
            event.getHook().setEphemeral(true).sendMessage(dk6.getAsMention() + " hinzugefügt.").queue();
        }
        if (selected.contains("dk7") && !hasGRoles) {
            guild.addRoleToMember(member, dk7).queue();
            event.getHook().setEphemeral(true).sendMessage(dk7.getAsMention() + " hinzugefügt.").queue();
        }
        if (selected.contains("dk8") && !hasGRoles) {
            guild.addRoleToMember(member, dk8).queue();
            event.getHook().setEphemeral(true).sendMessage(dk8.getAsMention() + " hinzugefügt.").queue();
        }
    }

    public void gildenRoleRemoveButton(ButtonInteractionEvent event) throws WrongGuildException {
        event.deferReply().setEphemeral(true).queue();
        var member = event.getMember();
        var guild = event.getGuild();

        final var dkd = guild.getRoleById(Configs.rollenIDs.gilden.dkd);
        final var dk1 = guild.getRoleById(Configs.rollenIDs.gilden.dk1);
        final var dk2 = guild.getRoleById(Configs.rollenIDs.gilden.dk2);
        final var dk4 = guild.getRoleById(Configs.rollenIDs.gilden.dk4);
        final var dk5 = guild.getRoleById(Configs.rollenIDs.gilden.dk5);
        final var dk6 = guild.getRoleById(Configs.rollenIDs.gilden.dk6);
        final var dk7 = guild.getRoleById(Configs.rollenIDs.gilden.dk7);
        final var dk8 = guild.getRoleById(Configs.rollenIDs.gilden.dk8);
        var guildRoles = List.of(dkd, dk1, dk2, dk4, dk5, dk6, dk7, dk8);

        for (Role role: guildRoles) {
            guild.removeRoleFromMember(member, role).queue();
        }

        event.getHook().setEphemeral(true).sendMessage("Gilden Rolle entfernt.").queue();
    }
}
