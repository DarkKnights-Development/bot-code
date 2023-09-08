package dev.darkknights.dkbot.management.memberdata;

import dev.darkknights.config.Configs;
import dev.darkknights.dkbot.management.APIRequests;
import dev.darkknights.dkbot.management.database.MemberDataBase;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import org.json.simple.parser.ParseException;

public class DataManagement {
    private static final MemberDataBase mdb = new MemberDataBase();

    public void restoreFlagState(ModalInteractionEvent event) throws ParseException {
        var dk = event.getGuild();
        var user = event.getUser();

        var sm = new ServerMember(mdb.getFromDatabase(user.getId()));
        var flags = sm.getDiscordFlags();

        // Rollen verteilen
        for (String role : flags.getRoles()) {
            switch (role) {
                case "Gründer" -> dk.addRoleToMember(user, dk.getRoleById(Configs.rollenIDs.general.founder)).queue();
                case "Designer" -> dk.addRoleToMember(user, dk.getRoleById(Configs.rollenIDs.general.designer)).queue();
                case "Leader" -> dk.addRoleToMember(user, dk.getRoleById(Configs.rollenIDs.general.leaderIG)).queue();
                case "Mod" -> dk.addRoleToMember(user, dk.getRoleById(Configs.rollenIDs.general.modIG)).queue();
                //case "YT" -> dk.addRoleToMember(user, dk.getRoleById(Configs.rollenIDs.general.yt)).queue();
                case "noEvent" -> dk.addRoleToMember(user, dk.getRoleById(Configs.rollenIDs.general.noEventRole)).queue();
            }
        }
    }


    public void readAndStoreMemberData(ModalInteractionEvent event) {
        var member = event.getMember();

        var flags = new DiscordFlags();

        // Rollen feststellen
        flags.setRoles(mdb.detectRoles(member));

        // Flags setzen
        var ign = event.getValue("ign").getAsString();
        var uuid = new APIRequests().getUUID(ign);

        var sm = new ServerMember(member.getIdLong(), ign, uuid, flags);
        try {
            var smJson = sm.toJsonObject();

            // In Database speichern
            mdb.saveToDatabase(smJson);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public void storeDataCmd(Member member, String ign, InteractionHook hook) {
        //var member = event.getMember();

        var flags = new DiscordFlags();

        // Rollen feststellen
        flags.setRoles(mdb.detectRoles(member));

        // Flags setzen
        var uuid = new APIRequests().getUUID(ign);

        var sm = new ServerMember(member.getIdLong(), ign, uuid, flags);
        try {
            var smJson = sm.toJsonObject();

            // In Database speichern
            mdb.saveToDatabase(smJson);

            hook.sendMessage("Account verknüpft und Daten gespeichert.").queue();
        } catch (ParseException e) {
            e.printStackTrace();
            hook.sendMessage("Es gab einen Fehler beim speichern. Probiere es später noch einmal.").queue();
        }
    }
}
