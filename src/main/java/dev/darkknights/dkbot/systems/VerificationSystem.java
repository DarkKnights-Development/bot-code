package dev.darkknights.dkbot.systems;

import de.transcript.Transcript;
import dev.darkknights.Logging;
import dev.darkknights.WrongGuildException;
import dev.darkknights.config.Configs;
import dev.darkknights.dkbot.management.APIRequests;
import dev.darkknights.dkbot.management.HypixelApiManagement;
import dev.darkknights.dkbot.management.VerifyBlacklist;
import dev.darkknights.dkbot.management.database.MemberDataBase;
import dev.darkknights.dkbot.management.memberdata.DataManagement;
import dev.darkknights.dkbot.systems.selfroles.SbRoleSystem;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import org.json.simple.parser.ParseException;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

public class VerificationSystem {
    private static final Logging log = new Logging();

    public void verifySetupCmd(SlashCommandInteractionEvent event) throws WrongGuildException {
        var channel = event.getChannel();

        // Support Ticket Channel
        var support = event.getGuild().getTextChannelById(Configs.channelIDs.supportTicketID);

        // Verify Msg
        var eb = new EmbedBuilder()
                .setColor(Color.GREEN)
                .setTitle("Verifizierung")
                .setDescription("Aus Sicherheitsgründen muss jeder User auf diesem Server seinen Discord Account auf hypixel eingetragen haben."
                        + "\nUm dich zu verifizieren, klicke unten auf den Button `verifizieren`"
                        + "\nHier wird dein Minecraft Name angefragt, um sicherzugehen, dass dein Discord Account mit deinem Minecraft Account auf hypixel verknüpft ist."
                        + "\n\nFalls du keinen Minecraft Account hast, aber dennoch gern verifiziert werden möchtest, musst du mit einem von unseren Admins kurz in einen Voice Chat. Wir überprüfen damit, dass niemand einen Ban umgehen kann. Klicke dazu unten auf den Button `kein Minecraft Account`."
                        + "\n\nBei Fragen oder Fehlern öffne ein Ticket in " + support.getAsMention() + ".")
                .setFooter(Configs.devs.eposs.cmdDevText(), Configs.devs.eposs.icon());

        channel.sendMessageEmbeds(eb.build())
                .addActionRow(Button.success("verify", "✅ verifizieren"), Button.secondary("verify-noMC", "kein Minecraft Account"))
                .queue();
        event.getHook().sendMessage("Done").queue();
    }


    public void verifyButton(ButtonInteractionEvent event) {
        var subject = TextInput.create("ign", "Minecraft Ingame Name", TextInputStyle.SHORT)
                .setPlaceholder("Gib deinen Minecraft Ingame Namen an.")
                .setMinLength(1)
                .setMaxLength(100) // or setRequiredRange(10, 100)
                .build();

        var modal = Modal.create("verify", "Verifizierung")
                .addComponents(ActionRow.of(subject))
                .build();

        event.replyModal(modal).queue();
    }


    public void noMCButton(ButtonInteractionEvent event) throws WrongGuildException {
        event.deferReply(true).queue();

        var dk = event.getGuild();
        var user = event.getUser();

        // Member perms im Ticket Channel
        Collection<Permission> perms = new ArrayList<>();
        perms.add(Permission.MESSAGE_SEND);
        perms.add(Permission.VIEW_CHANNEL);

        //Verify Ticket Kategorie
        var ticket = dk.createTextChannel("verify-" + user.getName(), dk.getCategoryById(1100316296345817178L)).
                syncPermissionOverrides().
                addMemberPermissionOverride(user.getIdLong(), perms, null).
                setTopic("Verify Ticket von " + user.getName()).complete();


        //Msg im Ticket Channel
        var eb = new EmbedBuilder()
                .setFooter(Configs.devs.eposs.cmdDevText(), Configs.devs.eposs.icon())
                .setColor(Color.CYAN)
                .setTitle("Verify Ticket")
                .setDescription("""
                        Willkommen bei der manuellen Verifizierung.
                        Sobald ein Admin Zeit hat, wird er sich hier bei dir melden.
                        Dann wird im Voice Chat überprüft, dass du nicht schon hier gebannt bist.
                                        
                        Wenn du von einem Freund eingeladen wurdest, schreibe bitte seinen Discord Namen (z.B. eposs) hier in den Chat.
                                        
                        **Erwähnen/Pingen von Admins schließt dich automatisch von einer Verifizierung aus.**
                        """
                );

        ticket.sendMessage("<@&" + Configs.rollenIDs.pingRollen.admin + "> | Ticket von " + user.getAsMention())
                .addEmbeds(eb.build())
                .addActionRow(Button.danger("verify-close", "Ticket schließen"))
                .queue();

        event.getHook().sendMessage("Dein Ticket für deine Verify Anfrage wurde erstellt ( " + ticket.getAsMention() + " )").queue();
    }


    public void verifyTicketCloseButton(ButtonInteractionEvent event) throws WrongGuildException {
        event.deferReply().setEphemeral(true).queue();

        event.editButton(event.getButton().asDisabled()).queue();

        var member = event.getMember();

        var roleList = member.getRoles();

        if (roleList.contains(event.getGuild().getRoleById(Configs.rollenIDs.general.admin))) {
            var channel = event.getChannel(); //Current Channel
            var logChannel = event.getGuild().getTextChannelById(Configs.channelIDs.logs.ticketLogID);

            var eb = new EmbedBuilder()
                    .setFooter(Configs.devs.eposs.cmdDevText(), Configs.devs.eposs.icon())
                    .setTitle("Verify-Ticket Log")
                    .setColor(Color.CYAN)
                    .setDescription("Das Verify Ticket `#" + channel.getName() + "` wurde gelöscht.");

            // https://github.com/GamingLPyt/JDA-HTML-Transcript
            var transcript = new Transcript();
            try {
                transcript.createTranscript(channel, logChannel);
            } catch (IOException e) {
                log.printErr("Verify Ticket Transcript");
                event.getHook().sendMessage("Transcript konnte nicht erstellt werden.").queue();
                event.editButton(event.getButton().asEnabled()).queue();
                return;
            }
            logChannel.sendMessageEmbeds(eb.build()).queue();
            event.getHook().sendMessage("Ticket wird gelöscht...").queue();
            channel.delete().queueAfter(5L, TimeUnit.SECONDS);
        } else {
            event.getHook().sendMessage("Du hast nicht die Berechtigung, das Ticket zu schließen.").queue();
            event.editButton(event.getButton().asEnabled()).queue();
        }
    }


    public void verifyModal(ModalInteractionEvent event) throws WrongGuildException, ParseException {
        event.deferReply().setEphemeral(true).queue();

        var dk = event.getGuild();
        var user = event.getUser();

        var eb = new EmbedBuilder()
                .setFooter(Configs.devs.eposs.cmdDevText(), Configs.devs.eposs.icon())
                .setTitle("Verify Log");

        var ign = event.getValue("ign").getAsString().toLowerCase();
        var UUID = new APIRequests().getUUID(ign);

        var support = dk.getTextChannelById(Configs.channelIDs.supportTicketID);
        var verifyLog = dk.getTextChannelById(Configs.channelIDs.logs.verifyLogID);

        boolean blacklistTrigger = false; // true wenn account auf blacklist

        // Ungültiger Mc Namen
        if (UUID.equals("Error UUID")) {
            eb.setColor(Color.YELLOW)
                    .setDescription(user.getAsMention() + " hat versucht, sich mit dem Account `" + ign + "` zu verifizieren. Dieser Account existiert nicht.");

            event.getHook().sendMessage("Der Minecraft Account `" + ign + "` existiert nicht." +
                    "\nVersuche es bitte erneut.").queue();

            verifyLog.sendMessageEmbeds(eb.build()).queue();
            return;
        }

        try {
            if (VerifyBlacklist.getBlacklist().contains(UUID)) {
                blacklistTrigger = true;

                eb.setColor(Color.RED)
                        .setDescription(user.getAsMention() + " hat versucht, sich mit dem Account `" + ign + "` zu verifizieren. Dieser Account ist auf der Blacklist.");

                event.getHook().sendMessage("Dein Minecraft Account `" + ign + "` ist gesperrt." +
                        "\nWenn du der Meinung bist, dass das ein Fehler ist, öffne ein Ticket in "
                        + support.getAsMention()).queue();
            } else {
                var hypApiMan = new HypixelApiManagement();

                String apiDc; //APIRequests.getDC(UUID);
                try {
                    apiDc = hypApiMan.getDiscord(UUID);
                } catch (NullPointerException e) {
                    verifyFail(eb, event.getHook(), user, ign, dk);
                    return;
                }

                var dc = user.getName();
                if (!user.getAsTag().contains("#0")) dc = user.getAsTag();

                if (dc.equals(apiDc)) {
                    var memberRole = dk.getRoleById(Configs.rollenIDs.general.memberRole);
                    var sbProfileRole = dk.getRoleById(Configs.rollenIDs.general.sbProfile);
                    var pingRole = dk.getRoleById(Configs.rollenIDs.general.pingRollen);

                    // Rollen geben
                    dk.addRoleToMember(user, memberRole).queue(
                            success -> {
                                // Willkommensnachricht in #general
                                var general = dk.getTextChannelById(Configs.channelIDs.generalChatID);
                                var memberCount = dk.getMembersWithRoles(memberRole).size();
                                memberCount++; // Discord ist wierd und die Anzahl wird nicht richtig erkannt... deshalb +1

                                var eb2 = new EmbedBuilder()
                                        .setColor(Color.green)
                                        .setFooter(Configs.devs.eposs.cmdDevText(), Configs.devs.eposs.icon())
                                        .setDescription("## Willkommen " + user.getAsMention() + "!" +
                                                "\nWir freuen uns, dass du uns gefunden hast!" +
                                                "\n*Du bist das " + memberCount + ". Mitglied* 🎉")
                                        .setThumbnail(user.getAvatarUrl());

                                general.sendMessageEmbeds(eb2.build()).queueAfter(1, TimeUnit.SECONDS);
                            });
                    event.getHook().sendMessage("Du bist nun verifiziert und hast Zugriff auf alle Kanäle!").setEphemeral(true).queue();

                    dk.addRoleToMember(user, sbProfileRole).queue();
                    dk.addRoleToMember(user, pingRole).queue();

                    // Discord Flags
                    var dataMan = new DataManagement();
                    var userID = user.getId();
                    if (new MemberDataBase().hasData(userID)) dataMan.restoreFlagState(event);
                    else dataMan.readAndStoreMemberData(event);

                    // Sb Rollen
                    var selectedProfile = hypApiMan.getSelectedProfile(UUID);

                    try {
                        var sbRs = new SbRoleSystem();
                        sbRs.addSbLvlRoles(user, dk, UUID, selectedProfile);
                        sbRs.addCataRoles(user, dk, UUID, selectedProfile);
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                        log.printErr("Verify: hatte eine der Sb Rollen nicht");
                    }

                    eb.setColor(Color.GREEN);
                    eb.setDescription(user.getAsMention() + " hat sich mit dem Account `" + ign + "` verifiziert.");
                } else {
                    verifyFail(eb, event.getHook(), user, ign, dk);
                    return;
                }
            }

            // Admin Ping wenn Account auf Blacklist
            if (blacklistTrigger) verifyLog.sendMessage("<@&1140685359441776640>").queue();

            verifyLog.sendMessageEmbeds(eb.build())
                    .addActionRow(Button.link("https://sky.shiiyu.moe/stats/" + ign, "SkyCrypt"), Button.link("https://plancke.io/hypixel/player/stats/" + ign, "Plancke"))
                    .queue();

        } catch (IOException e) {
            e.printStackTrace();
            event.getHook().sendMessage("Ein Fehler ist aufgetreten, bitte versuche es später nochmal.").queue();
        }
    }

    private void verifyFail(EmbedBuilder eb, InteractionHook hook, User user, String ign, Guild dk) {
        eb.setColor(Color.ORANGE);
        eb.setDescription(user.getAsMention() + " hat versucht sich mit dem Account `" + ign + "` zu verifizieren.");

        var verifyLog = dk.getTextChannelById(Configs.channelIDs.logs.verifyLogID);

        verifyLog.sendMessageEmbeds(eb.build())
                .addActionRow(Button.link("https://sky.shiiyu.moe/stats/" + ign, "SkyCrypt"), Button.link("https://plancke.io/hypixel/player/stats/" + ign, "Plancke"))
                .queue();

        var dc = user.getName();
        if (!user.getAsTag().contains("#0")) dc = user.getAsTag();
        hook.sendMessageEmbeds(Configs.dcNameErrorMsg(dc)).queue();
    }
}
