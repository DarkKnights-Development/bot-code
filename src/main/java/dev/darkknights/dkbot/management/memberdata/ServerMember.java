package dev.darkknights.dkbot.management.memberdata;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class ServerMember {

    private final long discordID;

    private DiscordFlags discordFlags;

    private String minecraftUsername;
    private String minecraftUUID;


    public ServerMember(long discordID, String minecraftUsername, String minecraftUUID, DiscordFlags discordFlags) {
        this.discordID = discordID;
        this.discordFlags = discordFlags;
        setMinecraftData(minecraftUsername, minecraftUUID);
    }
    public ServerMember(JSONObject data) {
        this.discordID = (long) data.get("discordID");

        this.minecraftUsername = (String) data.get("minecraftUsername");
        this.minecraftUUID = (String) data.get("minecraftUUID");

        this.discordFlags = DiscordFlags.fromJson((JSONArray) data.get("discordFlags"));
    }


    public JSONObject toJsonObject() throws ParseException {
        var parser = new JSONParser();

        var sb =
                "{" +
                "\"discordID\":" + discordID + "," +
                "\"minecraftUsername\":\"" + minecraftUsername + "\"," +
                "\"minecraftUUID\":\"" + minecraftUUID + "\"," +
                "\"discordFlags\":[" + discordFlags.toJsonString() + "]" +
                "}";
        //System.out.println(sb);
        return (JSONObject) parser.parse(sb);
    }

    public static ServerMember fromJsonObject(JSONObject data) {
        return new ServerMember(data);
    }


    public void setMinecraftData(String minecraftUsername, String minecraftUUID) {
        this.minecraftUUID = minecraftUUID;

        this.minecraftUsername = minecraftUsername;
    }


    public DiscordFlags getDiscordFlags() {
        return discordFlags;
    }

    public void setDiscordFlags(DiscordFlags discordFlags) {
        this.discordFlags = discordFlags;
    }

    public long getDiscordID() {
        return discordID;
    }

    public String getDiscordIDString() {
        return String.valueOf(discordID);
    }

    public String getMinecraftUsername() {
        return minecraftUsername;
    }

    public String getMinecraftUUID() {
        return minecraftUUID;
    }
}
