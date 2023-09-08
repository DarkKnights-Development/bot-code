package dev.darkknights.dkbot.listener.listen;

import dev.darkknights.WrongGuildException;
import dev.darkknights.dkbot.systems.SpendenSystem;
import dev.darkknights.dkbot.systems.VerificationSystem;
import dev.darkknights.dkbot.systems.selfroles.ClassRoleSystem;
import dev.darkknights.dkbot.systems.selfroles.GameModeRoleSystem;
import dev.darkknights.dkbot.systems.selfroles.GildenRoleSystem;
import dev.darkknights.dkbot.systems.selfroles.PingRoleSystem;
import net.dv8tion.jda.api.entities.Invite;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

public class ButtonInteraction {
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String button = event.getComponentId();

        try {
            sw:
            switch (button) {
                case "verify" -> new VerificationSystem().verifyButton(event);
                case "verify-noMC" -> new VerificationSystem().noMCButton(event);
                case "verify-close" -> new VerificationSystem().verifyTicketCloseButton(event);
                case "removeC" -> new ClassRoleSystem().classRoleRemoveButton(event);
                case "removeG" -> new GildenRoleSystem().gildenRoleRemoveButton(event);
                case "removeP" -> new PingRoleSystem().pingRoleRemoveButton(event);
                case "spenden" -> new SpendenSystem().spendenTicketCreateButton(event);
                case "spenden-close" -> new SpendenSystem().spendenTicketCloseButton(event);
                case "removeGm" -> new GameModeRoleSystem().gameModeRoleRemoveButton(event);
                case "delInvite" -> {
                    event.deferReply().setEphemeral(true).queue();

                    var msg = event.getMessage().getEmbeds().get(0).getDescription();

                    //###########################################
                    //# Invite Code aus Embed bekommen
                    //###########################################
                    var index1 = msg.indexOf("https://discord.gg/");
                    var index2 = msg.indexOf("Invalid");
                    if (index2 == -1) {
                        index2 = msg.indexOf("Infinite Duration");
                    }
                    var length = (index2 - 1) - (index1 + 19);

                    //###########################################
                    //# ursprünglicher invite
                    //###########################################
                    var invite = new char[length];
                    msg.getChars(index1 + 19, index2 - 1, invite, 0);
                    var code = new String(invite);

                    //###########################################
                    //# alle aktiven Invites
                    //###########################################
                    var invList = event.getGuild().retrieveInvites().complete();

                    for (Invite inv : invList) {
                        if (inv.getCode().equals(code)) {
                            event.getHook().sendMessage("Invite wurde gelöscht.").queue();
                            event.editButton(event.getButton().asDisabled()).queue();
                            inv.delete().queue();
                            break sw;
                        }
                    }
                    event.getHook().sendMessage("Invite wurde nicht gefunden.").queue();
                }
                case "test" -> event.reply("nö").setEphemeral(true).queue();
            }
        } catch (WrongGuildException e) {
            e.setLocation("dev.darkknights.listeners.EventListener.onButtonInteraction");
            event.getHook().setEphemeral(true).sendMessage("Dieser Button funktioniert nur auf dem DarkKnights Discord Server.").queue();
        } catch (NullPointerException e) {
            event.getHook().setEphemeral(true).sendMessage("Es gab einen Fehler beim Ausführen des Befehls.").queue();
        }
    }
}
