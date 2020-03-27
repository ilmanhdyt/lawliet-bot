package Commands.ExternalCategory;

import CommandListeners.*;
import CommandSupporters.Command;
import Constants.LogStatus;
import General.*;
import General.PostBundle;
import General.Reddit.RedditDownloader;
import General.Reddit.RedditPost;
import General.Tracker.TrackerData;
import org.javacord.api.entity.channel.ServerTextChannel;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;

import java.time.Instant;

@CommandProperties(
    trigger = "reddit",
    withLoadingBar = true,
    emoji = "\uD83E\uDD16",
    executable = false
)
public class RedditCommand extends Command implements onRecievedListener, onTrackerRequestListener {

    public RedditCommand() {
        super();
    }

    @Override
    public boolean onReceived(MessageCreateEvent event, String followedString) throws Throwable {
        followedString = Tools.cutSpaces(followedString);
        if (followedString.startsWith("r/")) followedString = followedString.substring(2);

        if (followedString.length() == 0) {
            event.getChannel().sendMessage(EmbedFactory.getCommandEmbedError(this, TextManager.getString(getLocale(), TextManager.GENERAL, "no_args"))).get();
            return false;
        } else {
            RedditPost post = RedditDownloader.getPost(getLocale(), followedString);

            if (post != null) {
                if (post.isNsfw() && !event.getServerTextChannel().get().isNsfw()) {
                    event.getChannel().sendMessage(EmbedFactory.getNSFWBlockEmbed(getLocale())).get();
                    return false;
                }

                EmbedBuilder eb = getEmbed(post);
                EmbedFactory.addLog(eb, LogStatus.WARNING, TextManager.getString(getLocale(), TextManager.GENERAL, "tracker", getPrefix(), getTrigger()));
                event.getChannel().sendMessage(eb).get();
                return true;
            } else {
                EmbedBuilder eb = EmbedFactory.getCommandEmbedError(this)
                        .setTitle(TextManager.getString(getLocale(), TextManager.GENERAL, "no_results"))
                        .setDescription(TextManager.getString(getLocale(), TextManager.GENERAL, "no_results_description", followedString));
                event.getChannel().sendMessage(eb).get();
                return false;
            }
        }
    }

    private EmbedBuilder getEmbed(RedditPost post) throws Throwable {
        EmbedBuilder eb = EmbedFactory.getCommandEmbedStandard(this, post.getDescription())
                .setTitle(post.getTitle())
                .setThumbnail(post.getThumbnail())
                .setAuthor(post.getAuthor(), "https://www.reddit.com/user/" + post.getAuthor(), "")
                .setTimestamp(post.getInstant())
                .setImage(post.getImage())
                .setUrl(post.getLink());

        String flairText = "";
        String flair = post.getFlair();
        if (flair != null && !("" + flair).equals("null") && !("" + flair).equals("") && !("" + flair).equals(" "))
            flairText = flair + " | ";

        String nsfwString = "";
        if (post.isNsfw()) {
            nsfwString = " " + getString("nsfw");
        }

        eb.setFooter(getString("footer", flairText, Tools.numToString(getLocale(), post.getScore()), Tools.numToString(getLocale(), post.getComments()), post.getDomain()) + nsfwString);

        return eb;
    }

    @Override
    public TrackerData onTrackerRequest(TrackerData trackerData) throws Throwable {
        ServerTextChannel channel = trackerData.getChannel().get();
        if (trackerData.getKey().length() == 0) {
            channel.sendMessage(EmbedFactory.getCommandEmbedError(this, TextManager.getString(getLocale(), TextManager.GENERAL, "no_args"))).get();
            return null;
        } else {
            trackerData.setInstant(Instant.now().plusSeconds(60 * 10));
            PostBundle<RedditPost> postBundle = RedditDownloader.getPostTracker(getLocale(), trackerData.getKey(), trackerData.getArg());

            boolean containsOnlyNsfw = true;

            if (postBundle != null) {
                for(int i = 0; i < postBundle.getPosts().size(); i++) {
                    RedditPost post = postBundle.getPosts().get(i);
                    if (!post.isNsfw() || channel.isNsfw()) {
                        if (trackerData.getArg() != null || i == 0) channel.sendMessage(getEmbed(post));
                        containsOnlyNsfw = false;
                    }
                }

                if (containsOnlyNsfw && trackerData.getArg() == null) {
                    channel.sendMessage(EmbedFactory.getNSFWBlockEmbed(getLocale())).get();
                    return null;
                }

                trackerData.setArg(postBundle.getNewestPost());
                return trackerData;
            } else {
                if (trackerData.getArg() == null) {
                    EmbedBuilder eb = EmbedFactory.getCommandEmbedError(this)
                            .setTitle(TextManager.getString(getLocale(), TextManager.GENERAL, "no_results"))
                            .setDescription(TextManager.getString(getLocale(), TextManager.COMMANDS, "reddit_noresults_tracker", trackerData.getKey()));
                    channel.sendMessage(eb).get();
                    return null;
                } else {
                    trackerData.setSaveChanges(false);
                    return trackerData;
                }
            }
        }
    }

    @Override
    public boolean trackerUsesKey() {
        return true;
    }

}
