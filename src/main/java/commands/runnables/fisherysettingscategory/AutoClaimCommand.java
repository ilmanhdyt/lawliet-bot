package commands.runnables.fisherysettingscategory;

import java.util.Locale;
import commands.listeners.CommandProperties;
import commands.runnables.CommandOnOffSwitchAbstract;
import core.cache.PatreonCache;
import mysql.modules.autoclaim.DBAutoClaim;
import net.dv8tion.jda.api.entities.Member;

@CommandProperties(
        trigger = "autoclaim",
        emoji = "\uD83E\uDD16",
        usesExtEmotes = true,
        executableWithoutArgs = true
)
public class AutoClaimCommand extends CommandOnOffSwitchAbstract {

    public AutoClaimCommand(Locale locale, String prefix) {
        super(locale, prefix, true);
    }

    @Override
    protected boolean isActive(Member member) {
        return DBAutoClaim.getInstance().retrieve().isActive(member.getIdLong());
    }

    @Override
    protected boolean setActive(Member member, boolean active) {
        if (!active || PatreonCache.getInstance().getUserTier(member.getIdLong(), false) >= 2) {
            DBAutoClaim.getInstance().retrieve().setActive(member.getIdLong(), active);
            return true;
        } else {
            return false;
        }
    }

}
