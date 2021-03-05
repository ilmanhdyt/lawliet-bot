package modules.repair;

import commands.runnables.utilitycategory.AutoChannelCommand;
import constants.PermissionDeprecated;
import core.DiscordApiManager;
import core.MainLogger;
import core.PermissionCheckRuntime;
import mysql.modules.autochannel.AutoChannelBean;
import mysql.modules.autochannel.DBAutoChannel;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.ISnowflake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AutoChannelRepair {

    private static final AutoChannelRepair ourInstance = new AutoChannelRepair();

    public static AutoChannelRepair getInstance() {
        return ourInstance;
    }

    private AutoChannelRepair() {
    }

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public void start(JDA jda) {
        executorService.submit(() -> run(jda));
    }

    public void run(JDA jda) {
        try {
            DBAutoChannel.getInstance().getAllChildChannelServerIds().stream()
                    .filter(serverId -> DiscordApiManager.getInstance().getResponsibleShard(serverId) == jda.getShardInfo().getShardId())
                    .map(jda::getGuildById)
                    .filter(Objects::nonNull)
                    .forEach(this::deleteEmptyVoiceChannels);
        } catch (SQLException e) {
            MainLogger.get().error("Error in auto channel synchronization");
        }
    }

    private void deleteEmptyVoiceChannels(Guild guild) {
        AutoChannelBean autoChannelBean = DBAutoChannel.getInstance().getBean(guild.getIdLong());
        autoChannelBean.getChildChannelIds().transform(guild::getVoiceChannelById, ISnowflake::getIdLong).stream()
                .filter(vc -> vc.getMembers().isEmpty() && PermissionCheckRuntime.getInstance().botHasPermission(autoChannelBean.getServerBean().getLocale(), AutoChannelCommand.class, vc, Permission.MANAGE_CHANNEL, Permission.VOICE_CONNECT))
                .forEach(vc -> vc.delete().reason("Auto Channel").queue()); //no log
    }

}
