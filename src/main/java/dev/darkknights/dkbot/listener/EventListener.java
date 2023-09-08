package dev.darkknights.dkbot.listener;

import dev.darkknights.dkbot.listener.listen.*;
import net.dv8tion.jda.api.events.guild.invite.GuildInviteCreateEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.EntitySelectInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class EventListener extends ListenerAdapter {

    //###########################################
    //# Buttons
    //###########################################
    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        new ButtonInteraction().onButtonInteraction(event);
    }

    //###########################################
    //# Msg Received
    //###########################################
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        new MessageReceived().onMessageReceived(event);
    }

    //###########################################
    //# Popup Fenster
    //###########################################
    @Override
    public void onModalInteraction(ModalInteractionEvent event) {
        new ModalInteraction().onModalInteraction(event);
    }

    //###########################################
    //# String Dropdown Menus
    //###########################################
    @Override
    public void onStringSelectInteraction(StringSelectInteractionEvent event) {
        new StringSelectInteraction().onStringSelectInteraction(event);
    }

    //###########################################
    //# Guild Invite Creation
    //###########################################
    @Override
    public void onGuildInviteCreate(GuildInviteCreateEvent event) {
        new GuildInviteCreate().onGuildInviteCreate(event);
    }

    //###########################################
    //# Kickt Member aus Stat Voice Channeln
    //###########################################
    @Override
    public void onGuildVoiceUpdate(GuildVoiceUpdateEvent event) {
        new GuildVoiceUpdate().onGuildVoiceUpdate(event);
    }

    //###########################################
    //# Role/Channel/User Dropdown Menu
    //###########################################
    @Override
    public void onEntitySelectInteraction(EntitySelectInteractionEvent event) {
        new EntitySelectInteraction().onEntitySelectInteraction(event);
    }
}
