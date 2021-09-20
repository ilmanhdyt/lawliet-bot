package core;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.GenericComponentInteractionCreateEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.requests.RestAction;

public class InteractionResponse {

    private final GenericComponentInteractionCreateEvent event;

    public InteractionResponse(GenericComponentInteractionCreateEvent event) {
        this.event = event;
    }

    public RestAction<Message> editMessageEmbeds(List<MessageEmbed> embeds, Collection<ActionRow> actionRows) {
        if (!event.isAcknowledged()) {
            Message message = event.getMessage();
            return event.editMessageEmbeds(embeds)
                    .setActionRows(actionRows)
                    .map(h -> message);
        } else {
            return event.getHook().editOriginalEmbeds(embeds)
                    .setActionRows(actionRows);
        }
    }

    public RestAction<Message> replyEmbeds(List<MessageEmbed> embeds, boolean ephemeral) {
        return replyEmbeds(embeds, Collections.emptyList(), ephemeral);
    }

    public RestAction<Message> replyEmbeds(List<MessageEmbed> embeds, Collection<ActionRow> actionRows, boolean ephemeral) {
        if (!event.isAcknowledged()) {
            Message message = event.getMessage();
            return event.replyEmbeds(embeds)
                    .addActionRows(actionRows)
                    .setEphemeral(ephemeral)
                    .map(h -> message);
        } else {
            return event.getHook().sendMessageEmbeds(embeds)
                    .addActionRows(actionRows)
                    .setEphemeral(ephemeral);
        }
    }

    public boolean isValid() {
        Instant interactionEnd = event.getTimeCreated().toInstant().plus(Duration.ofMinutes(14));
        return Instant.now().isBefore(interactionEnd);
    }

    public void complete() {
        if (isValid() && !event.isAcknowledged()) {
            event.deferEdit().queue();
        }
    }

}
