package dev.darkknights.dkbot.cmds.slash;

import dev.darkknights.Logging;
import dev.darkknights.WrongGuildException;
import dev.darkknights.config.Configs;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.awt.*;
import java.util.Map;


//###########################################
//# /send
//###########################################
public class send {
    private static final Logging log = new Logging();

    //###########################################
    //# /send message
    //###########################################
    public void message(SlashCommandInteractionEvent event) {
        var messageOption = event.getOption("message");
        var message = messageOption.getAsString();

        MessageChannel channel;
        var channelOption = event.getOption("channel");
        if (channelOption != null) {
            channel = channelOption.getAsChannel().asGuildMessageChannel();
        } else {
            channel = event.getChannel();
        }

        log.logCmd("send", "message", event.getUser().getName(), Map.of("msg", message));

        channel.sendMessage(message).queue();
        var response = "Message Send in " + channel.getAsMention();
        event.getHook().sendMessage(response).setEphemeral(true).queue();
    }


    //###########################################
    //# /send embed
    //###########################################
    public void embed(SlashCommandInteractionEvent event) {
        var user = event.getUser().getName();

        var eb = new EmbedBuilder()
                .setFooter(Configs.devs.eposs.cmdDevText() + " | Msg sent by @" + user, Configs.devs.eposs.icon());

        var messageOption = event.getOption("message");
        var input = messageOption.getAsString();
        var message = input.replaceAll(";;", "\n");
        eb.setDescription(message);

        MessageChannel channel;
        OptionMapping channelOption = event.getOption("channel");

        if (channelOption != null) {
            channel = channelOption.getAsChannel().asGuildMessageChannel();
        } else {
            channel = event.getChannel();
        }

        var embedTitle = event.getOption("title");
        var title = "x";
        if (embedTitle != null) {
            title = embedTitle.getAsString();
            eb.setTitle(title);
        }

        var embedColor = event.getOption("color");
        var color = Color.BLACK;
        var colorString = "";
        if (embedColor != null) colorString = embedColor.getAsString().toLowerCase();

        // Farbe des Embed
        switch (colorString) {
            case "dark_grey" -> color = Color.DARK_GRAY;
            case "grey" -> color = Color.GRAY;
            case "light_grey" -> color = Color.LIGHT_GRAY;
            case "white" -> color = Color.WHITE;
            case "red" -> color = Color.RED;
            case "orange" -> color = Color.ORANGE;
            case "yellow" -> color = Color.YELLOW;
            case "green" -> color = Color.GREEN;
            case "cyan" -> color = Color.CYAN;
            case "blue" -> color = Color.BLUE;
            case "pink" -> color = Color.PINK;
            case "magenta" -> color = Color.MAGENTA;
        }

        eb.setColor(color);

        log.logCmd("send", "embed", user, Map.of("title", title, "msg", input));

        channel.sendMessageEmbeds(eb.build()).queue();
        var response = "Message Send in " + channel.getAsMention();
        event.getHook().sendMessage(response).setEphemeral(true).queue();
    }


    //###########################################
    //# /send infochannel
    //###########################################
    public void infochannel(SlashCommandInteractionEvent event) throws WrongGuildException {
        var channel = event.getChannel().asTextChannel();

        var eb = new EmbedBuilder()
                .setColor(Color.orange)
                .setFooter(Configs.devs.eposs.cmdDevText(), Configs.devs.eposs.icon())
                .setTitle("Team Liste und Zuständigkeiten")
                .setDescription(
                        "<@&" + Configs.rollenIDs.general.teamMan + "> - <@" + Configs.userIDs.jesseID + ">\n" +
                                "<@&" + Configs.rollenIDs.general.teamMan + "> - <@" + Configs.userIDs.tillmenID + ">\n" +
                                "<@&" + Configs.rollenIDs.general.botMan + "> - <@" + Configs.userIDs.epossID + ">\n" +
                                "<@&" + Configs.rollenIDs.general.dcMan + "> - <@" + Configs.userIDs.geringverdienerID + ">\n" +
                                "<@&" + Configs.rollenIDs.general.admin + "> - <@" + Configs.userIDs.arcoID + ">\n" +
                                "<@&" + Configs.rollenIDs.general.admin + "> - <@" + Configs.userIDs.matyID + ">\n" //+
                                //"<@&" + Configs.rollenIDs.general.admin + "> - <@" + Configs.userIDs.eliasID + ">\n" +
                                //"<@&" + Configs.rollenIDs.general.admin + "> - <@" + Configs.userIDs.deshoouID + ">\n"
                );
        channel.sendMessageEmbeds(eb.build()).queue();

        eb.setTitle("Erklärung der Rollen")
                .setDescription(
                        "<@&" + Configs.rollenIDs.general.teamMan + "> **:** Organisation des Teams, Verwaltung der Leader/Mods\n" +
                                "<@&" + Configs.rollenIDs.general.botMan + "> **:** Verwaltung der Bots, Entwicklung des eigenen Bots\n" +
                                "<@&" + Configs.rollenIDs.general.dcMan + "> **:** Verwaltung der Rollen und Channel\n" +
                                "<@&" + Configs.rollenIDs.general.admin + "> **:** Moderation des Discord Servers, Bearbeitung der Support Tickets, Lösen spontan anfallender Probleme\n" +
                                "<@&" + Configs.rollenIDs.general.designer + "> **:** Member, die Serverbild, Banner, etc erstellt haben\n" +
                                "<@&" + Configs.rollenIDs.general.leaderIG + "> **/** <@&" + Configs.rollenIDs.general.modIG + "> **:** Kosmetische Rollen auf dem Discord Server, nur Aufgaben ingame\n" +
                                "<@&" + Configs.rollenIDs.general.yt + "> **:** Member, die bei unserem YouTube Kanal mithelfen\n"
                );
        channel.sendMessageEmbeds(eb.build()).queue();

        eb.setTitle("DarkKnight Community")
                .setDescription(
                        "Hallo, wir sind die DarkKnights Community, der Nachfolger der Aliance Community.\n" +
                                "Wir beschäftigen uns Hauptsächlich mit Hypixel und Hypixel Skyblock.\n" +
                                "Wir betreiben mehrere Skyblock Gilden denen du gerne beitreten kannst (siehe **DarkKnight Gilden**).\n\n" +
                                "Viel Spaß auf den Discord Server wünschen die Gründer:\n" +
                                "<@" + Configs.userIDs.jesseID + ">\n" +
                                "<@" + Configs.userIDs.epossID + ">\n" +
                                "<@" + Configs.userIDs.tillmenID + ">\n" +
                                "<@" + Configs.userIDs.geringverdienerID + ">\n" +
                                "<@" + Configs.userIDs.arcoID + ">\n" +
                                "<@" + Configs.userIDs.matyID + ">\n" +
                                "<@" + Configs.userIDs.deshoouID + ">\n" +
                                "<@" + Configs.userIDs.eliasID + ">\n" +
                                "<@" + Configs.userIDs.alexID + ">\n"
                );
        channel.sendMessageEmbeds(eb.build()).queue();


        eb.setTitle("DarkKnight Gilden")
                .setDescription("""
                        - **DarkKnights1** [ ehemals Ali18 ]
                        –> GM: <@705042128085975101> (Ign: MrBear130)

                        - **DarkKnights2** [ ehemals Ali2 ]
                        –> GM: <@684108553790947331> (Ign: TTVTheHerocatGHG)

                        - ~~**DarkKnights3** [ ehemals Ali3 ]~~
                        ~~–> GM: <@811259086787575838> (Ign: Crowny111)~~
                        Crowny hat DarkKnights verlassen und behält die Gilde mit dem Namen.

                        - **DarkKnights4** [ ehemals Ali11 ]
                        –> GM: <@502875153378705408> (Ign: Eposs)

                        - **DarkKnights5** [ ehemals Ali13 ]
                        –> GM: <@860976205078986774> (Ign: Deshoou)

                        - **DarkKnights6** [ ehemals Ali4 ]
                        –> GM: <@462510531710943233> (Ign: LittleLaqee)

                        - **DarkKnights7** [ ehemals Ali8 ]
                        –> GM: <@616626232775409698> (Ign: Lyror)
                                        
                        - **DarkKnights8** [ ehemals Ali5 ]
                        –> GM: <@477465163491180568> (Ign: mimapi)
                        –> Leader: <@775802030631419926> (Ign: skillrequired)
                                        
                        - **DarkKnightsDungeon** [ ehemals AliDungeons ]
                        –> GM: <@575649385522790412> (Ign: ArcoGames)
                        """
                );
        channel.sendMessageEmbeds(eb.build()).queue();

        event.getHook().sendMessage("Done").queue();
    }


    //###########################################
    //# /send nützliches
    //###########################################
    public void nuetzliches(SlashCommandInteractionEvent event) {
        var channel = event.getChannel().asTextChannel();

        var eb = new EmbedBuilder()
                .setFooter(Configs.devs.tillmen.cmdDevText(), Configs.devs.tillmen.icon())
                .setTitle("Nützliches")
                .setColor(Color.orange);
        eb.setDescription("`Benutzung der folgenden Mods und Clients auf eigene Gefahr`");
        eb.addField("__Modlauncher__", """
                **Forge:**  https://files.minecraftforge.net/net/minecraftforge/forge/index_1.8.9.html
                        (nach 5 Sekunden, oben rechts "Skip" drücken)
                                                    
                **Sky-Client:** https://hypixel.net/threads/skyclient-easily-install-and-update-skyblock-mods-and-packs.3731617/
                        (optional)
                                                    
                (^einfachste Wege um Mods zu installieren^)
                """, false);
        eb.addBlankField(false);
        eb.addField("__Wichtige Skyblock Mods__", """
                **Skytils (ST):** https://github.com/Skytils/SkytilsMod/releases/
                                                    
                **Skyblock Addons (SBA):** https://github.com/BiscuitDevelopment/SkyblockAddons/releases/
                                                    
                **NotEnoughUpdates (NEU):** https://github.com/Moulberry/NotEnoughUpdates/releases/
                        (zieht FPS)
                                                    
                **Scrollable Tooltips:** https://sk1er.club/mods/text_overflow_scroll
                                                    
                **Dankers Mod (DSM):** https://github.com/bowser0000/SkyblockMod/releases
                """, false);
        eb.addBlankField(false);
        eb.addField("__Optionale Mods__", """
                **Cowlection:** https://github.com/cow-mc/Cowlection/releases
                                                    
                **Skyblock Extras (SBE):** https://discord.com/invite/sbe
                        (kostenpflichtig)
                                                    
                **Chat Triggers:** https://www.chattriggers.com/
                        (Hat verschiedene Module zum hinzufügen)
                        Module Startseite: https://www.chattriggers.com/modules
                        
                **Soopy:** https://soopy.dev/soopyv2
                                                    
                **Dungeon Secret Waypoints:** https://github.com/Quantizr/DungeonRoomsMod/releases/
                """, false);
        eb.addBlankField(false);
        eb.addField("__FPS Booster Mods__", """
                **Patcher:** https://sk1er.club/mods/patcher
                                                    
                **Optifine:** https://optifine.net/adloadx?f=OptiFine_1.8.9_HD_U_M5.jar&x=e4d0
                """, false);
        eb.addBlankField(false);
        eb.addField("__FPS Boost Clients__", """
                **Badlion:** https://client.badlion.net/download
                        (keine eigenen Mods)
                                                    
                **LabyMod:** https://www.labymod.net/de
                        (kann mit anderen Mods konkurrieren)
                """, false);
        eb.addBlankField(false);
        eb.addField("__Tutorials__", """
                **Beginner Guide:** https://www.youtube.com/watch?v=Cr0nwCYDJNE
                                                    
                **Pets:** https://www.youtube.com/watch?v=Fd9cfcymr1U

                **Romero & Juliette Quest:** https://www.youtube.com/watch?v=d9H2YhthtOc

                **Dungeons:** https://www.youtube.com/watch?v=7LDHl3MfA5M

                **Dwarven Mines 1:** https://www.youtube.com/watch?v=j3HHhBFQ_18
                **Dwarven Mines 2:** https://www.youtube.com/watch?v=0JwelZJD9_A
                                                   
                **Crystal Hollows:** https://www.youtube.com/watch?v=P6jmJxPX74M

                **Crimson Island:** https://www.youtube.com/watch?v=NBZx45ZVRCg
                                                
                **Garden Tutorial:** https://www.youtube.com/watch?v=IsL0-T0knD4

                **Slayer:** https://www.youtube.com/watch?v=JKKOFT10P-g
                """, false);
        eb.addBlankField(false);
        eb.addField("__Webseiten__", """
                **Bazaar Tracker:** https://bazaartracker.com/
                                                    
                **Player Stats (Skycrypt):** https://sky.shiiyu.moe/
                                                    
                **Hypixel Forum:** https://hypixel.net/categories/skyblock.194/
                                                    
                **Skyblock Leaderboard:** https://skyblock.matdoes.dev/leaderboards
                                                    
                **Offiziell Skyblock Wiki:** https://wiki.hypixel.net/Main_Page
                                                    
                **Ah Tracker und Preise:** https://sky.coflnet.com/
                """, false);
        eb.addBlankField(false);
        eb.addField("__Kurze Anleitung__", """
                1. Mc Vanilla 1.8.9 starten und wieder schließen
                2. Launcher schließen
                3. Forge 1.8.9 **latest installer** herunterladen und installieren
                4. Launcher öffnen und Forge einmal starten (um zu gucken ob es überhaupt geht), dann wieder schließen
                5. In deinem Minecraft Ordner die Mods in den *Mods* Ordner legen
                6. Launcher öffnen und Forge starten —> viel Spaß die ganzen Configs einzustellen
                """, false);
        channel.sendMessageEmbeds(eb.build()).queue();

        event.getHook().sendMessage("Done").queue();
    }


    //###########################################
    //# /send regeln
    //###########################################
    public void regeln(SlashCommandInteractionEvent event) {
        var channel = event.getChannel().asTextChannel();

        var eb = new EmbedBuilder()
                .setFooter(Configs.devs.tillmen.cmdDevText(), Configs.devs.tillmen.icon())
                .setTitle("Regelwerk")
                .setColor(Color.orange);
        eb.addField("§ 1 [Einhaltung der Regeln]", """
                - **§ 1.1** Das Umgehen von Regeln ist verboten und wird administrativ geahndet.
                - **§ 1.2** Alle Discord Regeln gelten selbstverständlich auf diesem Server und müssen eingehalten werden.
                - **§ 1.3** Das Team kann Strafen für Dinge vergeben, die nicht explizit durch die Regeln verboten werden, aber dennoch nicht richtig sind.
                - **§ 1.4** Das Team muss sich nicht für Strafen rechtfertigen. Falls du eine Strafe unangemessen findest, öffne ein Ticket ( <#1067421699726901299> ). Wenn du ein Teammitglied per Direktnachticht wegen einer Strafe anschreibst, die dich nicht am öffnen eines Tickets abhält (Timeout), kann es weitere Konsequenzen geben.
                """, false);
        eb.addField("§ 2 [Verhaltenskodex]", """
                - **§ 2.1** Ein freundlicher, toleranter und respektvoller Umgang ist Pflicht.
                - **§ 2.2** Beleidigungen, Provokation und ähnliche Dinge sind verboten. Als solche gelten Aussagen, von denen sich der Angesprochene beleidigt, provoziert oder in anderer Art und Weise menschlich verletzt fühlt.
                - **§ 2.3** Mobbing wird mit sofortigem Ausschluss bestraft! Niemand wird auf dem Server ausgegrenzt!
                - **§ 2.4** Das Posten/Senden von pornografischen, nicht jugendfreien, rassistischen, volksverhetzenden oder in anderer Weise rechtswidrigen Inhalten ist untersagt.
                - **§ 2.5** Nickname/Avatare dürfen keine pornographischen, rassistischen oder beleidigenden Inhalte enthalten.
                - **§ 2.6** Betteln jeglicher Art ist untersagt. Einmal freundlich nach etwas fragen wird jedoch nicht bestraft.
                - **§ 2.7** Jegliche Art von Werbung ist untersagt.
                """, false);
        eb.addField("§ 3 [Verhaltenskodex - Voice Chat]", """
                - **§ 3.1** Alle Regeln aus **§ 2**, die auch auf Voice Channel angewandt werden können, gelten auch für Voice Channel.
                - **§ 3.2** Störgeräusche, Echos oder Ähnliches sind zu vermeiden.
                - **§ 3.3** Das Mitschneiden von Gesprächen ist auf dem gesamten Server nur nach Absprache mit den anwesenden Benutzern des entsprechenden Channels erlaubt. Willigt ein User nicht der Aufnahme ein, ist die Aufnahme des Gesprächs verboten.
                - **§ 3.4** Channel Hopping ist verboten. Das bedeutet, nicht einfach durch die Gegend herumzuswitchen.
                - **§ 3.5** Um AFK Voice XP farmen zu verhindern, ist es untersagt, länger als 10 Minuten __alleine__ in einem Voice Channel zu sein.
                """, false);
        eb.addField("§ 4 [Ingame Regeln]", """
                - **§ 4.1** Diese Regeln gelten zusätzlich zu **§ 1**, **§ 2** und **§ 3** für die Gilden auf Hypixel.
                - **§ 4.2** Alle Hypixel Regeln müssen eingehalten werden. Diese sind auf https://hypixel.net/rules zu finden.
                - **§ 4.3** Scammen oder Ratten (Session ID von Anderen auslesen) wird mit einem Ban bestraft.
                """, false);
        eb.addField("§ 5 [DarkKnights Bot]", """
                - **§ 5.1** Die Terms of Service ("ToS") und Privacy Policy ("PP") des DarkKnights Bots findet ihr unter https://github.com/DarkKnights-Development/info#readme
                - **§ 5.2** Wenn ihr den ToS oder der PP nicht zustimmen wollt, müsst ihr diesen Server verlassen.
                """, false);
        eb.addField("§ 6 [Änderungen der Regeln]", """
                - **§ 6.1** Das Team behält sich vor, die Regeln jederzeit und ohne Ankündigung zu ändern.
                - **§ 6.2** Änderungsdatum: `13.06.2023`
                """, false);
        channel.sendMessageEmbeds(eb.build()).queue();

        event.getHook().sendMessage("Done").queue();
    }


    //###########################################
    //# /send teamregeln
    //###########################################
    public void teamregeln(SlashCommandInteractionEvent event) {
        var channel = event.getChannel().asTextChannel();

        var eb = new EmbedBuilder()
                .setFooter(Configs.devs.eposs.cmdDevText(), Configs.devs.eposs.icon())
                .setTitle("Team Regeln")
                .setColor(Color.orange);
        eb.setDescription("""
                - Für Teammitglieder gilt ebenfalls das Regelwerk!
                                                    
                - Jegliche Form der Rechteausnutzung führt zu einem Ausschluss aus dem Team. Rechteausnutzung ist jede Handlung, die keinen moderativen Grund hat. Dazu zählen unter Anderem Moven und Verbindung trennen, Warns verteilen, Muten, Timeouten, Rollen entfernen oder hinzufügen.

                - Sollte ein Voice-Channel die maximale Memberzahl erreicht haben, darf der Channel nicht ohne triftigen Grund betreten werden.
                                                    
                - Das ständige Ändern des Namens und/oder des Profilbilds ist verboten. Namensänderungen bitte in den Team Chat schreiben.
                                                    
                - Sollte ein Teammitglied beim Betteln (Fragen nach gratis Items/Geld) erwischt werden, so muss mit einem SOFORTIGEN Ausschluss aus dem Team gerechnet werden.
                """);
        channel.sendMessageEmbeds(eb.build()).queue();

        event.getHook().sendMessage("Done").queue();
    }

    //###########################################
    //# /send supportregeln
    //###########################################
    public void supportregeln(SlashCommandInteractionEvent event) {
        var channel = event.getChannel().asTextChannel();

        var eb = new EmbedBuilder()
                .setFooter(Configs.devs.eposs.cmdDevText(), Configs.devs.eposs.icon())
                .setTitle("Support Regeln")
                .setColor(Color.orange);
        eb.setDescription("""
                - Nur 1 Ticket pro Person
                - Wiederholtes Öffnen von Tickets ohne Grund wird mit einem Ban bestraft.
                - Für Support im Talk: <#1067644223958028308>
                - Denkt daran, dass wir nicht den ganzen Tag gucken, ob jemand in <#1067644223958028308> wartet. Am wahrscheinlichsten guckt ein Admin auf den Talk, wenn er selbst gerade in einem Talk ist.
                """);
        channel.sendMessageEmbeds(eb.build()).queue();

        event.getHook().sendMessage("Done").queue();
    }
}
