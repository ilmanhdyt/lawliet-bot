package dashboard

import commands.Category
import core.MemberCacheController
import core.ShardManager
import core.TextManager
import core.atomicassets.AtomicGuild
import core.atomicassets.AtomicMember
import core.cache.PatreonCache
import core.utils.BotPermissionUtil
import dashboard.container.DashboardContainer
import dashboard.container.VerticalContainer
import mysql.modules.guild.DBGuild
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import org.json.JSONObject
import java.util.*

abstract class DashboardCategory(private val guildId: Long, private val userId: Long, val locale: Locale) {

    val atomicGuild: AtomicGuild
    val atomicMember: AtomicMember
    val properties: DashboardProperties
    val prefix: String
    val isPremium
        get() = PatreonCache.getInstance().hasPremium(atomicMember.idLong, true) ||
                PatreonCache.getInstance().isUnlocked(atomicGuild.idLong)

    private var components: DashboardContainer? = null

    init {
        atomicGuild = AtomicGuild(guildId)
        atomicMember = AtomicMember(guildId, userId)
        properties = this.javaClass.getAnnotation(DashboardProperties::class.java)
        prefix = DBGuild.getInstance().retrieve(guildId).prefix
    }

    abstract fun retrievePageTitle(): String

    abstract fun generateComponents(guild: Guild, mainContainer: VerticalContainer)

    fun draw(): DashboardContainer {
        val mainContainer = VerticalContainer()
        components = mainContainer
        atomicGuild.get().ifPresent { guild ->
            generateComponents(guild, mainContainer)
        }
        return components!!
    }

    fun receiveAction(action: JSONObject): ActionResult? {
        if (missingBotPermissions().isEmpty() && missingUserPermissions().isEmpty()) {
            return components?.receiveAction(action)
        } else {
            return null
        }
    }

    fun missingBotPermissions(): List<Permission> {
        return ShardManager.getLocalGuildById(guildId).orElse(null)?.let { guild ->
            return properties.botPermissions
                .filter { !BotPermissionUtil.can(guild, it) }
        } ?: emptyList()
    }

    fun missingUserPermissions(): List<Permission> {
        return ShardManager.getLocalGuildById(guildId).orElse(null)?.let { guild ->
            return MemberCacheController.getInstance().loadMember(guild, userId).get()?.let { member ->
                return properties.userPermissions
                    .filter { !BotPermissionUtil.can(member, it) }
            } ?: emptyList()
        } ?: emptyList()
    }

    fun getString(category: String, key: String, vararg args: String): String {
        var text = TextManager.getString(locale, category, key, *args)
        text = text.replace("{PREFIX}", prefix)
        return text
    }

    fun getString(category: String, key: String, option: Int, vararg args: String): String {
        var text = TextManager.getString(locale, category, key, option, *args)
        text = text.replace("{PREFIX}", prefix)
        return text
    }

    fun getString(category: String, key: String, secondOption: Boolean, vararg args: String): String {
        var text = TextManager.getString(locale, category, key, secondOption, *args)
        text = text.replace("{PREFIX}", prefix)
        return text
    }

    fun getString(category: Category, key: String, vararg args: String): String {
        var text = TextManager.getString(locale, category, key, *args)
        text = text.replace("{PREFIX}", prefix)
        return text
    }

    fun getString(category: Category, key: String, option: Int, vararg args: String): String {
        var text = TextManager.getString(locale, category, key, option, *args)
        text = text.replace("{PREFIX}", prefix)
        return text
    }

    fun getString(category: Category, key: String, secondOption: Boolean, vararg args: String): String {
        var text = TextManager.getString(locale, category, key, secondOption, *args)
        text = text.replace("{PREFIX}", prefix)
        return text
    }

}