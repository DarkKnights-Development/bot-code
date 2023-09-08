package dev.darkknights.dkbot.cmds.slash;

import dev.darkknights.Logging;
import dev.darkknights.WrongGuildException;
import dev.darkknights.config.Configs;
import dev.darkknights.dkbot.DarkKnightsBot;
import dev.darkknights.dkbot.systems.selfroles.SbRoleSystem;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import org.json.simple.parser.ParseException;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class sudo {
    private String sudoData;

    public void cmd(SlashCommandInteractionEvent event) throws ParseException, WrongGuildException, MalformedURLException {

        var cmdData = event.getGuild().retrieveCommands().complete();

        var sudoUser = event.getOption("user").getAsUser();
        var sudoCmd = event.getOption("cmd").getAsString();
        try {
            sudoData = event.getOption("data").getAsString();
        } catch (NullPointerException e) {
            sudoData = "noData";
        }

        new Logging().logCmd("sudo", event.getUser().getName(), Map.of("sudouser", sudoUser.getName(), "sudoCmd", sudoCmd, "sudoData", sudoData));

        // Dropdown Menu erstellen wenn cmd mit SubCmds
        List<Command.Subcommand> subCmds = null;
        for (Command data : cmdData) {
            if (data.getName().equals(sudoCmd)) {
                subCmds = data.getSubcommands();
            }
        }

        if (!subCmds.isEmpty()) {
            List<SelectOption> options = new ArrayList<>();
            subCmds.forEach(subCommand -> options.add(SelectOption.of(subCommand.getName(), sudoUser.getId())));

            var sel = StringSelectMenu.create("sudoSubCmdSelect").addOptions(options).setPlaceholder(sudoCmd).build();

            event.getHook().sendMessage("/sudo " + sudoUser.getName() + " " + sudoCmd).addActionRow(sel).queue();
        } else {
            executeCommand(sudoUser, sudoCmd, "noSubCmd", event.getHook());
        }
    }


    public void sudoSelectMenu(StringSelectInteractionEvent event) throws ParseException, WrongGuildException, MalformedURLException {
        event.deferReply(true).queue();

        var options = event.getSelectedOptions();
        var sudoCmd = event.getSelectMenu().getPlaceholder();
        var sudoSubCmd = options.get(0).getLabel();
        var sudoUserId = options.get(0).getValue();
        var sudoUser = event.getJDA().getUserById(sudoUserId);

        //event.getMessage().delete().queue();

        executeCommand(sudoUser, sudoCmd, sudoSubCmd, event.getHook());

        event.getHook().deleteOriginal().queue();
    }


    private void executeCommand(User sudoUser, String sudoCmd, String sudoSubCmd, InteractionHook hook) throws ParseException, WrongGuildException, MalformedURLException {
        hook.setEphemeral(true).sendMessage("Executing /" + sudoCmd + " as " + sudoUser.getName()).queue();

        var dk = DarkKnightsBot.getJDA().getGuildById(Configs.gildenIDs.darkKnightsID);

        switch (sudoCmd) {
            case "link" -> {
                var member = dk.getMemberById(sudoUser.getId());
                var ign = sudoData;
                if (sudoData.equals("noData")) {
                    hook.sendMessage("Error: data was null -> data = ign").setEphemeral(true).queue();
                    return;
                }

                new link().cmd(member, ign, hook);
            }
            case "sbroles", "roles", "sbrollen", "rollen" -> {
                var roleOption = sudoData;
                if (sudoData.equals("noData")) {
                    hook.sendMessage("Error: data was null -> data = role").setEphemeral(true).queue();
                    return;
                }

                var support = dk.getTextChannelById(Configs.channelIDs.supportTicketID);

                var sbRoleSys = new SbRoleSystem();
                switch (sudoSubCmd) {
                    //type: 1 == add | 0 == remove
                    case "add" -> sbRoleSys.sbRolesCmd(roleOption, support, sudoUser, dk, hook, 1);
                    case "remove" -> sbRoleSys.sbRolesCmd(roleOption, support, sudoUser, dk, hook, 0);
                }
            }
        }
    }
}
