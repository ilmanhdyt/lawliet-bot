package events.discordevents;

import java.util.function.Consumer;
import commands.CommandContainer;
import commands.CommandListenerMeta;
import core.EmbedFactory;
import core.TextManager;
import core.utils.BotPermissionUtil;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.component.GenericComponentInteractionCreateEvent;

public interface InteractionListenerHandler<T extends GenericComponentInteractionCreateEvent> {

    default boolean handleInteraction(T event, Class<?> clazz, Consumer<CommandListenerMeta<?>> listenerMetaConsumer) {
        if (event.getChannel() instanceof TextChannel && BotPermissionUtil.canWriteEmbed(event.getGuildChannel())) {
            for (CommandListenerMeta<?> listener : CommandContainer.getListeners(clazz)) {
                switch (listener.check(event)) {
                    case ACCEPT -> {
                        listenerMetaConsumer.accept(listener);
                        return false;
                    }
                    case DENY -> {
                        EmbedBuilder eb = EmbedFactory.getEmbedError(
                                listener.getCommand(),
                                TextManager.getString(listener.getCommand().getLocale(), TextManager.GENERAL, "button_listener_denied", listener.getCommand().getMemberAsMention().get())
                        );
                        event.replyEmbeds(eb.build())
                                .setEphemeral(true)
                                .queue();
                        return false;
                    }
                    case DENY_WITHOUT_AUTHOR_MENTION -> {
                        EmbedBuilder eb = EmbedFactory.getEmbedError(
                                listener.getCommand(),
                                TextManager.getString(listener.getCommand().getLocale(), TextManager.GENERAL, "button_listener_denied_no_mention")
                        );
                        event.replyEmbeds(eb.build())
                                .setEphemeral(true)
                                .queue();
                        return false;
                    }
                }
            }
        }
        return true;
    }

}
