package dev.darkknights.dkbot.cmds;

import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CmdManager extends ListenerAdapter {

    // alle Farben für Embeds
    private final String[] colors = new String[]{"black", "dark_grey", "grey", "light_grey", "white", "red", "orange", "yellow", "green", "cyan", "blue", "pink", "magenta"};

    // alle Skyblock Rollen
    private final String[] sbRoleOptions = new String[]{"Catacombs", "SbLevel", "all"};

    private final List<CommandData> cmdData = new ArrayList<>();
    private boolean cmdSetup = false;

    //###########################################
    //# Autocomplete Options
    //###########################################
    @Override
    public void onCommandAutoCompleteInteraction(CommandAutoCompleteInteractionEvent event) {
        var cmdName = event.getName();
        var focusedOption = event.getFocusedOption();

        //###########################################
        //# Embed Color
        //###########################################
        if (cmdName.equals("send") && event.getSubcommandName().equals("embed") && focusedOption.getName().equals("color")) {
            var options = Stream.of(colors)
                    .filter(color1 -> color1.startsWith(focusedOption.getValue())) // only display words that start with the user's current input
                    .map(color2 -> new Command.Choice(color2, color2)) // map the words to choices
                    .collect(Collectors.toList());
            event.replyChoices(options).queue();
        }


        //###########################################
        //# SbRoles Options
        //###########################################
        if (cmdName.equals("sbroles") || cmdName.equals("roles") || cmdName.equals("sbrollen") || cmdName.equals("rollen")) {
            if (focusedOption.getName().equals("roles")) {
                var options = Stream.of(sbRoleOptions)
                        .filter(role -> role.startsWith(focusedOption.getValue())) // only display words that start with the user's current input
                        .map(role -> new Command.Choice(role, role)) // map the words to choices
                        .collect(Collectors.toList());
                event.replyChoices(options).queue();
            }
        }


        //###########################################
        //# Sudo Cmd Options
        //###########################################
        if (cmdName.equals("sudo") && focusedOption.getName().equals("cmd")) {
            var cmdList = new ArrayList<String>();
            cmdData.forEach(data -> cmdList.add(data.getName()));
            String[] cmds = cmdList.toArray(new String[0]);

            var options = Stream.of(cmds)
                    .filter(cmd -> cmd.startsWith(focusedOption.getValue())) // only display words that start with the user's current input
                    .map(cmd -> new Command.Choice(cmd, cmd)) // map the words to choices
                    .collect(Collectors.toList());
            event.replyChoices(options).queue();
        }
    }


    //###########################################
    //# Guild Commands
    //###########################################
    private void setUpCommands() {
        if (cmdSetup) return;

        //###########################################
        //# Command: /test
        //###########################################
        cmdData.add(Commands.slash("test", "Test Cmd von Eposs"));


        //###########################################
        //# Standard OptionData für msg und channel
        //###########################################
        var msg = new OptionData(OptionType.STRING, "message", "The message the bot will say.", true);
        var channel = new OptionData(OptionType.CHANNEL, "channel", "The channel you want to send this message in", false)
                .setChannelTypes(ChannelType.TEXT, ChannelType.NEWS, ChannelType.VOICE);


        //###########################################
        //# Command: /sudo <user> <cmd>
        //###########################################
        var sudoUser = new OptionData(OptionType.USER, "user", "User, der den Cmd ausführt.", true);
        var sudoCmd = new OptionData(OptionType.STRING, "cmd", "Cmd, der ausgeführt wird.", true, true);
        var sudoData = new OptionData(OptionType.STRING, "data", "Optional Cmd Data", false, false);

        cmdData.add(Commands.slash("sudo", "Führt einen Befehl als ein anderer User aus.")
                .addOptions(sudoUser, sudoCmd, sudoData)
                .setDefaultPermissions(DefaultMemberPermissions.DISABLED)
        );


        //###########################################
        //# Command: /link <ign>
        //###########################################
        cmdData.add(Commands.slash("link", "Verknüpft deinen Discord Account mit deinem Minecraft Account.")
                .addOption(OptionType.STRING, "ign", "Dein Minecraft Ingame Name", true)
        );


        //###########################################
        //# Command: /ping
        //###########################################
        cmdData.add(Commands.slash("ping", "Zeigt den Ping des Bots"));

        //###########################################
        //# Command: /send + subcmds
        //###########################################

        // Command: /send message <message> [channel]
        var sendMessage = new SubcommandData("message", "Send a Message with the Bot.")
                .addOptions(msg, channel);

        // Command: /send embed <message> [color] [title] [channel]
        var sendEmbedTitle = new OptionData(OptionType.STRING, "title", "Embed title", false);
        var sendEmbedColor = new OptionData(OptionType.STRING, "color", "Embed color (default: black)", false, true);
        var sendEmbed = new SubcommandData("embed", "Send an embed Message with the bot.")
                .addOptions(msg, sendEmbedColor, sendEmbedTitle, channel);

        // Command: /send infochannel
        var sendInfochannel = new SubcommandData("infochannel", "Sendet alle Info Channel Nachrichten.");

        // Command: /send regeln
        var sendRegeln = new SubcommandData("regeln", "Sendet das Regelwerk.");

        // Command: /send nützliches
        var sendNuetzliches = new SubcommandData("nützliches", "Sendet die (Nützliches) Nachricht");

        // Command /send teamregeln
        var sendTeamRegeln = new SubcommandData("teamregeln", "Sendet die Team Regeln.");

        // Command /send supportregeln
        var sendSupportRegeln = new SubcommandData("supportregeln", "Sendet die Support Regeln");

        cmdData.add(Commands.slash("send", "send Cmd")
                .addSubcommands(sendMessage, sendEmbed, sendInfochannel, sendRegeln, sendNuetzliches, sendTeamRegeln, sendSupportRegeln)
                .setDefaultPermissions(DefaultMemberPermissions.DISABLED)
        );


        //###########################################
        //# Command: /setup (in current channel) + subcmds
        //###########################################
        var verify = new SubcommandData("verify", "Verify System setup (in current channel)");
        var classRoles = new SubcommandData("classroles", "Class Roles Setup (in current channel)");
        var guildRoles = new SubcommandData("guildroles", "Guild Roles Setup (in current channel)");
        var pingRoles = new SubcommandData("pingroles", "Ping Roles Setup (in current channel)");
        var spenden = new SubcommandData("spenden", "Spenden System Setup (in current channel)");
        var gameMode = new SubcommandData("gamemode", "GameMode System Setup (in current channel)");

        cmdData.add(Commands.slash("setup", "Setup for all systems  (in current channel)")
                .addSubcommands(verify, classRoles, guildRoles, pingRoles, spenden, gameMode)
                .setDefaultPermissions(DefaultMemberPermissions.DISABLED)
        );


        //###########################################
        //# Command: /sbprofile <ign>
        //###########################################
        /*var ignOption = new OptionData(OptionType.STRING, "ign", "Dein Minecraft ingame Name", true);
        cmdData.add(Commands.slash("sbprofile", "Aktualisiert deine Skyblock Level und Cata Level Rolle.")
                .addOptions(ignOption)
        );*/


        //###########################################
        //# Command: /removeroles
        //###########################################
        // cmdData.add(Commands.slash("removeroles", "Entfernt deine Skyblock Level und Cata Level Rolle."));


        //###########################################
        //# Command: /blacklist + subcmds
        //###########################################
        var blIgnAdd = new OptionData(OptionType.STRING, "ign", "Ingame Name, der gesperrt werden soll.", true);
        var blIgnRemove = new OptionData(OptionType.STRING, "ign", "Ingame Name, der entsperrt werden soll.", true);

        var blAdd = new SubcommandData("add", "Sperrt einen MC Account für die Verifizierung.").addOptions(blIgnAdd);
        var blRemove = new SubcommandData("remove", "Entsperrt einen MC Account für die Verifizierung.").addOptions(blIgnRemove);
        var blList = new SubcommandData("list", "Liste aller Accounts auf der Blacklist");

        cmdData.add(Commands.slash("blacklist", "Blacklist Cmd")
                .addSubcommands(blAdd, blRemove, blList)
        );
        cmdData.add(Commands.slash("bl", "Blacklist Cmd")
                .addSubcommands(blAdd, blRemove, blList)
        );

        //###########################################
        //# Command: /counter
        //###########################################
        cmdData.add(Commands.slash("counter", "Zeigt alle Counter an."));


        //###########################################
        //# Command: /ticket + subcmds (in current  channel)
        //###########################################
        var tClose = new SubcommandData("close", "Schließt das aktuelle Ticket");
        var tDelete = new SubcommandData("delete", "Löscht das aktuelle Ticket ohne Transcript");

        cmdData.add(Commands.slash("ticket", "Spenden Ticket Cmd")
                .addSubcommands(tClose, tDelete)
                .setDefaultPermissions(DefaultMemberPermissions.DISABLED)
        );


        //###########################################
        //# Command: /sbroles [ add | remove ] [ catacombs | level | all]
        //###########################################
        var sbrOption = new OptionData(OptionType.STRING, "roles", "Möglichkeiten: 'Catacombs' | 'SbLevel' | 'all'", true, true);

        var sbrAdd = new SubcommandData("add", "Fügt entsprechende Skyblock Rollen hinzu.").addOptions(sbrOption);
        var sbrRemove = new SubcommandData("remove", "Entfernt entsprechende Skyblock Rollen.").addOptions(sbrOption);

        cmdData.add(Commands.slash("sbroles", "Skyblock Rollen Cmd")
                .addSubcommands(sbrAdd, sbrRemove)
        );
        cmdData.add(Commands.slash("roles", "Skyblock Rollen Cmd")
                .addSubcommands(sbrAdd, sbrRemove)
        );
        cmdData.add(Commands.slash("rollen", "Skyblock Rollen Cmd")
                .addSubcommands(sbrAdd, sbrRemove)
        );
        cmdData.add(Commands.slash("sbrollen", "Skyblock Rollen Cmd")
                .addSubcommands(sbrAdd, sbrRemove)
        );


        //###########################################
        //# Command: /spende
        //#             list
        //#             set / add / remove <user> <betrag>
        //###########################################
        var userSpende = new OptionData(OptionType.USER, "spender", "User, der gespendet hat.", true);
        var spendenBetrag = new OptionData(OptionType.STRING, "betrag", "Spendenbetrag", true);

        var spendeList = new SubcommandData("list", "Listet alle Spender auf.");
        var spendeAdd = new SubcommandData("add", "Addiert einen Betrag zu einer Spende eines Users").addOptions(userSpende, spendenBetrag);
        var spendeRemove = new SubcommandData("remove", "Subtrahiert einen Betrag von einer Spende eines Users").addOptions(userSpende, spendenBetrag);
        var spendeSet = new SubcommandData("set", "Setzt einen Spendenbetrag eines Users").addOptions(userSpende, spendenBetrag);

        cmdData.add(Commands.slash("spende", "Fügt den Spenden-Betrag in der Datenbank hinzu.")
                .addSubcommands(spendeList, spendeAdd, spendeRemove, spendeSet)
                .setDefaultPermissions(DefaultMemberPermissions.DISABLED)
        );


        //###########################################
        //# Command: /giveaway create <channel> <description> <winner> <time> <Req> true/false>
        //#                     reroll <msgID> <user>
        //###########################################
        var gwChannel = new OptionData(OptionType.CHANNEL, "channel", "Giveaway Channel", true, false).setChannelTypes(ChannelType.TEXT);
        var gwDes = new OptionData(OptionType.STRING, "des", "Giveaway Beschreibung", true, false);
        var gwWinner = new OptionData(OptionType.INTEGER, "winner", "Giveaway Gewinner Anzahl", true, false);
        var gwZeit = new OptionData(OptionType.STRING, "time", "Giveaway Länge", true, false);
        var gwReq = new OptionData(OptionType.BOOLEAN, "req", "Giveaway hat Rollen Requirements", true, false);
        var gwMsgId = new OptionData(OptionType.STRING, "msgid", "Giveaway Message ID", true, false);

        var gwCreate = new SubcommandData("create", "Giveaway erstellen").addOptions(gwChannel, gwDes, gwWinner, gwZeit, gwReq);

        var gwUser = new OptionData(OptionType.USER, "user", "Giveaway User to reroll", false, false);

        var gwReroll = new SubcommandData("reroll", "Giveaway Gewinner rerollen").addOptions(gwMsgId, gwUser);

        cmdData.add(Commands.slash("giveaway", "Giveaway management")
                .addSubcommands(gwCreate, gwReroll)
                .setDefaultPermissions(DefaultMemberPermissions.DISABLED)
        );


        //###########################################
        //# Command: /apitest
        //###########################################
        cmdData.add(Commands.slash("apitest", "Tests Hypixel Skyblock API response."));


        // Event Auswertung
        //cmdData.add(Commands.slash("eventauswertung", "Serverbild Event Auswertung").setDefaultPermissions(DefaultMemberPermissions.DISABLED));


        //###########################################
        //# Command: /search <phrase>
        //###########################################
        var searchOpt = new OptionData(OptionType.STRING, "phrase", "Phrase nach der gesucht wird.", true);
        cmdData.add(Commands.slash("search", "Sucht im aktuellen Kanal nach der angegebenen Phrase.")
                .addOptions(searchOpt)
        );


        //###########################################
        //# Command: /infos <user>
        //###########################################
        var infoOpt = new OptionData(OptionType.USER, "user", "User, über den du Infos haben möchtest.", true);
        cmdData.add(Commands.slash("infos", "Zeigt alle Infos über einen User an.")
                .addOptions(infoOpt)
        );


        //###########################################
        //# Command: /stats
        //###########################################
        cmdData.add(Commands.slash("stats", "Zeigt Stats über den Bot"));


        //###########################################
        //# Command: /help
        //###########################################
        cmdData.add(Commands.slash("help", "Übersicht über alle Commands."));


        //###########################################
        //# Context Menu User
        //###########################################
        cmdData.add(Commands.context(Command.Type.USER, "Skyblock Links"));

        cmdData.add(Commands.context(Command.Type.USER, "Get user overview"));

        cmdData.add(Commands.context(Command.Type.USER, "Verify reminder").setDefaultPermissions(DefaultMemberPermissions.DISABLED));


        //###########################################
        //# Context Menu Message
        //###########################################
        cmdData.add(Commands.context(Command.Type.MESSAGE, "Get message formatted"));

        cmdData.add(Commands.context(Command.Type.MESSAGE, "Get embed content"));

        //cmdData.add(Commands.context(Command.Type.MESSAGE, "Edit Embed").setDefaultPermissions(DefaultMemberPermissions.DISABLED));

        System.out.println("DkBot cmdSetup");
        cmdSetup = true;
    }

    @Override
    public void onGuildReady(@NotNull GuildReadyEvent event) {
        setUpCommands();
        //###########################################
        //# Update Commands
        //###########################################
        // hinter jeden Cmd .setDefaultPermissions(DefaultMemberPermissions.DISABLED) --> nur Admins können den Cmd nutzen
        try {
            event.getGuild().updateCommands().addCommands(cmdData).queue();
            System.out.println("DkBot guildready " + event.getGuild().getName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}