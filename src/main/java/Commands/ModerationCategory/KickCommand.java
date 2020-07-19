package Commands.ModerationCategory;

import CommandListeners.CommandProperties;
import Constants.Permission;
import Core.Utils.PermissionUtil;
import org.javacord.api.entity.user.User;
import org.javacord.api.entity.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;

@CommandProperties(
    trigger = "kick",
    botPermissions = Permission.KICK_MEMBERS,
    userPermissions = Permission.KICK_MEMBERS | Permission.BAN_MEMBERS,
    emoji = "\uD83D\uDEAA",
    executable = false
)
public class KickCommand extends WarnCommand  {

    private final static Logger LOGGER = LoggerFactory.getLogger(KickCommand.class);

    @Override
    public void process(Server server, User user) throws Throwable {
        try {
            server.kickUser(user, reason).get();
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error("Exception on kick", e);
            server.kickUser(user).get();
        }
    }

    @Override
    public boolean canProcess(Server server, User userStarter, User userAim) {
        return PermissionUtil.canYouKickUser(server, userAim) && server.canKickUser(userStarter, userAim);
    }

}
