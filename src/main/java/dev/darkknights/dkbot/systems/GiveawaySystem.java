package dev.darkknights.dkbot.systems;

import dev.darkknights.Logging;
import dev.darkknights.WrongGuildException;
import dev.darkknights.config.Configs;
import dev.darkknights.dkbot.DarkKnightsBot;
import dev.darkknights.dkbot.management.database.GiveawayDataBase;
import dev.darkknights.dkbot.management.giveaways.Giveaway;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.selections.SelectOption;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.awt.*;
import java.sql.Date;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class GiveawaySystem {
    private static final Logging log = new Logging();

    /*
    Giveaway Create:
        Channel
        Beschreibung
        Gewinner Anzahl (int)
        Giveaway Zeit bis Ende (string)
        hat Req (bool) -> Role Select Menu

    Giveaway Reroll:
        Msg ID (string)
        opt. User -> oder alle

    TestBot.getTestJDA()
     */

    public void timerForActiveGiveaways() {
        try {
            ArrayList<JSONObject> allActive = new GiveawayDataBase().getAllActive();
            AtomicInteger counter = new AtomicInteger();

            if (allActive != null) {
                allActive.forEach(giveawayJson -> {
                    var giveaway = Giveaway.fromJson(giveawayJson);

                    startGiveawayTimer(giveaway);

                    counter.getAndIncrement();
                });
            }

            log.print("GDB: " + counter + " aktive Giveaways -> Timer gestartet.");
        } catch (ParseException e) {
            log.printErr("Giveaway Timer nicht gestartet!");
            var eposs = DarkKnightsBot.getJDA().getUserById(Configs.userIDs.epossID);  // Testbot -> DkBot
            eposs.openPrivateChannel().flatMap(privateChannel -> privateChannel.sendMessage("Giveaway Timer nicht gestartet!")).queue();
        }
    }

    private void giveawayStart(Giveaway giveaway, InteractionHook hook) {
        giveaway.setActive(true);

        var giveawayEb = giveaway.createEmbedStart();

        // Giveaway Nachricht senden
        var gwChannel = DarkKnightsBot.getJDA().getGuildById(Configs.gildenIDs.darkKnightsID).getTextChannelById(giveaway.getChannelID());
        gwChannel.sendMessageEmbeds(giveawayEb).queue(
                success -> {
                    var id = success.getIdLong();
                    giveaway.setMessageID(id);

                    try {
                        JSONObject giveAwayJson = giveaway.toJson();
                        new GiveawayDataBase().saveToDatabase(giveAwayJson);
                    } catch (ParseException e) {
                        e.printStackTrace();
                        log.printErr("Giveaway saveToDb ParseException: ID " + id);
                        hook.setEphemeral(true).sendMessage("Giveaway gestartet, aber Error beim Speichern in der Datenbank!").queue();
                        success.editMessage("<@" + Configs.userIDs.epossID + "> – Error beim Starten des Giveaways.").queue();
                        return;
                    }

                    success.addReaction(Configs.giveaways.giveawayEmoji).queue();

                    startGiveawayTimer(giveaway);

                    hook.setEphemeral(true).sendMessage("Giveaway gestartet und erfolgreich in Datenbank gesichert!").queue();
                }
        );
    }

    private void startGiveawayTimer(Giveaway giveaway) {
        var date = Date.from(giveaway.getEnde());

        var timer = new Timer();
        var task = new TimerTask() {
            public void run() {
                giveawayEnde(giveaway);
            }
        };

        timer.schedule(task, date);
    }

    private void giveawayEnde(Giveaway giveaway) {
        giveaway.setActive(false);

        var jda = DarkKnightsBot.getJDA();     // Testbot -> DkBot
        var guild = jda.getGuildById(Configs.gildenIDs.darkKnightsID);

        var channel = guild.getTextChannelById(giveaway.getChannelID());
        var msg = channel.retrieveMessageById(giveaway.getMessageID()).complete();

        var reaction = msg.getReaction(Configs.giveaways.giveawayEmoji);
        var users = reaction.retrieveUsers().complete();

        var userIDs = new ArrayList<Long>();
        users.forEach(user -> userIDs.add(user.getIdLong()));

        // Rollen Req...
        var roleReq = giveaway.getRollenRequirements();

        // Hat User eine der Req Rollen? true -> userId in gewinnerIds
        var moeglicheGewinnerIDs = new ArrayList<Long>();
        userIDs.forEach(userID -> {
            var member = guild.getMemberById(userID);

            // Alle Rollen IDs des Users
            var roleIDs = new ArrayList<Long>();
            member.getRoles().forEach(role -> roleIDs.add(role.getIdLong()));

            // jede Req Rolle -> wenn eine der Member Rollen = Req Rolle -> user kann gewinnen
            for (long req : roleReq) {
                for (long role : roleIDs) {
                    if (req == role) moeglicheGewinnerIDs.add(userID);
                }
            }
        });

        if (moeglicheGewinnerIDs.size() > 0) {
            // Anzahl Gewinner
            var gwZahl = giveaway.getGewinnerZahl();

            // Mögliche Gewinner kleiner notwendige Zahl Gewinner -> gwZahl = moeglGewinnerSize
            if (moeglicheGewinnerIDs.size() < gwZahl) gwZahl = moeglicheGewinnerIDs.size();

            var gewinnerIDs = new ArrayList<Long>();
            for (int i = 0; i < gwZahl; i++) {
                Random rand = new Random();
                // Random Zahl zw. 0 und max Index von möglGewinner
                int value = rand.nextInt(moeglicheGewinnerIDs.size());

                gewinnerIDs.add(moeglicheGewinnerIDs.get(value));
                moeglicheGewinnerIDs.remove(value);
            }
            giveaway.setGewinnerIDs(gewinnerIDs);
            giveaway.setTeilnehmerIDs(moeglicheGewinnerIDs);

            // ursprüngliche Giveaway Nachricht bearbeiten
            msg.editMessageEmbeds(giveaway.createEmbedEnd(false)).queue();

            // Gewinner pingen
            var gewinner = new StringBuilder();
            gewinnerIDs.forEach(id -> gewinner.append("<@").append(id).append("> | "));
            gewinner.replace(gewinner.length() - 3, gewinner.length() - 1, "");

            msg.reply("## Giveaway beendet!" +
                    "\nGewinner: " + gewinner +
                    "\n> " + giveaway.getBeschreibung()).queue();
        } else {
            msg.editMessageEmbeds(giveaway.createEmbedEnd(true)).queue();

            msg.reply("## Giveaway beendet!" +
                    "\nKein User erfüllt die Requirements. Keine Gewinner!" +
                    "\n> " + giveaway.getBeschreibung()).queue();
        }

        try {
            new GiveawayDataBase().saveToDatabase(giveaway.toJson());
        } catch (ParseException e) {
            e.printStackTrace();
            log.printErr("Giveaway saveToDb ParseException: ID " + giveaway.getMessageID());
        }
    }


    public void gwCreateCmd(SlashCommandInteractionEvent event) throws WrongGuildException, ParseException {
        var guild = event.getGuild();
        if (guild.getIdLong() != Configs.gildenIDs.darkKnightsID) throw new WrongGuildException("Giveaway Create");

        var gwChannel = event.getOption("channel").getAsChannel().asTextChannel();
        var gwDes = event.getOption("des").getAsString();
        var gwGewinnerZahl = event.getOption("winner").getAsInt();
        var gwEndeString = event.getOption("time").getAsString();
        var gwReq = event.getOption("req").getAsBoolean();

        Instant gwEndeInstant;
        try {
            gwEndeInstant = parseEndZeit(gwEndeString);
        } catch (NumberFormatException e) {
            log.printErr("Giveaway Zeit parseInt");
            event.getHook().setEphemeral(true).sendMessage("Zeitangabe (" + gwEndeString + ") ist ungültig." +
                            "\nZeit kann d/h/m für Tage/Stunden/Minuten enthalten, wenn keines enthalten wird Zeit in Minuten angenommen.")
                    .queue();
            return;
        }

        // Hat Giveaway Rollen Requirements?
        if (gwReq) {
            var eb = new EmbedBuilder()
                    .setFooter(Configs.devs.eposs.cmdDevText(), Configs.devs.eposs.icon())
                    .setTitle("Giveaway Create - Rollen Requirements")
                    .setColor(Color.blue)
                    .setDescription("Der Gewinner muss mindestens eine dieser Rollen haben.")
                    .addField("Channel", gwChannel.getAsMention(), true)
                    .addField("Gewinner", String.valueOf(gwGewinnerZahl), true)
                    .addField("End Zeit", "<t:" + gwEndeInstant.getEpochSecond() + ":R>", true)
                    .addField("Beschreibung", gwDes, false);

            var sel = StringSelectMenu.create("giveawayReq")
                    .setPlaceholder("Rollen Requirements auswählen")
                    .setRequiredRange(2, 10)
                    .addOption("Member (Standard)", gwEndeInstant.toString())
                    .addOption("Server Booster", "gwr:booster")
                    .addOption("Text Level 50", "gwr:text50")
                    .addOption("Text Level 25", "gwr:text25")
                    .addOption("Text Level 10", "gwr:text10")
                    .addOption("Voice Level 50", "gwr:voice50")
                    .addOption("Voice Level 25", "gwr:voice25")
                    .addOption("Voice Level 10", "gwr:voice10")
                    .setDefaultOptions(SelectOption.of("Member (Standard)", gwEndeInstant.toString()))
                    .build();

            event.getHook().sendMessageEmbeds(eb.build()).addActionRow(sel).queue();
        } else {
            // Member Rolle als standard Requirement
            var memberRole = guild.getRoleById(Configs.rollenIDs.general.memberRole);
            var rollenList = new ArrayList<Long>();
            rollenList.add(memberRole.getIdLong());

            var giveaway = new Giveaway(gwChannel.getIdLong(), gwGewinnerZahl, gwEndeInstant, gwDes, rollenList, event.getUser().getIdLong());
            giveawayStart(giveaway, event.getHook());
        }
    }

    public void gwCreateReqSelect(StringSelectInteractionEvent event) {
        event.deferReply(true).queue();

        var sel = event.getValues();
        var req = new ArrayList<Long>();

        if (sel.contains("gwr:booster")) req.add(Configs.rollenIDs.general.booster);

        if (sel.contains("gwr:text50")) req.add(Configs.rollenIDs.discordLevel.text50);
        if (sel.contains("gwr:text25")) req.add(Configs.rollenIDs.discordLevel.text25);
        if (sel.contains("gwr:text10")) req.add(Configs.rollenIDs.discordLevel.text10);

        if (sel.contains("gwr:voice50")) req.add(Configs.rollenIDs.discordLevel.voice50);
        if (sel.contains("gwr:voice25")) req.add(Configs.rollenIDs.discordLevel.voice25);
        if (sel.contains("gwr:voice10")) req.add(Configs.rollenIDs.discordLevel.voice10);

        // Giveaway Optionen aus Embed lesen
        String inst = null;
        for (SelectOption option : event.getSelectMenu().getOptions()) {
            if (option.getLabel().equals("Member (Standard)")) inst = option.getValue();
        }
        try {
            Instant endeInstant = Instant.parse(inst);

            long channelID = 0;
            int gwZahl = 0;
            String des = null;
            long hostID = event.getUser().getIdLong();

            var emb = event.getMessage().getEmbeds().get(0);
            for (MessageEmbed.Field field : emb.getFields()) {
                var title = field.getName();
                var text = field.getValue();

                switch (title) {
                    case "Channel" -> {
                        channelID = Long.parseLong(text.substring(text.indexOf('#') + 1, text.length() - 1));
                    }
                    case "Gewinner" -> {
                        gwZahl = Integer.parseInt(text);
                    }
                    case "Beschreibung" -> {
                        des = text;
                    }
                }
            }
            var giveaway = new Giveaway(channelID, gwZahl, endeInstant, des, req, hostID);
            giveawayStart(giveaway, event.getHook());
        } catch (DateTimeParseException e) {
            e.printStackTrace();
            log.printErr("Giveaway start DateTimeParseException");
            event.getHook().setEphemeral(true).sendMessage("Giveaway konnte nicht gestartet werden.").queue();
        }
    }

    private Instant parseEndZeit(String gwEnde) throws NumberFormatException {
        // gwEnde -> Instant
        var time = ZonedDateTime.now(ZoneId.of("Europe/Berlin"));
        time = time.withSecond(0).withNano(0);

        if (!gwEnde.contains("d") && !gwEnde.contains("h") && !gwEnde.contains("m")) {
            // keine Zeitangabe --> Zeit in Minuten
            int min = Integer.parseInt(gwEnde);
            time = time.plusMinutes(min);

        } else {
            var d = gwEnde.indexOf("d");
            var h = gwEnde.indexOf("h");
            var m = gwEnde.indexOf("m");

            try {
                int days = Integer.parseInt(gwEnde.substring(0, d));
                time = time.plusDays(days);
            } catch (IndexOutOfBoundsException e) {
                //System.out.println("d " + e.getMessage());
            }
            try {
                int hours = Integer.parseInt(gwEnde.substring(d + 1, h));
                time = time.plusHours(hours);
            } catch (IndexOutOfBoundsException e) {
                //System.out.println("h " + e.getMessage());
            }
            try {
                int min = Integer.parseInt(gwEnde.substring(h + 1, m));
                time = time.plusMinutes(min);
            } catch (IndexOutOfBoundsException e) {
                //System.out.println("m " + e.getMessage());
            }
        }

        return time.toInstant();
    }


    public void gwRerollCmd(SlashCommandInteractionEvent event) throws ParseException {
        var gwMsgId = event.getOption("msgid").getAsString();

        var gwJson = new GiveawayDataBase().getFromDatabase(gwMsgId);
        if (gwJson == null) {
            event.getHook().setEphemeral(true).sendMessage("Diese Nachricht ist kein Giveaway.").queue();
        }
        else {
            var giveaway = Giveaway.fromJson(gwJson);
            if (giveaway.isActive()) {
                event.getHook().setEphemeral(true).sendMessage("Dieses Giveaway ist noch bis <t: " + giveaway.getEnde().getEpochSecond() + ":R> aktiv. Es gibt noch keine Gewinner.").queue();
            }
            else {
                var alteGewinnerIDs = giveaway.getGewinnerIDs();
                List<Long> moeglicheGewinnerIDs = giveaway.getTeilnehmerIDs();

                if (moeglicheGewinnerIDs == null || moeglicheGewinnerIDs.size() == 0) {
                    event.getHook().sendMessage("Das Giveaway hat keine weiteren Teilnehmer, welche die Requirements erfüllen.").queue();
                    return;
                }

                var jda = DarkKnightsBot.getJDA();     // Testbot -> DkBot
                var guild = jda.getGuildById(Configs.gildenIDs.darkKnightsID);

                var channel = guild.getTextChannelById(giveaway.getChannelID());
                var msg = channel.retrieveMessageById(giveaway.getMessageID()).complete();

                // Damit NullPointer nach dem User bestimmen nicht alle Rerollen triggert
                boolean control = false;
                try {
                    // 1 User rerollen
                    var gwUser = event.getOption("user").getAsUser();
                    control = true;

                    var oldWinnerID = gwUser.getIdLong();
                    if (alteGewinnerIDs.contains(oldWinnerID)) {
                        alteGewinnerIDs.remove(oldWinnerID);

                        Random rand = new Random();
                        // Random Zahl zw. 0 und max Index von möglGewinner
                        int value = rand.nextInt(moeglicheGewinnerIDs.size());

                        var newWinnerID = moeglicheGewinnerIDs.get(value);
                        alteGewinnerIDs.add(newWinnerID);
                        moeglicheGewinnerIDs.remove(value);

                        giveaway.setGewinnerIDs(alteGewinnerIDs);
                        giveaway.setTeilnehmerIDs(moeglicheGewinnerIDs);

                        // ursprüngliche Giveaway Nachricht bearbeiten
                        msg.editMessageEmbeds(giveaway.createEmbedEnd(false)).queue();

                        // neuen Gewinner pingen
                        msg.reply("## Reroll - " + gwUser.getName() +
                                "\nNeuer Gewinner: <@" + newWinnerID + ">" +
                                "\n> " + giveaway.getBeschreibung()).queue();

                        event.getHook().setEphemeral(true).sendMessage("## Reroll" +
                                "\n" + gwUser.getAsMention() + " -> <@" + newWinnerID + ">").queue();
                    } else {
                        event.getHook().setEphemeral(true).sendMessage("Der User " + gwUser.getAsMention() + " ist kein Gewinner des Giveaways.").queue();
                        return;
                    }
                } catch (NullPointerException e) {
                    if (control) {
                        e.printStackTrace();
                        log.printErr("Giveaway Reroll - NullPointer, control = true");
                        event.getHook().sendMessage("Es gab einen Fehler beim ausführen des Befehls.").queue();
                        return;
                    } else {
                        // Alle Rerollen

                        var gwZahl = giveaway.getGewinnerZahl();
                        // Mögliche Gewinner kleiner notwendige Zahl Gewinner -> gwZahl = moeglGewinnerSize
                        if (moeglicheGewinnerIDs.size() < gwZahl) gwZahl = moeglicheGewinnerIDs.size();

                        var gewinnerIDs = new ArrayList<Long>();
                        for (int i = 0; i < gwZahl; i++) {
                            Random rand = new Random();
                            // Random Zahl zw. 0 und max Index von möglGewinner
                            int value = rand.nextInt(moeglicheGewinnerIDs.size());

                            gewinnerIDs.add(moeglicheGewinnerIDs.get(value));
                            moeglicheGewinnerIDs.remove(value);
                        }
                        giveaway.setGewinnerIDs(gewinnerIDs);
                        giveaway.setTeilnehmerIDs(moeglicheGewinnerIDs);

                        // ursprüngliche Giveaway Nachricht bearbeiten
                        msg.editMessageEmbeds(giveaway.createEmbedEnd(false)).queue();

                        // neue Gewinner pingen
                        var gewinner = new StringBuilder();
                        gewinnerIDs.forEach(id -> gewinner.append("<@").append(id).append("> | "));
                        gewinner.replace(gewinner.length() - 3, gewinner.length() - 1, "");

                        msg.reply("## Reroll - alle Gewinner" +
                                "\nNeue Gewinner: " + gewinner +
                                "\n> " + giveaway.getBeschreibung()).queue();

                        event.getHook().setEphemeral(true).sendMessage("## Reroll - alle" +
                                "\nNeue Gewinner: " + gewinner).queue();
                    }
                }
                try {
                    new GiveawayDataBase().saveToDatabase(giveaway.toJson());
                } catch (ParseException e) {
                    e.printStackTrace();
                    log.printErr("Giveaway saveToDb ParseException: ID " + giveaway.getMessageID());
                }
            }
        }
    }
}
