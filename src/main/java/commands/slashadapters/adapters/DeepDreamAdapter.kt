package commands.slashadapters.adapters

import commands.runnables.aitoyscategory.DeepDreamCommand
import commands.slashadapters.Slash
import commands.slashadapters.SlashMeta
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent

@Slash(command = DeepDreamCommand::class)
class DeepDreamAdapter : AIAdapterAbstract() {

    override fun process(event: SlashCommandInteractionEvent): SlashMeta {
        return SlashMeta(DeepDreamCommand::class.java, collectArgs(event))
    }

}