package events.discordevents.userroleremove;

import constants.AssetIds;
import constants.Settings;
import core.DiscordApiManager;
import core.MainLogger;
import core.utils.StringUtil;
import events.discordevents.DiscordEvent;
import events.discordevents.eventtypeabstracts.UserRoleRemoveAbstract;
import org.javacord.api.event.server.role.UserRoleRemoveEvent;
import org.javacord.api.util.logging.ExceptionLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@DiscordEvent
public class UserRoleRemovePatreonRole extends UserRoleRemoveAbstract {

    @Override
    public boolean onUserRoleRemove(UserRoleRemoveEvent event) throws Throwable {
        if (event.getServer().getId() == AssetIds.SUPPORT_SERVER_ID) {
            for(long roleId : Settings.PATREON_ROLE_IDS) {
                if (event.getRole().getId() == roleId) {
                    MainLogger.get().info("PATREON LEFT {} ({})", event.getUser().getDiscriminatedName(), event.getUser().getId());
                    DiscordApiManager.getInstance().fetchOwner().get().sendMessage("PATREON USER LEFT: " + StringUtil.escapeMarkdown(event.getUser().getDiscriminatedName())).exceptionally(ExceptionLogger.get());
                    break;
                }
            }
        }

        return true;
    }

}
