package Commands.NSFW;

import CommandListeners.CommandProperties;
import CommandListeners.onRecievedListener;
import org.javacord.api.event.message.MessageCreateEvent;

@CommandProperties(
        trigger = "realb",
        executable = true,
        emoji = "\uD83D\uDD1E",
        nsfw = true,
        withLoadingBar = true
)
public class RealbooruCommand extends PornCommand implements onRecievedListener {
    public RealbooruCommand() {
        super();
        domain = "realbooru.com";
        imageTemplate = "https://realbooru.com/images/%d/%f";
    }

    @Override
    public boolean onRecieved(MessageCreateEvent event, String followedString) throws Throwable {
        return onPornRequestRecieved(event, followedString, " -loli -shota");
    }
}
