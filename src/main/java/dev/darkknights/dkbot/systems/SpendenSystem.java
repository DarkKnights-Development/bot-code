package dev.darkknights.dkbot.systems;

import de.transcript.Transcript;
import dev.darkknights.Logging;
import dev.darkknights.WrongGuildException;
import dev.darkknights.config.Configs;
import dev.darkknights.dkbot.management.database.MemberDataBase;
import dev.darkknights.dkbot.systems.selfroles.SbRoleSystem;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

public class SpendenSystem {

    public void spendeListCmd(SlashCommandInteractionEvent event) throws ParseException {
        var allData = new MemberDataBase().getAllData();
        var parser = new JSONParser();
        long sum = 0;

        var eb = new EmbedBuilder()
                .setFooter(Configs.devs.eposs.cmdDevText(), Configs.devs.eposs.icon())
                .setTitle("Giveaway Spenden")
                .setColor(Color.YELLOW);

        for (JSONObject json : allData) {
            try {
                var id = (long) json.get("discordID");
                var spender = event.getGuild().getMemberById(id);

                var list = (JSONArray) parser.parse(json.get("discordFlags").toString());
                var flags = (JSONObject) list.get(0);
                var betrag = (long) flags.get("spendenBetrag");

                var title = spender.getUser().getName();

                if (betrag != 0) {
                    eb.addField(title,
                            "Spendenbetrag: `" + betrag + "` [ " + betrag / 1000000 + "m | " + betrag / 1000000000 + "b ]"
                            , false);
                    sum = sum + betrag;
                }
            } catch (NullPointerException ignored) {}
        }

        eb.setDescription("Gesamter Spendenbetrag: `" + sum + "` [ " + sum / 1000000 + "m | " + sum / 1000000000 + "b ]");

        event.getHook().sendMessageEmbeds(eb.build()).queue();
    }


    public void spendenSetupCmd(SlashCommandInteractionEvent event) throws WrongGuildException {
        var channel = event.getChannel();


        // Giveaway Erklärung
        var eb = new EmbedBuilder()
                .setFooter(Configs.devs.eposs.cmdDevText(), Configs.devs.eposs.icon())
                .setTitle("Giveaway Spenden")
                .setColor(Color.YELLOW);
        eb.setDescription("Ihr könnt für unsere Giveaway Geld oder Items spenden." +
                "\nIhr könnt die Zeit des Giveaways und die Anzahl der Gewinner selbst bestimmen." +
                "\n\n### Folgende Rollen kann man nur über Spenden erhalten:" +
                "\n- <@&" + Configs.rollenIDs.spenden.bronze + "> – 10 Millionen Coins Spende\s" +
                "\n- <@&" + Configs.rollenIDs.spenden.silber + "> – 25 Millionen Coins Spende\s" +
                "\n- <@&" + Configs.rollenIDs.spenden.gold + "> – 50 Millionen Coins Spende\s" +
                "\n- <@&" + Configs.rollenIDs.spenden.diamant + "> – 100 Millionen Coins Spende\s" +
                "\n- **Custom Rolle + privaten Voice Channel** – 250 Millionen Coins Spende\s" +
                "\n  *(Rollen Kategorien können entfernt werden, wenn man eine Custom Rolle und keine der Rollen der jeweiligen Kategorie hat)*\s" +
                "\n\nPreise von Item Spenden werden zum Zeitpunkt der Spende bestimmt und dann nicht mehr geändert!" +
                "\nSpenden können nicht zurückerstattet werden!" +
                "\nAlle Spenden werden von <@502875153378705408> verwaltet, nur er wird spenden entgegennehmen!" +
                "\n\nUm zu spenden, klicke unten auf den Button `Spenden`."
        );

        channel.sendMessageEmbeds(eb.build()).addActionRow(Button.primary("spenden", "Spenden")).queue();
        event.getHook().sendMessage("Done").queue();
    }

    public void spendenTicketCreateButton(ButtonInteractionEvent event) {
        var coins = TextInput.create("spendenCoins", "Spendenbetrag", TextInputStyle.SHORT)
                .setPlaceholder("10m/25m/50m/100m/250m/eigener Betrag")
                .setRequiredRange(3, 100)
                .build();
        var dauer = TextInput.create("spendenDauer", "Dauer des Giveaways", TextInputStyle.SHORT)
                .setPlaceholder("1 Tag/1d/etc")
                .setRequiredRange(2, 100)
                .build();
        var winner = TextInput.create("spendenAnzahl", "Anzahl der Gewinner des Giveaways", TextInputStyle.SHORT)
                .setPlaceholder("1/1 Gewinner/etc")
                .setRequiredRange(1, 100)
                .build();
        var req = TextInput.create("spendenReq", "Requirements für das Giveaway", TextInputStyle.SHORT)
                .setPlaceholder("-/keine/nur Gildenmember/etc")
                .setRequiredRange(1, 100)
                .build();

        var modal = Modal.create("spenden", "Giveaway Spenden")
                .addComponents(ActionRow.of(coins), ActionRow.of(dauer), ActionRow.of(winner), ActionRow.of(req))
                .build();

        event.replyModal(modal).queue();
    }

    public void spendenTicketCloseButton(ButtonInteractionEvent event) throws WrongGuildException {
        event.deferReply().setEphemeral(true).queue();

        event.editButton(event.getButton().asDisabled()).queue();

        var user = event.getMember().getUser();

        if (user.getIdLong() == Configs.userIDs.epossID) {
            var channel = event.getChannel(); //Current Channel
            var log = event.getGuild().getTextChannelById(Configs.channelIDs.logs.ticketLogID);

            var eb = new EmbedBuilder()
                    .setFooter(Configs.devs.eposs.cmdDevText(), Configs.devs.eposs.icon())
                    .setTitle("Spenden-Ticket Log")
                    .setColor(Color.CYAN)
                    .setDescription("Das Spenden Ticket `#" + channel.getName() + "` wurde gelöscht.");

            // https://github.com/GamingLPyt/JDA-HTML-Transcript
            var transcript = new Transcript();
            try {
                transcript.createTranscript(channel, log);
            } catch (IOException e) {
                new Logging().printErr("Spenden Ticket Transcript");
                event.getHook().sendMessage("Transcript konnte nicht erstellt werden.").queue();
                event.editButton(event.getButton().asEnabled()).queue();
                return;
            }
            log.sendMessageEmbeds(eb.build()).queue();
            event.getHook().sendMessage("Ticket wird gelöscht...").queue();
            channel.delete().queueAfter(5L, TimeUnit.SECONDS);
        } else {
            event.getHook().sendMessage("Du hast nicht die Berechtigung, das Ticket zu schließen.").queue();
            event.editButton(event.getButton().asEnabled()).queue();
        }
    }

    public void spendenTicketCreateModal(ModalInteractionEvent event) {
        event.deferReply().setEphemeral(true).queue();
        var user = event.getUser();
        var dk = event.getGuild();

        var coins = event.getValue("spendenCoins").getAsString();
        var dauer = event.getValue("spendenDauer").getAsString();
        var winner = event.getValue("spendenAnzahl").getAsString();
        var req = event.getValue("spendenReq").getAsString();

        // Member perms im Ticket Channel
        Collection<Permission> perms = new ArrayList<>();
        perms.add(Permission.MESSAGE_SEND);
        perms.add(Permission.VIEW_CHANNEL);

        //Spenden Kategorie
        var ticket = dk.createTextChannel("spende-" + user.getName(), dk.getCategoryById(1088103086154858576L)).
                syncPermissionOverrides().
                addMemberPermissionOverride(user.getIdLong(), perms, null).
                setTopic("Spenden Ticket von " + user.getName()).complete();

        //Msg im Ticket Channel
        var eb = new EmbedBuilder()
                .setFooter(Configs.devs.eposs.cmdDevText(), Configs.devs.eposs.icon())
                .setColor(Color.CYAN)
                .setTitle("Spenden Ticket")
                .setDescription("""
                        Eposs wird sich so bald wie möglich im Ticket melden.
                        Überprüfe solange deine Angaben zum Giveaway unten.
                        Sollten diese fehlerhaft sein, schreibe die richtigen Angaben bitte ins Ticket.
                        """
                )
                .addField("Spendenbetrag", coins, false)
                .addField("Dauer des Giveaways", dauer, false)
                .addField("Anzahl der Gewinner", winner, false)
                .addField("Giveaway Requirements", req, false);

        ticket.sendMessage("<@502875153378705408> | Ticket von " + user.getAsMention())
                .addEmbeds(eb.build())
                .addActionRow(Button.danger("spenden-close", "Ticket schließen"))
                .queue();

        event.getHook().sendMessage("Dein Ticket wurde erstellt ( " + ticket.getAsMention() + " )").queue();
    }


    // @Spender-Bronze – 10 Millionen Coins Spende
    // @Spender-Silber – 25 Millionen Coins Spende
    // @Spender-Gold – 50 Millionen Coins Spende
    // @Spender-Diamant – 100 Millionen Coins Spende
    public void spendenRollenUpdate(long betrag, User spender, Guild guild) {
        //var user = spender.getUser();
        var sbRoleSys = new SbRoleSystem();

        if (betrag < 10000000 || betrag > 250000000) removeSpendenRollen(spender, guild);
        else if (betrag < 25000000) {
            var bronze = guild.getRoleById(Configs.rollenIDs.spenden.bronze);
            if (sbRoleSys.hasNotRole(spender, bronze, "spende", guild)) guild.addRoleToMember(spender, bronze).queue();
        }
        else if (betrag < 50000000) {
            var silber = guild.getRoleById(Configs.rollenIDs.spenden.silber);
            if (sbRoleSys.hasNotRole(spender, silber, "spende", guild)) guild.addRoleToMember(spender, silber).queue();
        }
        else if (betrag < 100000000) {
            var gold = guild.getRoleById(Configs.rollenIDs.spenden.gold);
            if (sbRoleSys.hasNotRole(spender, gold, "spende", guild)) guild.addRoleToMember(spender, gold).queue();
        }
        else if (betrag < 250000000) {
            var dia = guild.getRoleById(Configs.rollenIDs.spenden.diamant);
            if (sbRoleSys.hasNotRole(spender, dia, "spende", guild)) guild.addRoleToMember(spender, dia).queue();
        }
    }

    public void removeSpendenRollen(User spender, Guild guild) {
        var bronze = guild.getRoleById(Configs.rollenIDs.spenden.bronze);
        var silber = guild.getRoleById(Configs.rollenIDs.spenden.silber);
        var gold = guild.getRoleById(Configs.rollenIDs.spenden.gold);
        var dia = guild.getRoleById(Configs.rollenIDs.spenden.diamant);

        guild.removeRoleFromMember(spender, bronze).queue();
        guild.removeRoleFromMember(spender, silber).queue();
        guild.removeRoleFromMember(spender, gold).queue();
        guild.removeRoleFromMember(spender, dia).queue();
    }
}
