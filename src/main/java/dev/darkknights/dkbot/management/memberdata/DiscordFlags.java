package dev.darkknights.dkbot.management.memberdata;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DiscordFlags {

    /**
     * Relevante Rollen wie Admin, Leader oder Event-Ausschluss
     */
    private List<String> roles = new ArrayList<>();
    private final List<String> allRoles = List.of("TeamM", "BotM", "DiscordM", "Admin", "Gründer", "Designer", "Leader", "Mod", "YT", "noEvent");

    /**
     * Betrag in Millionen Coins
     */
    private long spendenBetrag = 0;


    public String toJsonString() {

        return "{" +
                "\"spendenBetrag\":" + spendenBetrag + "," +
                "\"roles\":" + stringListToJsonString(roles) +
                "}";
    }


    private String stringListToJsonString(List<String> list) {
        var sb = new StringBuilder().append("[");
        for (String s : list) {
            sb.append("\"").append(s).append("\",");
        }
        if (sb.charAt(sb.length() - 1) != '[') {
            sb.insert(sb.length() - 1, "]");
        } else {
            sb.append("]");
        }
        //System.out.println(sb);
        return sb.toString();
    }


    public static DiscordFlags fromJson(JSONArray jsonA) {
        var json = (JSONObject) jsonA.get(0);

        var ret = new DiscordFlags();

        ret.setSpendenBetrag((long) json.get("spendenBetrag"));

        var roleArray = (JSONArray) json.get("roles");

        for (Object s : roleArray) {
            ret.addRole(s.toString());
        }

        return ret;
    }



    /**
     * Füge Role Flag hinzu
     *
     * @param role Mögliche Flags: "TeamM", "BotM", "DiscordM", "Admin", "Gründer", "Designer", "Leader", "Mod", "YT", "noEvent"
     */
    public void addRole(String role) {
        if (allRoles.contains(role) && !roles.contains(role)) {
            roles.add(role);
        }
    }

    /**
     * Entfernt Role Flag, wenn vorhanden. Sonst passiert nichts.
     *
     * @param role Mögliche Flags: "TeamM", "BotM", "DiscordM", "Admin", "Gründer", "Designer", "Leader", "Mod", "YT", "noEvent"
     */
    public void removeRole(String role) {
        roles.remove(role);
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public long getSpendenBetrag() {
        return spendenBetrag;
    }

    public void setSpendenBetrag(long spendenBetrag) {
        this.spendenBetrag = spendenBetrag;
    }
}
