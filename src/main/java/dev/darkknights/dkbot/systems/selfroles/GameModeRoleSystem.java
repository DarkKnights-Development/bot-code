package dev.darkknights.dkbot.systems.selfroles;

import dev.darkknights.WrongGuildException;
import dev.darkknights.config.Configs;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;

import java.awt.*;

public class GameModeRoleSystem {

    public void gameModeRoleSetupCmd(SlashCommandInteractionEvent event) {
        var channel = event.getChannel();

        //Ping Rollen Auswahl
        var eb = new EmbedBuilder()
                .setColor(Color.YELLOW)
                .setTitle("Gamemode Rollen")
                .setDescription("Wähle unten deine Gamemode Rolle aus."
                    + "\nUm alle Rollen zu entfernen klicke auf `Alle entfernen`.")
                .setFooter(Configs.devs.eposs.cmdDevText(), Configs.devs.eposs.icon());

        channel.sendMessageEmbeds(eb.build()).addActionRow(
                StringSelectMenu.create("Gamemode Rollen")
                        .addOption("♻️ Ironman", "ironmanrolle")
                        .addOption("🏝 Stranded", "strandedrolle")
                        .build())
                .addActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.danger("removeGm", "Alle entfernen")).queue();
        event.getHook().sendMessage("Done").queue();
    }

    public void gameModeRoleStringSelect(StringSelectInteractionEvent event) throws WrongGuildException {
        event.deferReply().setEphemeral(true).queue();

        var selected = event.getValues();
        var user = event.getUser();
        var dk = event.getGuild();

        final var ironManRolle = dk.getRoleById(Configs.rollenIDs.general.ironman);
        final var strandedRolle = dk.getRoleById(Configs.rollenIDs.general.stranded);

        if (selected.contains("ironmanrolle")) {
            dk.addRoleToMember(user, ironManRolle).queue();
            event.getHook().sendMessage("Du hast " + ironManRolle.getAsMention() + " erhalten.").setEphemeral(true).queue();
        }
        if (selected.contains("strandedrolle")) {
            dk.addRoleToMember(user, strandedRolle).queue();
            event.getHook().sendMessage("Du hast " + strandedRolle.getAsMention() + " erhalten.").setEphemeral(true).queue();
        }
    }

    public void gameModeRoleRemoveButton(ButtonInteractionEvent event) throws WrongGuildException {
        event.deferReply().setEphemeral(true).queue();

        var member = event.getMember();
        var dk = event.getGuild();

        final var ironManRolle = dk.getRoleById(Configs.rollenIDs.general.ironman);
        final var strandedRolle = dk.getRoleById(Configs.rollenIDs.general.stranded);

        dk.removeRoleFromMember(member, ironManRolle).queue();
        dk.removeRoleFromMember(member, strandedRolle).queue();

        event.getHook().setEphemeral(true).sendMessage("Gamemode Rollen entfernt.").queue();
    }
}
