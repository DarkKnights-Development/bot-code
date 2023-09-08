package dev.darkknights.dkbot;

import dev.darkknights.Bots;
import dev.darkknights.dkbot.cmds.CmdManager;
import dev.darkknights.dkbot.cmds.ContextMenus;
import dev.darkknights.dkbot.cmds.SlashCmd;
import dev.darkknights.dkbot.listener.EventListener;
import dev.darkknights.testbot.TestBot;
import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

import javax.security.auth.login.LoginException;

public class DarkKnightsBot {

    private static JDA dkJDA;

    public DarkKnightsBot() throws LoginException {
        Dotenv config = Bots.getConfig();

        //###########################################
        //# Bot Token from .env File
        //###########################################
        var token = config.get("TOKEN");

        var builder = JDABuilder.createDefault(token);

        //###########################################
        //# Bot Status
        //###########################################
        builder.setStatus(OnlineStatus.ONLINE);

        //###########################################
        //# Bot activity
        //###########################################
        builder.setActivity(Activity.playing("/help | watches everyone..."));

        //###########################################
        //# Intents die man nutzt
        //###########################################
        builder.enableIntents(
                GatewayIntent.GUILD_MEMBERS,
                GatewayIntent.GUILD_MESSAGES,
                GatewayIntent.GUILD_PRESENCES,
                GatewayIntent.MESSAGE_CONTENT,
                GatewayIntent.GUILD_VOICE_STATES
        );
        builder.disableIntents(GatewayIntent.AUTO_MODERATION_EXECUTION, GatewayIntent.AUTO_MODERATION_CONFIGURATION);

        //###########################################
        //# Cache
        //###########################################
        builder.setMemberCachePolicy(MemberCachePolicy.ALL);

        dkJDA = builder.build();

        //###########################################
        //# Register listeners
        //###########################################
        dkJDA.addEventListener(new EventListener(), new CmdManager(), new SlashCmd(), new ContextMenus());
    }


    public static JDA getJDA() {
        if (dkJDA == null) return TestBot.getTestJDA();
        return dkJDA;
    }

}
