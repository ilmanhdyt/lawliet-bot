package core;

import java.time.Instant;
import core.utils.BotUtil;

public class Program {

    private static boolean stopped = false;
    private final static Instant startTime = Instant.now();

    public static void init() {
        System.out.println("-------------------------------------");
        System.out.println("Production Mode: " + productionMode());
        System.out.println("Cluster ID: " + getClusterId());
        System.out.println("Version: " + BotUtil.getCurrentVersion());
        System.out.println("-------------------------------------");
    }

    public static void onStop() {
        MainLogger.get().info("### STOPPING BOT ###\n{}\nThreads: {}", Console.getMemory(), Thread.getAllStackTraces().size());
        stopped = true;
        ShardManager.stop();
    }

    public static boolean productionMode() {
        return System.getenv("PRODUCTION").equals("true");
    }

    public static boolean isRunning() {
        return !stopped;
    }

    public static boolean publicVersion() {
        return true;
    }

    public static int getClusterId() {
        return Integer.parseInt(System.getenv("CLUSTER"));
    }

    public static Instant getStartTime() {
        return startTime;
    }

}