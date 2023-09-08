package dev.darkknights.dkbot.cmds;

import dev.darkknights.WrongGuildException;
import dev.darkknights.config.Configs;
import dev.darkknights.dkbot.management.database.MemberDataBase;
import dev.darkknights.dkbot.management.memberdata.ServerMember;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.MessageType;
import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.*;

public class ContextMenus extends ListenerAdapter {

    //###########################################
    //# Msg in command-log bei jedem genutzten cmd
    //###########################################
    private void cmdLog(UserContextInteractionEvent event, String cmd) throws WrongGuildException {
        if (!Configs.gildenIDs.getDkGuild().equals(event.getGuild())) return;

        var cmdUser = event.getUser().getAsMention();

        var eb = new EmbedBuilder()
                .setFooter(Configs.devs.eposs.cmdDevText(), Configs.devs.eposs.icon())
                .setColor(Color.RED)
                .setTitle("Command Log")
                .setDescription(cmdUser + " hat `" + cmd + "` genutzt.\n" +
                        "Target: " + event.getTargetMember().getAsMention());

        event.getGuild().getTextChannelById(Configs.channelIDs.logs.cmdLogID).sendMessageEmbeds(eb.build()).queue();
    }

    private void cmdLog(MessageContextInteractionEvent event, String cmd) throws WrongGuildException {
        if (!Configs.gildenIDs.getDkGuild().equals(event.getGuild())) return;

        var cmdUser = event.getUser().getAsMention();

        var eb = new EmbedBuilder()
                .setFooter(Configs.devs.eposs.cmdDevText(), Configs.devs.eposs.icon())
                .setColor(Color.RED)
                .setTitle("Command Log")
                .setDescription(cmdUser + " hat `" + cmd + "` in " + event.getTarget().getChannel().getAsMention() +
                        " an der Nachricht" + event.getTarget().getJumpUrl() + "genutzt.");

        event.getGuild().getTextChannelById(Configs.channelIDs.logs.cmdLogID).sendMessageEmbeds(eb.build()).queue();
    }


    //###########################################
    //# User Context Menus ("Apps")
    //###########################################
    @Override
    public void onUserContextInteraction(UserContextInteractionEvent event) {
        var cmd = event.getName();

        try {
            switch (cmd) {
                case "Skyblock Links" -> {
                    event.deferReply(true).queue();
                    cmdLog(event, cmd);

                    var target = event.getTarget();
                    var targetID = target.getId();

                    var eb = new EmbedBuilder()
                            .setColor(Color.GREEN)
                            .setFooter(Configs.devs.eposs.cmdDevText(), Configs.devs.eposs.icon())
                            .setTitle("Skyblock Links");

                    var mdb = new MemberDataBase();

                    if (mdb.hasData(targetID)) {
                        var sm = new ServerMember(mdb.getFromDatabase(targetID));
                        var ign = sm.getMinecraftUsername();
                        var UUID = sm.getMinecraftUUID();

                        eb.addField("Discord", target.getAsMention() + "\n" + target.getName() + "\n" + targetID, true)
                                .addField("Minecraft",
                                        "Ign: " + ign + "\nUUID: " + UUID
                                        , true)
                                .addField("Links",
                                        "[Skycrypt](https://sky.shiiyu.moe/stats/" + ign + ")" +
                                                "\n[Plancke](https://plancke.io/hypixel/player/stats/" + ign + ")" +
                                                "\n[Auction History](https://sky.coflnet.com/player/" + UUID + ")"
                                        , true);
                    } else {
                        eb.setDescription("Für diesen User konnten keine Daten gefunden werden." +
                                "\nDiese App funktioniert nur, wenn der User bereits </link:1113182819393478657> genutzt hat.");
                    }

                    event.getHook().sendMessageEmbeds(eb.build()).queue();
                }
                case "Get user overview" -> {
                    event.deferReply().setEphemeral(true).queue();
                    cmdLog(event, cmd);

                    var user = event.getUser();
                    var target = event.getTarget();

                    var flagString = new StringBuilder();

                    var flags = target.getFlags();
                    flags.forEach(flag -> flagString.append(flag.getName()).append("\n"));

                    var created = target.getTimeCreated().toEpochSecond();

                    var eb = new EmbedBuilder()
                            .setFooter(Configs.devs.eposs.cmdDevText(), Configs.devs.eposs.icon())
                            .setColor(Color.CYAN)
                            .setImage(target.getAvatarUrl())
                            .addField("Mention", target.getAsMention(), true)
                            .addField("Username", "@" + target.getName(), true)
                            .addField("Global Display Name", target.getEffectiveName(), true)
                            .addField("ID", target.getId(), true)
                            .addField("Time Created", "<t:" + created + ":F> | <t:" + created + ":R>", false)
                            .addField("Flags", String.valueOf(flagString), false);
                    if (target.isBot()) eb.setTitle("🤖 Bot Overview");
                    else eb.setTitle("👤 User Overview");

                    user.openPrivateChannel().flatMap(privateChannel -> privateChannel.sendMessageEmbeds(eb.build())).queue(
                            success -> event.getHook().sendMessage("User Overview sent to DMs.").queue(),
                            failure -> event.getHook().sendMessage("DM konnte nicht gesendet werden, bitte erlaube DMs von diesem Server.").queue()
                    );
                }
                case "Verify reminder" -> {
                    event.deferReply(true).queue();
                    cmdLog(event, cmd);

                    var target = event.getTarget();
                    var targetM = event.getTargetMember();

                    if (!targetM.getRoles().contains(event.getGuild().getRoleById(Configs.rollenIDs.general.memberRole))) {
                        var msg = """
                                Willkommen auf dem DarkKnights Discord Server.
                                Diese Nachricht wurde dir geschickt, da du dich noch nicht verifiziert hast.
                                Bitte verifiziere dich in <#1075859541138354246> mit deinem Minecraft Ingame Namen.
                                                            
                                Falls du Fragen zur Verifizierung hast, öffne gern ein Ticket in <#1067421699726901299> oder schreibe <@502875153378705408> (`<Eposs>#0042`) eine Direktnachricht.
                                                            
                                LG Das DarkKnights Team.
                                """;

                        target.openPrivateChannel().flatMap(privateChannel -> privateChannel.sendMessage(msg)).queue(
                                success -> event.getHook().sendMessage("Der User " + target.getAsMention() + " wurde per DM an die Verifizierung erinnert.").queue(),
                                failure -> event.getHook().sendMessage("Der User " + target.getAsMention() + " hat den Bot blockiert oder seine Dms geschlossen. Es konnte keine Erinnerung geschickt werden.").queue()
                        );
                    } else {
                        event.getHook().sendMessage("Dieser User ist bereits verifiziert.").queue();
                    }
                }
            }
        } catch (WrongGuildException e) {
            e.setLocation("dev.darkknights.cmds.ContextMenus.onUserContextInteraction");
            event.getHook().setEphemeral(true).sendMessage("Diese App funktioniert nur auf dem DarkKnights Discord Server.").queue();
        } catch (Exception e) {
            e.printStackTrace();
            event.getHook().setEphemeral(true).sendMessage("Es gab einen Fehler beim Ausführen des Befehls.").queue();
        }
    }


    //###########################################
    //# Message Context Menus ("Apps")
    //###########################################
    @Override
    public void onMessageContextInteraction(MessageContextInteractionEvent event) {
        var cmd = event.getName();

        try {
            switch (cmd) {
                case "Get message formatted" -> {
                    event.deferReply().setEphemeral(true).queue();
                    cmdLog(event, cmd);

                    var user = event.getUser();
                    var msg = event.getTarget();

                    if (msg.getType().equals(MessageType.DEFAULT)) {
                        user.openPrivateChannel().flatMap(privateChannel -> privateChannel.sendMessage("``` " + msg.getContentRaw() + " ```\n" + msg.getJumpUrl())).queue(
                                success -> event.getHook().sendMessage("Formatted message content sent to DMs " +
                                        "\nEmbed Nachrichten funktionieren damit nicht.").queue(),
                                failure -> event.getHook().sendMessage("DM konnte nicht gesendet werden, bitte erlaube DMs von diesem Server.").queue()
                        );
                    } else {
                        event.getHook().sendMessage("Message Type incompatible.").queue();
                    }
                }
                case "Get embed content" -> {
                    event.deferReply().setEphemeral(true).queue();
                    cmdLog(event, cmd);

                    var user = event.getUser();
                    var msg = event.getTarget();

                    var sb = new StringBuilder();

                    var embedList = msg.getEmbeds();
                    for (MessageEmbed eb : embedList) {
                        if (eb.getTitle() != null) {
                            sb.append("**Title:** ```").append(eb.getTitle()).append("```\n");
                        }
                        if (eb.getDescription() != null) {
                            sb.append("**Description:** ```").append(eb.getDescription()).append("```\n");
                        }

                        var ebFields = eb.getFields();
                        for (MessageEmbed.Field field : ebFields) {
                            sb.append("**Field Name:** ```").append(field.getName()).append("``` [inline:").append(field.isInline()).append("]\n");
                            sb.append("**Field Content:** ```").append(field.getValue()).append("```\n");
                        }
                    }

                    if (sb.length() > 1900) {
                        event.getHook().sendMessage("Embed message content to long.").queue();
                    } else {
                        user.openPrivateChannel().flatMap(privateChannel -> privateChannel.sendMessage("**__Get embed content app:__**\n" + msg.getJumpUrl() + "\n\n" + sb)).queue(
                                success -> event.getHook().sendMessage("Embed message content sent to DMs").queue(),
                                failure -> event.getHook().sendMessage("DM konnte nicht gesendet werden, bitte erlaube DMs von diesem Server.").queue()
                        );
                    }
                }
                case "Edit Embed" -> {
                    event.deferReply(true).queue();
                    cmdLog(event, cmd);

                    if (event.getMember().getIdLong() == Configs.devs.eposs.discordID()) {
                        var msg = event.getTarget();

                        var embeds = msg.getEmbeds();
                        if (embeds.size() > 0) {
                            var embed = embeds.get(0);
                            var eb = new EmbedBuilder(embed)
                                    .setFooter(Configs.devs.eposs.cmdDevText(), Configs.devs.eposs.icon());
                            msg.editMessageEmbeds(eb.build()).queue();
                            event.getHook().sendMessage("done").queue();
                        } else {
                            event.getHook().sendMessage("Diese Nachricht hat keine Embeds.").queue();
                        }
                    } else {
                        event.getHook().sendMessage("Nur @eposs kann diese App nutzen").queue();
                    }
                }
            }
        } catch (WrongGuildException e) {
            e.setLocation("dev.darkknights.cmds.ContextMenus.onMessageContextInteraction");
            event.getHook().setEphemeral(true).sendMessage("Diese App funktioniert nur auf dem DarkKnights Discord Server.").queue();
        } catch (NullPointerException e) {
            event.getHook().setEphemeral(true).sendMessage("Es gab einen Fehler beim Ausführen des Befehls.").queue();
        }
    }
}
