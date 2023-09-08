package dev.darkknights.dkbot.management.giveaways;

import dev.darkknights.config.Configs;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.awt.*;
import java.time.Instant;
import java.util.List;

public class Giveaway {
    boolean active;
    String beschreibung;
    Instant ende;
    long gewinnerZahl;

    long channelID;
    long messageID;
    long hostID;

    List<Long> rollenRequirements;
    List<Long> teilnehmerIDs;
    List<Long> gewinnerIDs;


    public Giveaway(long channelID, long gewinnerZahl, Instant ende, String beschreibung, List<Long> rollenRequirements, long hostID) {
        this.channelID = channelID;
        this.gewinnerZahl = gewinnerZahl;
        this.ende = ende;
        this.beschreibung = beschreibung;
        this.rollenRequirements = rollenRequirements;
        this.hostID = hostID;
    }


    public MessageEmbed createEmbedStart() {
        return new EmbedBuilder()
                .setFooter(Configs.devs.eposs.cmdDevText() + " | Banner by @storm8ng", Configs.devs.eposs.icon())
                .setTitle("🎉 **Neues Giveaway!** 🎉")
                .setColor(Color.BLUE)
                .setDescription(beschreibung)
                .addField("Verbleibende Zeit", "<t:" + ende.getEpochSecond() + ":R>", true)
                .addField("Anzahl Gewinner", String.valueOf(gewinnerZahl), true)
                .addField("Giveaway Host", "<@" + hostID + ">", true)
                .setImage(Configs.giveaways.giveawayBild)
                .build();
    }

    public MessageEmbed createEmbedEnd(boolean keineGewinner) {
        var gewinner = new StringBuilder();
        if (keineGewinner) {
            gewinner.append("Kein User erfüllt die Requirements. Keine Gewinner!");
        }
        else {
            gewinnerIDs.forEach(id -> gewinner.append("<@").append(id).append("> | "));
            gewinner.replace(gewinner.length() - 3, gewinner.length() - 1, "");
        }

        return new EmbedBuilder()
                .setFooter(Configs.devs.eposs.cmdDevText() + " | Banner by @storm8ng", Configs.devs.eposs.icon())
                .setTitle("Giveaway beendet!")
                .setColor(Color.CYAN)
                .setDescription(beschreibung)
                .addField("Verbleibende Zeit", "*beendet*", true)
                .addField("Anzahl Gewinner", String.valueOf(gewinnerZahl), true)
                .addField("Giveaway Host", "<@" + hostID + ">", true)
                .addField("Gewinner", gewinner.toString(), false)
                .setImage(Configs.giveaways.giveawayBild)
                .build();
    }


    public JSONObject toJson() throws ParseException {
        var jsonString = new StringBuilder().append("{")
                .append("\"channelID\":").append(channelID).append(",")
                .append("\"msgID\":").append(messageID).append(",")
                .append("\"hostID\":").append(hostID).append(",")
                .append("\"gewinnerZahl\":").append(gewinnerZahl).append(",")
                .append("\"ende\":\"").append(ende.toString()).append("\",")
                .append("\"beschreibung\":\"").append(beschreibung).append("\",")
                .append("\"active\":").append(active).append(",")
                .append("\"rollen\":[");

        rollenRequirements.forEach(role -> jsonString.append(role).append(","));
        // letztes ',' entfernen
        if (jsonString.charAt(jsonString.length()-1) == ',') jsonString.deleteCharAt(jsonString.length()-1);
        jsonString.append("],");

        if (gewinnerIDs != null) {
            jsonString.append("\"gewinnerIDs\":[");
            gewinnerIDs.forEach(id -> jsonString.append(id).append(","));
            // letztes ',' entfernen
            if (jsonString.charAt(jsonString.length()-1) == ',') jsonString.deleteCharAt(jsonString.length()-1);
            jsonString.append("],");
        }

        if (teilnehmerIDs != null) {
            jsonString.append("\"teilnehmerIDs\":[");
            teilnehmerIDs.forEach(id -> jsonString.append(id).append(","));
            // letztes ',' entfernen
            if (jsonString.charAt(jsonString.length()-1) == ',') jsonString.deleteCharAt(jsonString.length()-1);
            jsonString.append("],");
        }

        // letztes ',' entfernen
        if (jsonString.charAt(jsonString.length()-1) == ',') jsonString.deleteCharAt(jsonString.length()-1);

        jsonString.append("}");

        return (JSONObject) new JSONParser().parse(jsonString.toString());
    }

    public static Giveaway fromJson(JSONObject json) {
        var temp = new Giveaway(
                (Long) json.get("channelID"),
                (Long) json.get("gewinnerZahl"),
                Instant.parse(json.get("ende").toString()),
                json.get("beschreibung").toString(),
                (List<Long>) json.get("rollen"),
                (Long) json.get("hostID")
        );

        temp.setActive((Boolean) json.get("active"));
        temp.setMessageID((Long) json.get("msgID"));

        var gewinnerIDs = json.get("gewinnerIDs");
        if (gewinnerIDs != null) temp.setGewinnerIDs((List<Long>) gewinnerIDs);

        var teilnehmerIDs = json.get("teilnehmerIDs");
        if (teilnehmerIDs != null) temp.setTeilnehmerIDs((List<Long>) teilnehmerIDs);

        return temp;
    }


    public long getChannelID() {
        return channelID;
    }

    public long getGewinnerZahl() {
        return gewinnerZahl;
    }

    public Instant getEnde() {
        return ende;
    }

    public String getBeschreibung() {
        return beschreibung;
    }

    public List<Long> getRollenRequirements() {
        return rollenRequirements;
    }

    public long getMessageID() {
        return messageID;
    }

    public void setMessageID(long messageID) {
        this.messageID = messageID;
    }

    public List<Long> getGewinnerIDs() {
        return gewinnerIDs;
    }

    public void setGewinnerIDs(List<Long> gewinnerIDs) {
        this.gewinnerIDs = gewinnerIDs;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public long getHostID() {
        return hostID;
    }

    public List<Long> getTeilnehmerIDs() {
        return teilnehmerIDs;
    }

    public void setTeilnehmerIDs(List<Long> teilnehmerIDs) {
        this.teilnehmerIDs = teilnehmerIDs;
    }
}
