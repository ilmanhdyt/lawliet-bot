package commands.slashadapters.adapters

import commands.runnables.gimmickscategory.EveryoneCommand
import commands.slashadapters.Slash
import commands.slashadapters.SlashAdapter
import commands.slashadapters.SlashMeta
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

@Slash(command = EveryoneCommand::class)
class EveryoneAdapter : SlashAdapter() {

    public override fun addOptions(commandData: SlashCommandData): SlashCommandData {
        return commandData
            .addOption(OptionType.USER, "members", "Mention one or more relevant members", false)
            .addOption(OptionType.BOOLEAN, "everyone", "If you want to mention everyone", false)
    }

    override fun process(event: SlashCommandInteractionEvent): SlashMeta {
        return SlashMeta(EveryoneCommand::class.java, collectArgs(event))
    }

}