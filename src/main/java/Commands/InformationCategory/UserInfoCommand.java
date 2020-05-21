package Commands.InformationCategory;

import CommandListeners.CommandProperties;

import CommandSupporters.Command;
import Core.EmbedFactory;
import Core.Mention.MentionUtil;
import Core.TextManager;
import Core.Utils.TimeUtil;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;
import org.javacord.api.event.message.MessageCreateEvent;

import java.util.ArrayList;

@CommandProperties(
        trigger = "userinfo",
        emoji = "\uD83D\uDC81",
        executable = true,
        aliases = {"userinfos", "userstat", "userstats", "accountinfo", "whois"}
)
public class UserInfoCommand extends Command {

    @Override
    public boolean onMessageReceived(MessageCreateEvent event, String followedString) throws Throwable {
        boolean noMention = false;
        Server server = event.getServer().get();
        ArrayList<User> list = MentionUtil.getUsers(event.getMessage(), followedString).getList();
        if (list.size() > 5) {
            event.getChannel().sendMessage(EmbedFactory.getCommandEmbedError(this,
                    TextManager.getString(getLocale(),TextManager.GENERAL,"too_many_users"))).get();
            return false;
        } else if (list.size() == 0) {
            list.add(event.getMessage().getUserAuthor().get());
            noMention = true;
        }

        for(User user: list) {

            String[] type = getString("type").split("\n");
            int typeN = 0;
            if (!user.isBot()) {
                typeN = 1;
                if (server.getOwner().getId() == user.getId()) typeN = 2;
                if (user.isBotOwner()) typeN = 3;
            }

            String[] args = {
                    type[typeN],
                    user.getName(),
                    user.getNickname(server).isPresent() ? user.getNickname(server).get() : "-",
                    user.getDiscriminator(),
                    user.getIdAsString(),
                    user.getAvatar().getUrl().toString() + "?size=2048",
                    user.getJoinedAtTimestamp(server).isPresent() ? TimeUtil.getInstantString(getLocale(), user.getJoinedAtTimestamp(server).get(), true) : "-",
                    TimeUtil.getInstantString(getLocale(), user.getCreationTimestamp(), true),
                    TextManager.getString(getLocale(), TextManager.GENERAL, "status_" + user.getStatus().getStatusString())
            };

            EmbedBuilder eb = EmbedFactory.getCommandEmbedStandard(this, getString("template", args)).
                    setThumbnail(user.getAvatar().getUrl().toString());

            if (noMention) {
                eb.setFooter(TextManager.getString(getLocale(), TextManager.GENERAL, "mention_optional"));
                if (followedString.length() > 0)
                    EmbedFactory.addNoResultsLog(eb, getLocale(), followedString);
            }

            event.getServerTextChannel().get().sendMessage(eb).get();
        }
        return true;
    }

}
