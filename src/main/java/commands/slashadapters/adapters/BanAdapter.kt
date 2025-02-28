package commands.slashadapters.adapters

import commands.runnables.moderationcategory.BanCommand
import commands.slashadapters.Slash
import commands.slashadapters.SlashAdapter
import commands.slashadapters.SlashMeta
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

@Slash(command = BanCommand::class)
class BanAdapter : SlashAdapter() {

    public override fun addOptions(commandData: SlashCommandData): SlashCommandData {
        return commandData
            .addOption(OptionType.STRING, "members", "Mention one or more members who shall be banned", true)
            .addOption(OptionType.STRING, "reason", "The reason of this mod action", false)
            .addOption(OptionType.STRING, "duration", "The duration of the ban (e.g. 1h 3m)", false)
    }

    override fun process(event: SlashCommandInteractionEvent): SlashMeta {
        return SlashMeta(BanCommand::class.java, collectArgs(event))
    }

}