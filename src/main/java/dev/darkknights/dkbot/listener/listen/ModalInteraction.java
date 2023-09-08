package dev.darkknights.dkbot.listener.listen;

import dev.darkknights.WrongGuildException;
import dev.darkknights.dkbot.systems.SpendenSystem;
import dev.darkknights.dkbot.systems.VerificationSystem;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import org.json.simple.parser.ParseException;

public class ModalInteraction {
    public void onModalInteraction(ModalInteractionEvent event) {
        var modal = event.getModalId();

        try {
            switch (modal) {
                case "verify" -> new VerificationSystem().verifyModal(event);
                case "spenden" -> new SpendenSystem().spendenTicketCreateModal(event);
            }
        } catch (WrongGuildException e) {
            e.setLocation("dev.darkknights.listeners.EventListener.onModalInteraction");
            event.getHook().setEphemeral(true).sendMessage("Diese Interaction funktioniert nur auf dem DarkKnights Discord Server.").queue();
        } catch (NullPointerException | ParseException e) {
            e.printStackTrace();
            event.getHook().setEphemeral(true).sendMessage("Es gab einen Fehler beim Ausführen des Befehls.").queue();
        }
    }
}
