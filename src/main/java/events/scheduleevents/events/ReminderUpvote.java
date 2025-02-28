package events.scheduleevents.events;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Locale;
import commands.Category;
import constants.ExceptionRunnable;
import constants.ExternalLinks;
import core.CustomObservableMap;
import core.EmbedFactory;
import core.Program;
import core.TextManager;
import events.scheduleevents.ScheduleEventFixedRate;
import mysql.modules.subs.DBSubs;
import mysql.modules.subs.SubSlot;
import mysql.modules.upvotes.DBUpvotes;
import mysql.modules.upvotes.UpvoteSlot;
import mysql.modules.upvotes.UpvotesData;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;

@ScheduleEventFixedRate(rateValue = 1, rateUnit = ChronoUnit.MINUTES)
public class ReminderUpvote implements ExceptionRunnable {

    private boolean unlocked = true;

    @Override
    public void run() throws Throwable {
        if (Program.getClusterId() == 1 && unlocked) {
            unlocked = false;
            try {
                CustomObservableMap<Long, SubSlot> subMap = DBSubs.getInstance().retrieve(DBSubs.Command.CLAIM);
                UpvotesData upvotesData = DBUpvotes.getInstance().retrieve();
                CustomObservableMap<Long, UpvoteSlot> upvoteMap = upvotesData.getUpvoteMap();
                for (UpvoteSlot upvoteSlot : new ArrayList<>(upvoteMap.values())) {
                    if (Instant.now().isAfter(upvoteSlot.getLastUpdate().plus(12, ChronoUnit.HOURS)) &&
                            upvoteMap.containsKey(upvoteSlot.getUserId())
                    ) {
                        upvoteMap.remove(upvoteSlot.getUserId());
                        SubSlot sub = subMap.get(upvoteSlot.getUserId());
                        if (sub != null) {
                            Locale locale = sub.getLocale();
                            EmbedBuilder eb = EmbedFactory.getEmbedDefault()
                                    .setTitle(TextManager.getString(locale, Category.FISHERY, "claim_message_title"))
                                    .setDescription(TextManager.getString(locale, Category.FISHERY, "claim_message_desc"));
                            Button button = Button.of(ButtonStyle.LINK, ExternalLinks.UPVOTE_URL, TextManager.getString(locale, Category.FISHERY, "claim_message_button"));
                            sub.sendEmbed(locale, eb, button);
                        }
                    }
                }
            } finally {
                unlocked = true;
            }
        }
    }

}
