package commands.runnables.invitetrackingcategory;

import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import commands.CommandEvent;
import commands.listeners.CommandProperties;
import commands.listeners.MessageInputResponse;
import commands.runnables.NavigationAbstract;
import constants.LogStatus;
import core.EmbedFactory;
import core.TextManager;
import core.utils.BotPermissionUtil;
import core.utils.MentionUtil;
import core.utils.StringUtil;
import modules.invitetracking.InviteTracking;
import mysql.modules.invitetracking.DBInviteTracking;
import mysql.modules.invitetracking.InviteTrackingData;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.IMentionable;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import org.jetbrains.annotations.NotNull;

@CommandProperties(
        trigger = "invitetracking",
        userGuildPermissions = Permission.MANAGE_SERVER,
        botGuildPermissions = Permission.MANAGE_SERVER,
        releaseDate = { 2021, 9, 21 },
        emoji = "✉️",
        usesExtEmotes = true,
        executableWithoutArgs = true,
        aliases = { "invitetracker", "invtracking", "invitet", "invt", "invtracker" }
)
public class InviteTrackingCommand extends NavigationAbstract {

    private final int MAIN = 0,
            SET_LOGCHANNEL = 1;

    private InviteTrackingData inviteTrackingData;
    private boolean resetLog = true;

    public InviteTrackingCommand(Locale locale, String prefix) {
        super(locale, prefix);
    }

    @Override
    public boolean onTrigger(@NotNull CommandEvent event, @NotNull String args) {
        inviteTrackingData = DBInviteTracking.getInstance().retrieve(event.getGuild().getIdLong());
        registerNavigationListener(event.getMember());
        return true;
    }

    @ControllerMessage(state = SET_LOGCHANNEL)
    public MessageInputResponse onMessageSetLogChannel(MessageReceivedEvent event, String input) {
        List<TextChannel> channelList = MentionUtil.getTextChannels(event.getGuild(), input).getList();
        if (channelList.size() == 0) {
            setLog(LogStatus.FAILURE, TextManager.getNoResultsString(getLocale(), input));
            return MessageInputResponse.FAILED;
        } else {
            TextChannel channel = channelList.get(0);
            if (BotPermissionUtil.canWriteEmbed(channel)) {
                inviteTrackingData.setChannelId(channelList.get(0).getIdLong());
                setLog(LogStatus.SUCCESS, getString("channelset"));
                setState(0);
                return MessageInputResponse.SUCCESS;
            } else {
                setLog(LogStatus.FAILURE, TextManager.getString(getLocale(), TextManager.GENERAL, "permission", channel.getName()));
                return MessageInputResponse.FAILED;
            }
        }
    }

    @ControllerButton(state = DEFAULT_STATE)
    public boolean onButtonMain(ButtonInteractionEvent event, int i) throws SQLException, InterruptedException {
        switch (i) {
            case -1 -> {
                deregisterListenersWithComponentMessage();
                return false;
            }
            case 0 -> {
                inviteTrackingData.toggleActive();
                setLog(LogStatus.SUCCESS, getString("activeset", inviteTrackingData.isActive()));
                if (inviteTrackingData.isActive()) {
                    InviteTracking.synchronizeGuildInvites(event.getGuild());
                }
                resetLog = true;
                return true;
            }
            case 1 -> {
                setState(1);
                resetLog = true;
                return true;
            }
            case 2 -> {
                inviteTrackingData.togglePing();
                setLog(LogStatus.SUCCESS, getString("pingset", inviteTrackingData.getPing()));
                resetLog = true;
                return true;
            }
            case 3 -> {
                inviteTrackingData.toggleAdvanced();
                setLog(LogStatus.SUCCESS, getString("advancedset", inviteTrackingData.isAdvanced()));
                resetLog = true;
                return true;
            }
            case 4 -> {
                if (resetLog) {
                    resetLog = false;
                    setLog(LogStatus.WARNING, TextManager.getString(getLocale(), TextManager.GENERAL, "confirm_warning_button"));
                } else {
                    DBInviteTracking.getInstance().resetInviteTrackerSlots(event.getGuild().getIdLong());
                    inviteTrackingData = DBInviteTracking.getInstance().retrieve(event.getGuild().getIdLong());
                    resetLog = true;
                    setLog(LogStatus.SUCCESS, getString("reset"));
                    setState(0);
                }
                return true;
            }
        }
        return false;
    }

    @ControllerButton(state = SET_LOGCHANNEL)
    public boolean onButtonLogChannel(ButtonInteractionEvent event, int i) {
        if (i == -1) {
            setState(0);
            return true;
        } else if (i == 0) {
            inviteTrackingData.setChannelId(null);
            setLog(LogStatus.SUCCESS, getString("channelset"));
            setState(0);
            return true;
        }
        return false;
    }

    @Draw(state = MAIN)
    public EmbedBuilder onDrawMain(Member member) {
        String notSet = TextManager.getString(getLocale(), TextManager.GENERAL, "notset");
        String[] options = getString("state0_options").split("\n");
        Button[] buttons = new Button[options.length];
        for (int i = 0; i < buttons.length; i++) {
            buttons[i] = Button.of(
                    i != buttons.length - 1 ? ButtonStyle.PRIMARY : ButtonStyle.DANGER,
                    String.valueOf(i),
                    options[i]
            );
        }
        setComponents(buttons);

        return EmbedFactory.getEmbedDefault(this, getString("state0_description"))
                .addField(getString("state0_mactive"), StringUtil.getOnOffForBoolean(getTextChannel().get(), getLocale(), inviteTrackingData.isActive()), true)
                .addField(getString("state0_mchannel"), StringUtil.escapeMarkdown(inviteTrackingData.getTextChannel().map(IMentionable::getAsMention).orElse(notSet)), true)
                .addField(getString("state0_mping"), StringUtil.getOnOffForBoolean(getTextChannel().get(), getLocale(), inviteTrackingData.getPing()), true)
                .addField(getString("state0_madvanced"), StringUtil.getOnOffForBoolean(getTextChannel().get(), getLocale(), inviteTrackingData.isAdvanced()), true);
    }

    @Draw(state = SET_LOGCHANNEL)
    public EmbedBuilder onDrawLogChannel(Member member) {
        setComponents(getString("state1_clear"));
        return EmbedFactory.getEmbedDefault(this, getString("state1_description"), getString("state1_title"));
    }

}
