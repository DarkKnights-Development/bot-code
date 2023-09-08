package dev.darkknights.dkbot.cmds.slash;

import dev.darkknights.Logging;
import dev.darkknights.config.Configs;
import dev.darkknights.dkbot.management.APIRequests;
import dev.darkknights.dkbot.management.HypixelApiManagement;
import dev.darkknights.dkbot.management.memberdata.DataManagement;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.InteractionHook;
import org.json.simple.parser.ParseException;

import java.util.Map;

public class link {
    public void cmd(Member member, String ign, InteractionHook hook) throws ParseException {
        var user = member.getUser();

        new Logging().logCmd("link", user.getName(), Map.of("ign", ign));

        try {
            var apiDc = new HypixelApiManagement().getDiscord(new APIRequests().getUUID(ign)); //APIRequests.getDC(APIRequests.getUUID(ign));

            var dc = user.getName();
            if (!user.getAsTag().contains("#0")) dc = user.getAsTag();

            if (dc.equals(apiDc)) {
                new DataManagement().storeDataCmd(member, ign, hook);
            } else {
                hook.sendMessageEmbeds(Configs.dcNameErrorMsg(dc)).queue();
            }
        } catch (NullPointerException e) {
            var dc = user.getName();
            if (!user.getAsTag().contains("#0")) dc = user.getAsTag();
            hook.sendMessageEmbeds(Configs.dcNameErrorMsg(dc)).queue();
        }
    }
}
