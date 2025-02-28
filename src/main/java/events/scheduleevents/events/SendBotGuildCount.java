package events.scheduleevents.events;


import java.time.temporal.ChronoUnit;
import constants.ExceptionRunnable;
import core.Program;
import core.ShardManager;
import core.botlists.*;
import events.scheduleevents.ScheduleEventFixedRate;

@ScheduleEventFixedRate(rateValue = 5, rateUnit = ChronoUnit.MINUTES)
public class SendBotGuildCount implements ExceptionRunnable {

    @Override
    public void run() throws Throwable {
        if (Program.productionMode() && Program.publicVersion() && Program.getClusterId() == 1) {
            ShardManager.getGlobalGuildSize().ifPresent(totalServers -> {
                TopGG.updateServerCount(totalServers);
                Botsfordiscord.updateServerCount(totalServers);
                BotsOnDiscord.updateServerCount(totalServers);
                Discordbotlist.updateServerCount(totalServers);
                Discordbotsgg.updateServerCount(totalServers);
            });
        }
    }

}