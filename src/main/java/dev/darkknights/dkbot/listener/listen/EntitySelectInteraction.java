package dev.darkknights.dkbot.listener.listen;

import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent;

public class EntitySelectInteraction {
    public void onEntitySelectInteraction(EntitySelectInteractionEvent event) {
        var menu = event.getComponentId();

        try {
            switch (menu) {

            }
        } catch (NullPointerException e) {
            event.getHook().setEphemeral(true).sendMessage("Es gab einen Fehler beim Ausführen des Befehls.").queue();
        }
    }
}
