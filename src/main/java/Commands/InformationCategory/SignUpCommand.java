package Commands.InformationCategory;

import CommandListeners.CommandProperties;

import CommandSupporters.Command;
import Constants.Settings;
import Core.EmbedFactory;
import MySQL.DBGiveaway;
import org.javacord.api.event.message.MessageCreateEvent;

@CommandProperties(
        trigger = "signup",
        emoji = "✏️",
        executable = true,
        aliases = {"giveaway", "singup", "register"}
)
public class SignUpCommand extends Command {

    @Override
    public boolean onMessageReceived(MessageCreateEvent event, String followedString) throws Throwable {
        boolean success = DBGiveaway.registerGiveaway(event.getServer().get(), event.getMessage().getUserAuthor().get());

        if (success) {
            event.getChannel().sendMessage(EmbedFactory.getCommandEmbedStandard(this,
                    getString("success", Settings.SERVER_INVITE_URL,
                            event.getMessage().getUserAuthor().get().getMentionTag(),
                            event.getServer().get().getName()
                    )
            )).get();
        } else {
            event.getChannel().sendMessage(EmbedFactory.getCommandEmbedError(this,
                    getString("exists", Settings.SERVER_INVITE_URL),
                    getString("exists_title")
            )).get();
        }

        return success;
    }
    
}
