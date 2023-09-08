package dev.darkknights.dkbot.listener.listen;

import dev.darkknights.WrongGuildException;
import dev.darkknights.dkbot.cmds.slash.sudo;
import dev.darkknights.dkbot.systems.GiveawaySystem;
import dev.darkknights.dkbot.systems.selfroles.ClassRoleSystem;
import dev.darkknights.dkbot.systems.selfroles.GameModeRoleSystem;
import dev.darkknights.dkbot.systems.selfroles.GildenRoleSystem;
import dev.darkknights.dkbot.systems.selfroles.PingRoleSystem;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import org.json.simple.parser.ParseException;

import java.net.MalformedURLException;

public class StringSelectInteraction {
    public void onStringSelectInteraction(StringSelectInteractionEvent event) {
        var menu = event.getComponentId();

        try {
            switch (menu) {
                case "Class Rollen" -> new ClassRoleSystem().classRoleStringSelect(event);
                case "Gilden Rollen" -> new GildenRoleSystem().gildenRoleStringSelect(event);
                case "Ping Rollen" -> new PingRoleSystem().pingRoleStringSelect(event);
                case "Gamemode Rollen" -> new GameModeRoleSystem().gameModeRoleStringSelect(event);
                case "sudoSubCmdSelect" -> new sudo().sudoSelectMenu(event);
                case "giveawayReq" -> new GiveawaySystem().gwCreateReqSelect(event);
            }
        } catch (WrongGuildException e) {
            e.setLocation("dev.darkknights.listeners.EventListener.onStringSelectInteraction");
            event.getHook().setEphemeral(true).sendMessage("Diese Interaction funktioniert nur auf dem DarkKnights Discord Server.").queue();
        } catch (NullPointerException | ParseException | MalformedURLException e) {
            event.getHook().setEphemeral(true).sendMessage("Es gab einen Fehler beim Ausführen des Befehls.").queue();
        }
    }

}
