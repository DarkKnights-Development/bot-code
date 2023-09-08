package dev.darkknights.dkbot.cmds.slash;

import dev.darkknights.config.Configs;
import dev.darkknights.dkbot.management.database.MemberDataBase;
import dev.darkknights.dkbot.management.memberdata.ServerMember;
import dev.darkknights.dkbot.systems.SpendenSystem;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.json.simple.parser.ParseException;

public class spende {
    public void cmd(SlashCommandInteractionEvent event) throws ParseException {
        String subCmd = event.getSubcommandName();

        var user = event.getUser();

        var sSys = new SpendenSystem();

        if (subCmd.equals("list")) {
            sSys.spendeListCmd(event);
            return;
        }

        if (user.getIdLong() != Configs.devs.eposs.discordID()) {
            event.getHook().setEphemeral(true).sendMessage("Nur Eposs kann diesen Befehl nutzen, da er die Spenden verwaltet.").queue();
            return;
        }

        var spender = event.getOption("spender").getAsUser();
        var spenderID = spender.getId();

        var guild = event.getGuild();

        var betragStr = event.getOption("betrag").getAsString();

        if (betragStr.contains("m")) betragStr = betragStr.replace("m", "000000");
        if (betragStr.contains("b")) betragStr = betragStr.replace("b", "000000000");
        long betrag = Long.parseLong(betragStr);

        var mdb = new MemberDataBase();

        if (mdb.hasData(spenderID)) {
            var sm = new ServerMember(mdb.getFromDatabase(spenderID));
            var flags = sm.getDiscordFlags();

            switch (subCmd) {
                case "set" -> {
                    var betragAlt = flags.getSpendenBetrag();
                    flags.setSpendenBetrag(betrag); //Spendenbetrag überschreiben

                    mdb.saveToDatabase(sm.toJsonObject());

                    sSys.spendenRollenUpdate(betrag, spender, guild);

                    event.getHook().sendMessage("Spendenbetrag für " + spender.getAsMention() + " auf `" + betrag + "` [ " + betrag / 1000000 + "m | " + betragAlt / 1000000000 + "b ]" +
                            "\n(Vorher: `" + betrag + "` [ " + betragAlt / 1000000 + "m | " + betragAlt / 1000000000 + "b ])").queue();
                }
                case "add" -> {
                    var val = flags.getSpendenBetrag() + betrag;
                    flags.setSpendenBetrag(val);

                    mdb.saveToDatabase(sm.toJsonObject());

                    sSys.spendenRollenUpdate(val, spender, guild);

                    event.getHook().sendMessage("Spendenbetrag für " + spender.getAsMention() + " auf `" + val + "` [ " + val / 1000000 + "m | " + val / 1000000000 + "b ] gesetzt.").queue();
                }
                case "remove" -> {
                    var val = flags.getSpendenBetrag() - betrag;
                    if (val < 0) val = 0;

                    mdb.saveToDatabase(sm.toJsonObject());

                    sSys.spendenRollenUpdate(val, spender, guild);

                    event.getHook().sendMessage("Spendenbetrag für " + spender.getAsMention() + " auf `" + val + "` [ " + val / 1000000 + "m | " + val / 1000000000 + "b ] gesetzt.").queue();
                }
            }
        } else {
            event.getHook().sendMessage(spender.getAsMention() + " hat noch nicht </link:1113182819393478657> genutzt...").queue();
        }
    }
}
