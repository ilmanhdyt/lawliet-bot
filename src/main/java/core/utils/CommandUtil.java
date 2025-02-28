package core.utils;

import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import commands.Command;
import commands.CommandEvent;
import core.EmbedFactory;
import core.ExceptionLogger;
import core.TextManager;
import core.mention.MentionList;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.BaseGuildMessageChannel;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.requests.restaction.MessageAction;

public class CommandUtil {

    public static ChannelResponse differentChannelExtract(Command command, CommandEvent event, String args, Permission... permissions) {
        String[] words = args.split(" ");
        BaseGuildMessageChannel channel = event.getTextChannel();
        MentionList<BaseGuildMessageChannel> messageChannelsFirst = MentionUtil.getBaseGuildMessageChannels(event.getGuild(), words[0]);
        if (messageChannelsFirst.getList().size() > 0) {
            channel = messageChannelsFirst.getList().get(0);
            args = args.substring(words[0].length()).trim();
        } else {
            MentionList<BaseGuildMessageChannel> messageChannelsLast = MentionUtil.getBaseGuildMessageChannels(event.getGuild(), words[words.length - 1]);
            if (messageChannelsLast.getList().size() > 0) {
                channel = messageChannelsLast.getList().get(0);
                args = args.substring(0, args.length() - words[words.length - 1].length()).trim();
            }
        }

        HashSet<Permission> permissionSet = new HashSet<>(List.of(Permission.MESSAGE_SEND, Permission.MESSAGE_EMBED_LINKS));
        permissionSet.addAll(Arrays.asList(permissions));
        Permission[] finalPermissions = permissionSet.toArray(Permission[]::new);

        EmbedBuilder missingPermissionsEmbed = BotPermissionUtil.getUserAndBotPermissionMissingEmbed(
                command.getLocale(),
                channel,
                event.getMember(),
                new Permission[0],
                finalPermissions,
                new Permission[0],
                finalPermissions,
                new Permission[0]
        );
        if (missingPermissionsEmbed != null) {
            command.drawMessageNew(missingPermissionsEmbed)
                    .exceptionally(ExceptionLogger.get());
            return null;
        }

        return new ChannelResponse(args, channel);
    }

    public static CompletableFuture<Message> differentChannelSendMessage(Command command, CommandEvent event, BaseGuildMessageChannel channel, EmbedBuilder eb, Map<String, InputStream> fileAttachmentMap) throws ExecutionException, InterruptedException {
        if (event.getChannel() == channel) {
            command.addAllFileAttachments(fileAttachmentMap);
            return command.drawMessageNew(eb);
        } else {
            MessageAction messageAction = channel.sendMessageEmbeds(eb.build());
            for (String name : fileAttachmentMap.keySet()) {
                messageAction = messageAction.addFile(fileAttachmentMap.get(name), name);
            }
            CompletableFuture<Message> messageFuture = messageAction.submit();

            EmbedBuilder confirmEmbed = EmbedFactory.getEmbedDefault(command, TextManager.getString(command.getLocale(), TextManager.GENERAL, "message_sent_channel", channel.getAsMention()));
            command.drawMessageNew(confirmEmbed).exceptionally(ExceptionLogger.get());

            return messageFuture;
        }
    }


    public static class ChannelResponse {

        private final String args;
        private final BaseGuildMessageChannel channel;

        public ChannelResponse(String args, BaseGuildMessageChannel channel) {
            this.args = args;
            this.channel = channel;
        }

        public String getArgs() {
            return args;
        }

        public BaseGuildMessageChannel getChannel() {
            return channel;
        }

    }

}
