package commands.runnables.utilitycategory;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import commands.Command;
import commands.CommandEvent;
import commands.listeners.CommandProperties;
import commands.listeners.OnButtonListener;
import constants.Emojis;
import core.EmbedFactory;
import core.ExceptionLogger;
import core.TextManager;
import core.mention.Mention;
import core.utils.BotPermissionUtil;
import core.utils.EmojiUtil;
import core.utils.MentionUtil;
import modules.RoleAssigner;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import org.jetbrains.annotations.NotNull;

@CommandProperties(
        trigger = "assignroles",
        userGuildPermissions = Permission.MANAGE_ROLES,
        botGuildPermissions = Permission.MANAGE_ROLES,
        emoji = "\uD83D\uDCE5",
        executableWithoutArgs = false,
        patreonRequired = true,
        turnOffTimeout = true,
        requiresFullMemberCache = true,
        aliases = { "giverole", "assign", "assignrole" }
)
public class AssignRoleCommand extends Command implements OnButtonListener {

    private static final String CANCEL_EMOJI = Emojis.X;
    private static final RoleAssigner roleAssigner = new RoleAssigner();

    private Mention rolesMention;

    public AssignRoleCommand(Locale locale, String prefix) {
        super(locale, prefix);
    }

    @Override
    public boolean onTrigger(@NotNull CommandEvent event, @NotNull String args) {
        List<Role> roles = MentionUtil.getRoles(event.getGuild(), args).getList();

        /* check for no role mention */
        if (roles.isEmpty()) {
            drawMessageNew(EmbedFactory.getEmbedError(this, getString("no_role"))).exceptionally(ExceptionLogger.get());
            return false;
        }

        /* check for missing role manage permissions bot */
        List<Role> rolesMissingPermissions = roles.stream()
                .filter(r -> !BotPermissionUtil.canManage(r))
                .collect(Collectors.toList());
        if (rolesMissingPermissions.size() > 0) {
            Mention mention = MentionUtil.getMentionedStringOfRoles(getLocale(), rolesMissingPermissions);
            drawMessageNew( EmbedFactory.getEmbedError(this, TextManager.getString(getLocale(), TextManager.GENERAL, "permission_role", mention.isMultiple(), mention.getMentionText())))
                    .exceptionally(ExceptionLogger.get());
            return false;
        }

        /* check for missing role manage permissions user */
        rolesMissingPermissions = roles.stream()
                .filter(r -> !BotPermissionUtil.canManage(r))
                .collect(Collectors.toList());
        if (rolesMissingPermissions.size() > 0) {
            Mention mention = MentionUtil.getMentionedStringOfRoles(getLocale(), rolesMissingPermissions);
            drawMessageNew(EmbedFactory.getEmbedError(this, TextManager.getString(getLocale(), TextManager.GENERAL, "permission_role_user", mention.isMultiple(), mention.getMentionText())))
                    .exceptionally(ExceptionLogger.get());
            return false;
        }

        rolesMention = MentionUtil.getMentionedStringOfRoles(getLocale(), roles);
        Optional<CompletableFuture<Boolean>> futureOpt = roleAssigner.assignRoles(event.getGuild(), roles, addRole());

        /* check for busy */
        if (futureOpt.isEmpty()) {
            drawMessageNew(EmbedFactory.getEmbedError(this, getString("busy_desc"), getString("busy_title")))
                    .exceptionally(ExceptionLogger.get());
            return false;
        }

        CompletableFuture<Boolean> future = futureOpt.get();
        future.thenAccept(this::onAssignmentFinished);

        setComponents(Button.of(ButtonStyle.SECONDARY, "quit", TextManager.getString(getLocale(), TextManager.GENERAL, "process_abort")));
        registerButtonListener(event.getMember());
        return true;
    }

    protected boolean addRole() {
        return true;
    }

    private void onAssignmentFinished(boolean success) {
        deregisterListenersWithComponents();
        if (success) {
            drawMessage(EmbedFactory.getEmbedDefault(this, getString("success_desc", rolesMention.isMultiple(), rolesMention.getMentionText())))
                    .exceptionally(ExceptionLogger.get());
        } else {
            drawMessage(EmbedFactory.getEmbedError(this, getString("canceled_desc", rolesMention.isMultiple(), rolesMention.getMentionText()), getString("canceled_title")))
                    .exceptionally(ExceptionLogger.get());
        }
    }

    @Override
    public boolean onButton(@NotNull ButtonInteractionEvent event) throws Throwable {
        event.deferEdit().queue();
        deregisterListenersWithComponents();
        roleAssigner.cancel(event.getGuild().getIdLong());
        return false;
    }

    @Override
    public EmbedBuilder draw(@NotNull Member member) throws Throwable {
        return EmbedFactory.getEmbedDefault(
                this,
                getString("loading", rolesMention.isMultiple(), rolesMention.getMentionText(), EmojiUtil.getLoadingEmojiMention(getTextChannel().orElse(null)), CANCEL_EMOJI)
        );
    }

}
