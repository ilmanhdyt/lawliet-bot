package core;

import java.util.EnumSet;
import java.util.List;
import javax.security.auth.login.LoginException;
import commands.SlashCommandManager;
import constants.AssetIds;
import core.utils.StringUtil;
import events.discordevents.DiscordEventAdapter;
import events.scheduleevents.ScheduleEventManager;
import modules.BumpReminder;
import modules.SupportTemplates;
import modules.repair.MainRepair;
import modules.schedulers.*;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import net.dv8tion.jda.api.utils.AllowedMentions;
import net.dv8tion.jda.api.utils.ConcurrentSessionController;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.dv8tion.jda.internal.utils.IOUtil;

public class DiscordConnector {

    private static boolean started = false;
    private static final ConcurrentSessionController concurrentSessionController = new ConcurrentSessionController();

    private static final JDABuilder jdaBuilder = JDABuilder.createDefault(System.getenv("BOT_TOKEN"))
            .setSessionController(concurrentSessionController)
            .setMemberCachePolicy(MemberCacheController.getInstance())
            .setChunkingFilter(new ChunkingFilterController())
            .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_PRESENCES, GatewayIntent.GUILD_MESSAGE_TYPING)
            .enableCache(CacheFlag.ACTIVITY)
            .disableCache(CacheFlag.ROLE_TAGS)
            .setActivity(Activity.watching(getActivityText()))
            .setHttpClient(IOUtil.newHttpClientBuilder().addInterceptor(new CustomInterceptor()).build())
            .addEventListeners(new DiscordEventAdapter());

    static {
        concurrentSessionController.setConcurrency(Integer.parseInt(System.getenv("CONCURRENCY")));
        ShardManager.addShardDisconnectConsumer(DiscordConnector::reconnectApi);
    }

    public static void connect(int shardMin, int shardMax, int totalShards) {
        if (started) return;
        started = true;

        MainLogger.get().info("Bot is logging in...");
        ShardManager.init(shardMin, shardMax, totalShards);
        EnumSet<Message.MentionType> deny = EnumSet.of(Message.MentionType.EVERYONE, Message.MentionType.HERE, Message.MentionType.ROLE);
        MessageAction.setDefaultMentions(EnumSet.complementOf(deny));
        MessageAction.setDefaultMentionRepliedUser(false);
        AllowedMentions.setDefaultMentionRepliedUser(false);

        new Thread(() -> {
            for (int i = shardMin; i <= shardMax; i++) {
                try {
                    jdaBuilder.useSharding(i, totalShards)
                            .build();
                } catch (LoginException e) {
                    MainLogger.get().error("EXIT - Invalid token", e);
                    System.exit(2);
                }
            }
        }, "Shard-Starter").start();
    }

    public static void reconnectApi(int shardId) {
        MainLogger.get().info("Shard {} is getting reconnected...", shardId);

        try {
            jdaBuilder.useSharding(shardId, ShardManager.getTotalShards())
                    .build();
        } catch (LoginException e) {
            MainLogger.get().error("EXIT - Invalid token", e);
            System.exit(3);
        }
    }

    public static void onJDAJoin(JDA jda) {
        ShardManager.addJDA(jda);
        MainLogger.get().info("Shard {} connection established", jda.getShardInfo().getShardId());

        checkConnectionCompleted(jda);
        if (Program.productionMode()) {
            MainRepair.start(jda, 20);
        }
    }

    private synchronized static void checkConnectionCompleted(JDA jda) {
        if (ShardManager.isEverythingConnected() && !ShardManager.isReady()) {
            onConnectionCompleted(jda);
        }
    }

    private synchronized static void onConnectionCompleted(JDA jda) {
        new ScheduleEventManager().start();
        if (Program.productionMode() && Program.publicVersion()) {
            BumpReminder.start();
        }
        AlertScheduler.start();
        ReminderScheduler.start();
        GiveawayScheduler.start();
        TempBanScheduler.start();
        ServerMuteScheduler.start();
        JailScheduler.start();
        ShardManager.start();
        MainLogger.get().info("### ALL SHARDS CONNECTED SUCCESSFULLY! ###");

        try {
            List<CommandData> commandDataList = SlashCommandManager.initialize();
            if (Program.productionMode()) {
                if (Program.isNewVersion()) {
                    MainLogger.get().info("Pushing new slash commands");
                    jda.updateCommands()
                            .addCommands(commandDataList)
                            .queue();
                } else {
                    MainLogger.get().info("Skipping slash commands because it's not a new version");
                }
            } else {
                ShardManager.getLocalGuildById(AssetIds.BETA_SERVER_ID).get()
                        .updateCommands()
                        .addCommands(commandDataList)
                        .addCommands(SupportTemplates.generateSupportContextCommands())
                        .queue();
            }
        } catch (Throwable e) {
            MainLogger.get().error("Exception on slash commands load", e);
        }

        Guild guild = jda.getGuildById(AssetIds.SUPPORT_SERVER_ID);
        if (guild != null) {
            guild.updateCommands()
                    .addCommands(SupportTemplates.generateSupportContextCommands())
                    .queue();
        }
    }

    public static void updateActivity(JDA jda) {
        jda.getPresence().setActivity(Activity.watching(getActivityText()));
    }

    private static String getActivityText() {
        return ShardManager.getGlobalGuildSize()
                .map(globalGuildSize -> "L.help | " + StringUtil.numToString(globalGuildSize) + " | www.lawlietbot.xyz")
                .orElse("L.help | www.lawlietbot.xyz");
    }

    public static boolean hasStarted() {
        return started;
    }

}