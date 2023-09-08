package dev.darkknights.dkbot.systems.selfroles;

import dev.darkknights.WrongGuildException;
import dev.darkknights.config.Configs;
import dev.darkknights.dkbot.management.HypixelApiManagement;
import dev.darkknights.dkbot.management.database.MemberDataBase;
import dev.darkknights.dkbot.management.memberdata.ServerMember;
import dev.darkknights.dkbot.systems.SpendenSystem;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.InteractionHook;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import java.awt.*;
import java.net.MalformedURLException;
import java.util.List;

public class SbRoleSystem {

    //###########################################
    //# Skyblock Profile Rollen Verwaltung
    //###########################################
    public void sbRolesCmd(String roleOption, TextChannel support, User user, Guild dk, InteractionHook hook, int type) throws WrongGuildException, ParseException, MalformedURLException { //type: 1 == add | 0 == remove
/*
        var roleOption = event.getOption("roles").getAsString().toLowerCase();

        var support = event.getGuild().getTextChannelById(Configs.channelIDs.supportTicketID);

        var user = event.getUser();
        var dk = event.getGuild();
*/
        //type: 1 == add | 0 == remove
        switch (type) {
            case 0 -> {
                switch (roleOption) {
                    default ->
                            hook.sendMessage(roleOption + " ist keine gültige Option. Bitte wähle eine der vorgeschlagenen Optionen.").queue();
                    case "all" -> {
                        removeCataRoles(user, dk);
                        removeSbLvlRoles(user, dk);
                        hook.sendMessage("Alle **Skyblock Profile Rollen** wurden entfernt.").queue();
                    }
                    case "catacombs" -> {
                        removeCataRoles(user, dk);
                        hook.sendMessage("Deine **Catacombs Level Rolle** wurde entfernt.").queue();
                    }
                    case "sblevel" -> {
                        removeSbLvlRoles(user, dk);
                        hook.sendMessage("Deine **Skyblock Level Rolle** wurde entfernt.").queue();
                    }
                }
            }
            case 1 -> {
                var userID = user.getId();

                var mdb = new MemberDataBase();

                // Ign <--> Dc Name Check
                if (mdb.hasData(userID)) {
                    var sm = new ServerMember(mdb.getFromDatabase(userID));
                    var UUID = sm.getMinecraftUUID();

                    var selectedProfile = new HypixelApiManagement().getSelectedProfile(UUID);

                    //type: 1 == add | 0 == remove
                    switch (roleOption) {
                        default ->
                                hook.sendMessage(roleOption + " ist keine gültige Option. Bitte wähle eine der vorgeschlagenen Optionen.").queue();
                        case "all" -> {
                            boolean cata = addCataRoles(user, dk, UUID, selectedProfile);
                            boolean sb = addSbLvlRoles(user, dk, UUID, selectedProfile);

                            if (cata && sb) {
                                hook.sendMessage("Du hast alle **Skyblock Profile Rollen** erhalten.").queue();
                            } else if (!cata && !sb) {
                                hook.sendMessage("Es konnten keine **Skyblock Profile Rollen** festgestellt werden." +
                                        "\nVersuche es mit den einzelnen Befehlen nochmal." +
                                        "\nFalls das ein Fehler ist, öffne bitte ein Ticket in " + support.getAsMention() + " ."
                                ).queue();
                            }
                        }
                        case "catacombs" -> {
                            if (addCataRoles(user, dk, UUID, selectedProfile))
                                hook.sendMessage("Du hast deine **Catacombs Level Rolle** erhalten.").queue();
                            else hook.sendMessage("Es konnte kein **Catacombs Level** festgestellt werden." +
                                    "\nDas passiert, wenn du noch kein Dungeons gespielt hast." +
                                    "\nFalls das ein Fehler ist, öffne bitte ein Ticket in " + support.getAsMention() + " ."
                            ).queue();
                        }
                        case "sblevel" -> {
                            if (addSbLvlRoles(user, dk, UUID, selectedProfile))
                                hook.sendMessage("Du hast deine **Skyblock Level Rolle** erhalten.").queue();
                            else hook.sendMessage("Es konnte kein **Skyblock Level** festgestellt werden." +
                                    "\nDas passiert, wenn du noch fast kein Skyblock gespielt hast." +
                                    "\nFalls das ein Fehler ist, öffne bitte ein Ticket in " + support.getAsMention() + " ."
                            ).queue();
                        }
                    }
                } else {
                    var eb = new EmbedBuilder()
                            .setColor(Color.RED)
                            .setFooter(Configs.devs.eposs.cmdDevText(), Configs.devs.eposs.icon())
                            .setDescription("Du hast deinen Discord Account noch nicht mit deinem Minecraft Account verknüpft." +
                                    "\nNutze </link:1113182819393478657> um deine Accounts zu verknüpfen und versuche es dann erneut.");
                    hook.sendMessageEmbeds(eb.build()).queue();
                }
            }
        }
    }


    //###########################################
    //# add Cata Rollen
    //###########################################
    public boolean addCataRoles(User user, Guild guild, String UUID, JSONObject profile) {
        int cataLvl; //APIRequests.getCataLvl(UUID);
        try {
            cataLvl = new HypixelApiManagement().getCataLvl(UUID, profile);
        } catch (NullPointerException e) {
            cataLvl = -1;
        }

        switch (cataLvl) {
            case -1:
                return false;
            case 0:
                var cata_0 = guild.getRoleById(Configs.rollenIDs.cataLevel.c0);
                if (hasNotRole(user, cata_0, "cata", guild)) guild.addRoleToMember(user, cata_0).queue();
                break;
            case 1:
                var cata_10 = guild.getRoleById(Configs.rollenIDs.cataLevel.c10);
                if (hasNotRole(user, cata_10, "cata", guild)) guild.addRoleToMember(user, cata_10).queue();
                break;
            case 2:
                var cata_20 = guild.getRoleById(Configs.rollenIDs.cataLevel.c20);
                if (hasNotRole(user, cata_20, "cata", guild)) guild.addRoleToMember(user, cata_20).queue();
                break;
            case 3:
                var cata_30 = guild.getRoleById(Configs.rollenIDs.cataLevel.c30);
                if (hasNotRole(user, cata_30, "cata", guild)) guild.addRoleToMember(user, cata_30).queue();
                break;
            case 4:
                var cata_40 = guild.getRoleById(Configs.rollenIDs.cataLevel.c40);
                if (hasNotRole(user, cata_40, "cata", guild)) guild.addRoleToMember(user, cata_40).queue();
                break;
            case 5:
                var cata_50 = guild.getRoleById(Configs.rollenIDs.cataLevel.c50);
                if (hasNotRole(user, cata_50, "cata", guild)) guild.addRoleToMember(user, cata_50).queue();
                break;
        }
        return true;
    }


    //###########################################
    //# remove Cata Rollen
    //###########################################
    private void removeCataRoles(User user, Guild guild) {
        var cataRoles = List.of(
                guild.getRoleById(Configs.rollenIDs.cataLevel.c0),
                guild.getRoleById(Configs.rollenIDs.cataLevel.c10),
                guild.getRoleById(Configs.rollenIDs.cataLevel.c20),
                guild.getRoleById(Configs.rollenIDs.cataLevel.c30),
                guild.getRoleById(Configs.rollenIDs.cataLevel.c40),
                guild.getRoleById(Configs.rollenIDs.cataLevel.c50)
        );

        for (Role cata : cataRoles) {
            guild.removeRoleFromMember(user, cata).complete();
        }
    }


    //###########################################
    //# add Sb Lvl Rollen
    //###########################################
    public boolean addSbLvlRoles(User user, Guild guild, String UUID, JSONObject profile) {
        int sbLvl; //APIRequests.getSbLvl(UUID);
        try {
            sbLvl = new HypixelApiManagement().getSbLvlExp(UUID, profile);
        } catch (NullPointerException e) {
            sbLvl = -1;
        }

        if (sbLvl == -1) return false;
        else if (sbLvl < 40) {
            var lvl_0 = guild.getRoleById(Configs.rollenIDs.skyBlockLevel.ab0);
            if(hasNotRole(user, lvl_0, "sbLvl", guild)) guild.addRoleToMember(user, lvl_0).queue();
        } else if (sbLvl < 80) {
            var lvl_40 = guild.getRoleById(Configs.rollenIDs.skyBlockLevel.ab40);
            if(hasNotRole(user, lvl_40, "sbLvl", guild)) guild.addRoleToMember(user, lvl_40).queue();
        } else if (sbLvl < 120) {
            var lvl_80 = guild.getRoleById(Configs.rollenIDs.skyBlockLevel.ab80);
            if(hasNotRole(user, lvl_80, "sbLvl", guild)) guild.addRoleToMember(user, lvl_80).queue();
        } else if (sbLvl < 160) {
            var lvl_120 = guild.getRoleById(Configs.rollenIDs.skyBlockLevel.ab120);
            if(hasNotRole(user, lvl_120, "sbLvl", guild)) guild.addRoleToMember(user, lvl_120).queue();
        } else if (sbLvl < 200) {
            var lvl_160 = guild.getRoleById(Configs.rollenIDs.skyBlockLevel.ab160);
            if(hasNotRole(user, lvl_160, "sbLvl", guild)) guild.addRoleToMember(user, lvl_160).queue();
        } else if (sbLvl < 240) {
            var lvl_200 = guild.getRoleById(Configs.rollenIDs.skyBlockLevel.ab200);
            if(hasNotRole(user, lvl_200, "sbLvl", guild)) guild.addRoleToMember(user, lvl_200).queue();
        } else if (sbLvl < 280) {
            var lvl_240 = guild.getRoleById(Configs.rollenIDs.skyBlockLevel.ab240);
            if(hasNotRole(user, lvl_240, "sbLvl", guild)) guild.addRoleToMember(user, lvl_240).queue();
        } else if (sbLvl < 320) {
            var lvl_280 = guild.getRoleById(Configs.rollenIDs.skyBlockLevel.ab280);
            if(hasNotRole(user, lvl_280, "sbLvl", guild)) guild.addRoleToMember(user, lvl_280).queue();
        } else if (sbLvl < 360) {
            var lvl_320 = guild.getRoleById(Configs.rollenIDs.skyBlockLevel.ab320);
            if(hasNotRole(user, lvl_320, "sbLvl", guild)) guild.addRoleToMember(user, lvl_320).queue();
        } else if (sbLvl < 400) {
            var lvl_360 = guild.getRoleById(Configs.rollenIDs.skyBlockLevel.ab360);
            if(hasNotRole(user, lvl_360, "sbLvl", guild)) guild.addRoleToMember(user, lvl_360).queue();
        } else if (sbLvl < 440) {
            var lvl_400 = guild.getRoleById(Configs.rollenIDs.skyBlockLevel.ab400);
            if(hasNotRole(user, lvl_400, "sbLvl", guild)) guild.addRoleToMember(user, lvl_400).queue();
        } else if (sbLvl < 480) {
            var lvl_440 = guild.getRoleById(Configs.rollenIDs.skyBlockLevel.ab440);
            if(hasNotRole(user, lvl_440, "sbLvl", guild)) guild.addRoleToMember(user, lvl_440).queue();
        } else if (sbLvl > 480) {
            var lvl_480 = guild.getRoleById(Configs.rollenIDs.skyBlockLevel.ab480);
            if(hasNotRole(user, lvl_480, "sbLvl", guild)) guild.addRoleToMember(user, lvl_480).queue();
        }
        return true;
    }


    //###########################################
    //# remove SbLvl Rollen
    //###########################################
    private void removeSbLvlRoles(User user, Guild guild) {
        var lvlRoles = List.of(
                guild.getRoleById(Configs.rollenIDs.skyBlockLevel.ab0),
                guild.getRoleById(Configs.rollenIDs.skyBlockLevel.ab40),
                guild.getRoleById(Configs.rollenIDs.skyBlockLevel.ab80),
                guild.getRoleById(Configs.rollenIDs.skyBlockLevel.ab120),
                guild.getRoleById(Configs.rollenIDs.skyBlockLevel.ab160),
                guild.getRoleById(Configs.rollenIDs.skyBlockLevel.ab200),
                guild.getRoleById(Configs.rollenIDs.skyBlockLevel.ab240),
                guild.getRoleById(Configs.rollenIDs.skyBlockLevel.ab280),
                guild.getRoleById(Configs.rollenIDs.skyBlockLevel.ab320),
                guild.getRoleById(Configs.rollenIDs.skyBlockLevel.ab360),
                guild.getRoleById(Configs.rollenIDs.skyBlockLevel.ab400),
                guild.getRoleById(Configs.rollenIDs.skyBlockLevel.ab440),
                guild.getRoleById(Configs.rollenIDs.skyBlockLevel.ab480)
        );

        for (Role lvl : lvlRoles) {
            guild.removeRoleFromMember(user, lvl).complete();
        }
    }


    /**
     * Check if Member has Role equal to Level
     * @param user Member
     * @param role Role to add
     * @param flag cata, sbLvl, spende
     * @param guild Guild
     * @return false if Member has Role, else removes all Roles of flag type and returns true
     */
    public boolean hasNotRole(User user, Role role, String flag, Guild guild) {
        if (guild.getMemberById(user.getId()).getRoles().contains(role)) return false;
        else {
            switch (flag) {
                case "cata" -> removeCataRoles(user, guild);
                case "sbLvl" -> removeSbLvlRoles(user, guild);
                case "spende" -> new SpendenSystem().removeSpendenRollen(user, guild);
            }
            return true;
        }
    }
}
