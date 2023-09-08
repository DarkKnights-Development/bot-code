package dev.darkknights.dkbot.systems.selfroles;

import dev.darkknights.WrongGuildException;
import dev.darkknights.config.Configs;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;

import java.awt.*;

public class ClassRoleSystem {

    public void classRoleSetupCmd(SlashCommandInteractionEvent event) {
        var channel = event.getChannel();

        // Class Rollen Auswahl Msg
        var eb = new EmbedBuilder()
                .setColor(Color.YELLOW)
                .setTitle("Dungeon Class Rollen")
                .setDescription("Wähle unten deine Class Rollen aus."
                    + "\nUm alle Rollen zu entferenen klicke auf `Alle entfernen`.")
                .setFooter(Configs.devs.eposs.cmdDevText(), Configs.devs.eposs.icon());

        channel.sendMessageEmbeds(eb.build())
                .addActionRow(StringSelectMenu.create("Class Rollen")
                        .addOption("🏹 Archer", "archer")
                        .addOption("🧙 Mage", "mage")
                        .addOption("⚔️ Berserker", "berserk")
                        .addOption("🛡 Tank", "tank")
                        .addOption("💚 Healer", "healer")
                        .build())
                .addActionRow(Button.danger("removeC", "Alle entfernen")).queue();
        event.getHook().sendMessage("Done").queue();
    }

    public void classRoleStringSelect(StringSelectInteractionEvent event) throws WrongGuildException {
        var selected = event.getValues();
        var user = event.getUser();
        var dk = event.getGuild();

        final var archerRole = dk.getRoleById(Configs.rollenIDs.classRollen.archer);
        final var mageRole = dk.getRoleById(Configs.rollenIDs.classRollen.mage);
        final var bersRole = dk.getRoleById(Configs.rollenIDs.classRollen.bers);
        final var tankRole = dk.getRoleById(Configs.rollenIDs.classRollen.tank);
        final var healerRole = dk.getRoleById(Configs.rollenIDs.classRollen.healer);

        if (selected.contains("archer")) {
            dk.addRoleToMember(user, archerRole).queue();
            event.reply("Du hast " + archerRole.getAsMention() + " erhalten.").setEphemeral(true).queue();
        }
        if (selected.contains("mage")) {
            dk.addRoleToMember(user, mageRole).queue();
            event.reply("Du hast " + mageRole.getAsMention() + " erhalten.").setEphemeral(true).queue();
        }
        if (selected.contains("berserk")) {
            dk.addRoleToMember(user, bersRole).queue();
            event.reply("Du hast " + bersRole.getAsMention() + " erhalten.").setEphemeral(true).queue();
        }
        if (selected.contains("tank")) {
            dk.addRoleToMember(user, tankRole).queue();
            event.reply("Du hast " + tankRole.getAsMention() + " erhalten.").setEphemeral(true).queue();
        }
        if (selected.contains("healer")) {
            dk.addRoleToMember(user, healerRole).queue();
            event.reply("Du hast " + healerRole.getAsMention() + " erhalten.").setEphemeral(true).queue();
        }
    }

    public void classRoleRemoveButton(ButtonInteractionEvent event) throws WrongGuildException {
        event.deferReply().setEphemeral(true).queue();
        var member = event.getMember();
        var dk = event.getGuild();

        final var archerRole = dk.getRoleById(Configs.rollenIDs.classRollen.archer);
        final var mageRole = dk.getRoleById(Configs.rollenIDs.classRollen.mage);
        final var bersRole = dk.getRoleById(Configs.rollenIDs.classRollen.bers);
        final var tankRole = dk.getRoleById(Configs.rollenIDs.classRollen.tank);
        final var healerRole = dk.getRoleById(Configs.rollenIDs.classRollen.healer);

        dk.removeRoleFromMember(member, archerRole).queue();
        dk.removeRoleFromMember(member, mageRole).queue();
        dk.removeRoleFromMember(member, bersRole).queue();
        dk.removeRoleFromMember(member, tankRole).queue();
        dk.removeRoleFromMember(member, healerRole).queue();

        event.getHook().setEphemeral(true).sendMessage("Alle Class Rollen entfernt.").queue();
    }
}
