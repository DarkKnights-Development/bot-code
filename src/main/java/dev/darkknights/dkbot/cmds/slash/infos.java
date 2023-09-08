package dev.darkknights.dkbot.cmds.slash;

import dev.darkknights.Logging;
import dev.darkknights.config.Configs;
import dev.darkknights.dkbot.management.HypixelApiManagement;
import dev.darkknights.dkbot.management.database.MemberDataBase;
import dev.darkknights.dkbot.management.database.SbProfileDatabase;
import dev.darkknights.dkbot.management.memberdata.ServerMember;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.json.simple.parser.ParseException;

import java.awt.*;
import java.net.MalformedURLException;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class infos {

    public void infosCmd(SlashCommandInteractionEvent event) throws ParseException, MalformedURLException {
        var user = event.getUser();

        var target = event.getOption("user").getAsUser();
        var targetID = target.getId();

        new Logging().logCmd("infos", user.getName(), Map.of("target", target.getName()));

        var mdb = new MemberDataBase();

        if (mdb.hasData(targetID)) {
            var eb = new EmbedBuilder()
                    .setFooter(Configs.devs.eposs.cmdDevText(), Configs.devs.eposs.icon())
                    .setColor(Color.BLUE)
                    .setTitle("User Infos")
                    .setDescription(target.getAsMention() + " – Join/Creation Date: Erwähnung anklicken.");

            var dataJson = mdb.getFromDatabase(targetID);
            var dcData = new ServerMember(dataJson);
            var ign = dcData.getMinecraftUsername();
            var uuid = dcData.getMinecraftUUID();

            eb.addField("Mc Ign und UUID", ign + " – " + uuid, false);

            if (new SbProfileDatabase().hasData(uuid)) {
                var hypApiMan = new HypixelApiManagement();

                var sbLvlXp = hypApiMan.getSbLvlExp(uuid, hypApiMan.getSelectedProfile(uuid));

                eb.addField("Skyblock Level", String.valueOf(sbLvlXp), true);
            }

            try {
                var guild = HypixelApiManagement.API.getGuildByPlayer(uuid).get().getGuild().getName();
                eb.addField("Gilde", guild, true);
            } catch (ExecutionException | InterruptedException e) {
                new Logging().printErr("/infos - Exception while getting Guild - " + e.getMessage());
            }

            eb.addField("Spendenbetrag", dcData.getDiscordFlags().getSpendenBetrag() + " Coins", true);

            eb.addField("Links",
                    "[SkyCrypt](https://sky.shiiyu.moe/stats/" + ign + ")" +
                            " | [Plancke](https://plancke.io/hypixel/player/stats/" + ign + ")" +
                            " | [Auction History](https://sky.coflnet.com/player/" + uuid + ")"
                    , false);

            event.getHook().sendMessageEmbeds(eb.build()).queue();
        } else {
            event.getHook().sendMessage("Der Member " + target.getAsMention() + " hat keine Daten in der Datenbank.").queue();
        }
    }
}
