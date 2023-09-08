package dev.darkknights.dkbot.cmds;

import dev.darkknights.WrongGuildException;
import dev.darkknights.config.Configs;
import dev.darkknights.dkbot.DarkKnightsBot;
import dev.darkknights.dkbot.cmds.slash.*;
import dev.darkknights.dkbot.management.APIRequests;
import dev.darkknights.dkbot.management.VerifyBlacklist;
import dev.darkknights.dkbot.systems.GiveawaySystem;
import dev.darkknights.dkbot.systems.ServerbildEventSystem;
import dev.darkknights.dkbot.systems.SpendenSystem;
import dev.darkknights.dkbot.systems.VerificationSystem;
import dev.darkknights.dkbot.systems.selfroles.*;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.json.simple.parser.ParseException;

import java.awt.*;
import java.io.IOException;

import static dev.darkknights.Bots.test;

public class SlashCmd extends ListenerAdapter {

    //###########################################
    //# Msg in command-log bei jedem genutzten cmd
    //###########################################
    private void cmdLog(SlashCommandInteractionEvent event) throws WrongGuildException {
        var cmd = event.getName();
        var fullCmdName = event.getFullCommandName();
        var cmdId = event.getCommandId();

        var userMention = event.getUser().getAsMention();
        var channelMention = event.getChannel().getAsMention();

        test(event, cmd);
        if (!Configs.gildenIDs.getDkGuild().equals(event.getGuild())) return;

        var eb = new EmbedBuilder()
                .setFooter(Configs.devs.eposs.cmdDevText(), Configs.devs.eposs.icon())
                .setColor(Color.RED)
                .setTitle("Command Log");
        eb.setDescription(userMention + " hat </" + fullCmdName + ":" + cmdId + "> in " + channelMention + " genutzt.");

        event.getGuild().getTextChannelById(Configs.channelIDs.logs.cmdLogID).sendMessageEmbeds(eb.build()).queue();
    }


    //###########################################
    //# Slash Commands
    //#
    /*
    //check ob user in team
                    for (Role role : event.getMember().getRoles()) {
        if (Configs.rollenIDs.general.teamCmdRoleList.contains(role.getIdLong())) {
            //Befehl
            return;
        }
    }
                    event.getHook().setEphemeral(true).sendMessage("Dieser Befehl ist nur für Mods und höher nutzbar.").queue();
     */
    //###########################################

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        String cmd = event.getName();
        String subCmd = event.getSubcommandName();

        try {
            switch (cmd) {
                case "test" -> {
                    event.deferReply(true).queue();
                    cmdLog(event);

                    event.getHook().sendMessage("https://cdn.discordapp.com/attachments/1069924738798981140/1093198842054332596/Bildschirmfoto_2023-04-05_um_17.41.29.png").queue();
                }
                case "link" -> {
                    event.deferReply(true).queue();
                    cmdLog(event);
                    new CmdUseCount().cmdUse(cmd);

                    var member = event.getMember();
                    var ign = event.getOption("ign").getAsString();
                    var hook = event.getHook();

                    new link().cmd(member, ign, hook);
                }
                case "send" -> {
                    event.deferReply().setEphemeral(true).queue();
                    cmdLog(event);
                    new CmdUseCount().cmdUse(cmd);

                    switch (subCmd) {
                        case "message" -> new send().message(event);
                        case "embed" -> new send().embed(event);
                        case "infochannel" -> new send().infochannel(event);
                        case "regeln" -> new send().regeln(event);
                        case "nützliches" -> new send().nuetzliches(event);
                        case "teamregeln" -> new send().teamregeln(event);
                        case "supportregeln" -> new send().supportregeln(event);
                    }
                }
                case "counter" -> {
                    event.deferReply().setEphemeral(false).queue();
                    cmdLog(event);
                    new CmdUseCount().cmdUse(cmd);

                    new counter().counterCmd(event);
                }
                case "blacklist", "bl" -> {
                    event.deferReply().queue();
                    cmdLog(event);
                    new CmdUseCount().cmdUse("blacklist");

                    // check ob user in team
                    for (Role role : event.getMember().getRoles()) {
                        if (Configs.rollenIDs.general.teamCmdRoleList.contains(role.getIdLong())) {
                            //Befehl
                            switch (subCmd) {
                                case "add" -> new VerifyBlacklist().cmdAddToBlacklist(event);
                                case "remove" -> new VerifyBlacklist().cmdRemoveFromBlacklist(event);
                                case "list" -> new VerifyBlacklist().cmdBlList(event);
                            }
                            return;
                        }
                    }

                    event.getHook().setEphemeral(true).sendMessage("Dieser Befehl ist nur für Mods und höher nutzbar.").queue();
                }
                case "setup" -> {
                    event.deferReply().setEphemeral(true).queue();
                    cmdLog(event);
                    new CmdUseCount().cmdUse(cmd);

                    switch (subCmd) {
                        case "verify" -> new VerificationSystem().verifySetupCmd(event);
                        case "classroles" -> new ClassRoleSystem().classRoleSetupCmd(event);
                        case "guildroles" -> new GildenRoleSystem().gildenRoleSetupCmd(event);
                        case "pingroles" -> new PingRoleSystem().pingRoleSetupCmd(event);
                        case "spenden" -> new SpendenSystem().spendenSetupCmd(event);
                        case "gamemode" -> new GameModeRoleSystem().gameModeRoleSetupCmd(event);
                    }
                }
                case "ticket" -> {
                    event.deferReply().setEphemeral(true).queue();
                    cmdLog(event);
                    new CmdUseCount().cmdUse(cmd);

                    new ticket().cmd(event);
                }
                case "sbroles", "roles", "sbrollen", "rollen" -> {
                    event.deferReply(true).queue();
                    cmdLog(event);
                    new CmdUseCount().cmdUse("sbroles");

                    var roleOption = event.getOption("roles").getAsString().toLowerCase();
                    var support = event.getGuild().getTextChannelById(Configs.channelIDs.supportTicketID);
                    var user = event.getUser();
                    var dk = event.getGuild();
                    var hook = event.getHook();

                    switch (subCmd) {
                        //type: 1 == add | 0 == remove
                        case "add" -> new SbRoleSystem().sbRolesCmd(roleOption, support, user, dk, hook, 1);
                        case "remove" -> new SbRoleSystem().sbRolesCmd(roleOption, support, user, dk, hook, 0);
                    }
                }
                case "apitest" -> {
                    event.deferReply(true).queue();
                    cmdLog(event);

                    //check ob user in team
                    for (Role role : event.getMember().getRoles()) {
                        if (Configs.rollenIDs.general.teamCmdRoleList.contains(role.getIdLong())) {
                            //Befehl

                            new APIRequests().apiTest();

                            event.getHook().sendMessage("Done... (siehe <#1066502364477005824> )").queue();

                            return;
                        }
                    }
                    event.getHook().setEphemeral(true).sendMessage("Dieser Befehl ist nur für Mods und höher nutzbar.").queue();
                }
                case "eventauswertung" -> {
                    event.deferReply(true).queue();
                    cmdLog(event);

                    new ServerbildEventSystem().cmdEventAuswertung(event);
                }
                case "spende" -> {
                    event.deferReply(false).queue();
                    cmdLog(event);
                    new CmdUseCount().cmdUse(cmd);

                    new spende().cmd(event);
                }
                case "stats" -> {
                    event.deferReply(false).queue();
                    cmdLog(event);
                    new CmdUseCount().cmdUse(cmd);

                    new CmdUseCount().statsCmd(event);
                }
                case "ping" -> {
                    event.deferReply(false).queue();
                    cmdLog(event);
                    new CmdUseCount().cmdUse(cmd);

                    var jda = DarkKnightsBot.getJDA();
                    var gatePing = jda.getGatewayPing();
                    var restPing = jda.getRestPing().complete();

                    var eb = new EmbedBuilder()
                            .setFooter(Configs.devs.eposs.cmdDevText(), Configs.devs.eposs.icon())
                            .setColor(Color.GREEN)
                            .setTitle("Pong!")
                            .setDescription(
                                    "Bot: `" + restPing + "ms`" +
                                            "\nDiscord API: `" + gatePing + "ms`");

                    event.getHook().sendMessageEmbeds(eb.build()).queue();
                }
                case "sudo" -> {
                    event.deferReply(true).queue();
                    cmdLog(event);

                    if (event.getUser().getIdLong() == Configs.userIDs.epossID) new sudo().cmd(event);
                    else event.getHook().sendMessage("Dieser Befehl ist nicht verfügbar.").queue();
                }
                case "giveaway" -> {
                    event.deferReply(true).queue();
                    cmdLog(event);
                    new CmdUseCount().cmdUse(cmd);

                    switch (subCmd) {
                        case "create" -> new GiveawaySystem().gwCreateCmd(event);
                        case "reroll" -> new GiveawaySystem().gwRerollCmd(event);
                    }
                }
                case "search" -> {
                    event.deferReply(true).queue();
                    cmdLog(event);
                    new CmdUseCount().cmdUse(cmd);

                    new search().searchCmd(event);
                }
                case "infos" -> {
                    event.deferReply(true).queue();
                    cmdLog(event);

                    //check ob user in team
                    for (Role role : event.getMember().getRoles()) {
                        if (Configs.rollenIDs.general.teamCmdRoleList.contains(role.getIdLong())) {
                            //Befehl

                            new infos().infosCmd(event);

                            return;
                        }
                    }
                    event.getHook().setEphemeral(true).sendMessage("Dieser Befehl ist nur für Mods und höher nutzbar.").queue();
                }
                case "help" -> {
                    event.deferReply().setEphemeral(false).queue();
                    cmdLog(event);
                    new CmdUseCount().cmdUse(cmd);

                    var eb = new EmbedBuilder()
                            .setFooter(Configs.devs.eposs.cmdDevText(), Configs.devs.eposs.icon())
                            .setColor(Color.YELLOW)
                            .setTitle("DarkKnight Bot Command Liste");
                    eb.addField("Help Command",
                            "- </help:1129945244704718907> Zeigt diese Nachricht an."
                            , false);
                    eb.addField("Skyblock Commands",
                            """
                                    - </sbroles add:1129945244704718899> Vergibt entsprechende Skyblock Profile Rollen.
                                    (Alias: /roles, /sbrollen, /rollen)
                                    - </link:1129945244176232490> Verknüpfe deinen Minecraft Account mit deinem Discord Account.
                                    """
                            , false);
                    eb.addField("Misc",
                            """
                                    - </ping:1129945244176232491> Zeigt den Ping des Bots und der Discord API.
                                    - </counter:1129945244176232496> Zeigt alle Counter an.
                                    - </stats:1129945244704718905> Zeigt Stats über den Bot.
                                    - </search:1142048751020675193> Sucht nach einer Phrase im aktuellen Kanal.
                                    """
                            , false);
                    eb.addField("Infos", """
                                    ToS / Privacy Policy:
                                    https://github.com/DarkKnights-Development/info#readme
                                                                
                                    Bot entwickelt von:
                                    <@502875153378705408> | <@691643342106263602>
                                     """
                            , false);

                    event.getHook().sendMessageEmbeds(eb.build()).queue();
                }
            }
        } catch (WrongGuildException e) {
            e.setLocation("dev.darkknights.cmds.SlashCmd.onSlashCommandInteraction");
            event.getHook().setEphemeral(true).sendMessage("Dieser Cmd funktioniert nur auf dem DarkKnights Discord Server.").queue();
        } catch (Exception e) {
            e.printStackTrace();
            event.getHook().setEphemeral(true).sendMessage("Es gab einen Fehler beim Ausführen des Befehls.").queue();
        }
        // event.getHook().setEphemeral(true).sendMessage("Der Bot ist noch in der Entwicklung und nicht dauerhaft online!").queue();
    }

    @Deprecated
    private void cmdLog(SlashCommandInteractionEvent event, String cmd) throws WrongGuildException, IOException, ParseException {
        test(event, cmd);

        if (!Configs.gildenIDs.getDkGuild().equals(event.getGuild())) return;

        String cmdUser = event.getUser().getAsMention();

        EmbedBuilder eb = new EmbedBuilder();
        eb.setFooter(Configs.devs.eposs.cmdDevText(), Configs.devs.eposs.icon());
        eb.setColor(Color.RED);
        eb.setTitle("Command Log");
        eb.setDescription(cmdUser + " hat `/" + cmd + "` in " + event.getChannel().getAsMention() + " genutzt.");

        event.getGuild().getTextChannelById(Configs.channelIDs.logs.cmdLogID).sendMessageEmbeds(eb.build()).queue();
    }

    @Deprecated
    private void cmdLog(SlashCommandInteractionEvent event, String cmd, String subCmd) throws WrongGuildException, IOException, ParseException {
        test(event, cmd);

        if (!Configs.gildenIDs.getDkGuild().equals(event.getGuild())) return;

        String cmdUser = event.getUser().getAsMention();

        EmbedBuilder eb = new EmbedBuilder();
        eb.setFooter(Configs.devs.eposs.cmdDevText(), Configs.devs.eposs.icon());
        eb.setColor(Color.RED);
        eb.setTitle("Command Log");
        eb.setDescription(cmdUser + " hat `/" + cmd + " " + subCmd + "` in " + event.getChannel().getAsMention() + " genutzt.");

        event.getGuild().getTextChannelById(Configs.channelIDs.logs.cmdLogID).sendMessageEmbeds(eb.build()).queue();
    }
}
