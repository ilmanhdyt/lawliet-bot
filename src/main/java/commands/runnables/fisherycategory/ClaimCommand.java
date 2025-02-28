package commands.runnables.fisherycategory;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import commands.Command;
import commands.CommandEvent;
import commands.listeners.CommandProperties;
import commands.runnables.FisheryInterface;
import constants.Emojis;
import constants.ExternalLinks;
import constants.LogStatus;
import core.EmbedFactory;
import core.ExceptionLogger;
import core.utils.EmbedUtil;
import core.utils.StringUtil;
import modules.fishery.Fishery;
import mysql.modules.autoclaim.DBAutoClaim;
import mysql.modules.fisheryusers.DBFishery;
import mysql.modules.fisheryusers.FisheryMemberData;
import mysql.modules.upvotes.DBUpvotes;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.utils.TimeFormat;

@CommandProperties(
        trigger = "claim",
        botChannelPermissions = Permission.MESSAGE_EXT_EMOJI,
        emoji = "\uD83C\uDF80",
        executableWithoutArgs = true,
        onlyPublicVersion = true,
        usesExtEmotes = true,
        aliases = { "c" }
)
public class ClaimCommand extends Command implements FisheryInterface {

    public ClaimCommand(Locale locale, String prefix) {
        super(locale, prefix);
    }

    @Override
    public boolean onFisheryAccess(CommandEvent event, String args) {
        Instant nextUpvote = DBUpvotes.getInstance().retrieve().getLastUpvote(event.getMember().getIdLong()).plus(12, ChronoUnit.HOURS);
        FisheryMemberData userBean = DBFishery.getInstance().retrieve(event.getGuild().getIdLong()).getMemberData(event.getMember().getIdLong());
        int upvotesUnclaimed = userBean.getUpvoteStack();
        userBean.clearUpvoteStack();

        Button upvoteButton = Button.of(ButtonStyle.LINK, ExternalLinks.UPVOTE_URL, getString("button"));
        if (upvotesUnclaimed == 0) {
            EmbedBuilder eb;
            if (DBAutoClaim.getInstance().retrieve().isActive(event.getMember().getIdLong())) {
                eb = EmbedFactory.getEmbedDefault(this, getString("autoclaim"));
                EmbedUtil.addLog(eb, LogStatus.WARNING, getString("reminder"));
            } else {
                eb = EmbedFactory.getEmbedDefault(this, getString("nothing_description"))
                        .setColor(EmbedFactory.FAILED_EMBED_COLOR);
            }

            if (nextUpvote != null) addRemainingTimeNotification(eb, nextUpvote);
            setComponents(upvoteButton);
            drawMessageNew(eb).exceptionally(ExceptionLogger.get());
            return false;
        } else {
            long fishes = Fishery.getClaimValue(userBean);

            EmbedBuilder eb = EmbedFactory.getEmbedDefault(this, getString("claim", upvotesUnclaimed != 1, StringUtil.numToString(upvotesUnclaimed), StringUtil.numToString(Math.round(fishes * upvotesUnclaimed))));
            if (nextUpvote != null) addRemainingTimeNotification(eb, nextUpvote);
            EmbedUtil.addLog(eb, LogStatus.WARNING, getString("reminder"));

            MessageEmbed userChangeValueEmbed = userBean.changeValuesEmbed(event.getMember(), fishes * upvotesUnclaimed, 0).build();
            setComponents(upvoteButton);
            setAdditionalEmbeds(userChangeValueEmbed);
            drawMessageNew(eb).exceptionally(ExceptionLogger.get());
            return true;
        }
    }

    private void addRemainingTimeNotification(EmbedBuilder eb, Instant nextUpvote) {
        eb.addField(Emojis.ZERO_WIDTH_SPACE, getString("date", nextUpvote.isAfter(Instant.now()), TimeFormat.DATE_TIME_SHORT.atInstant(nextUpvote).toString()), false);
    }

}
