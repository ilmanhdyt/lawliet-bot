package commands.slashadapters.adapters

import commands.runnables.externalcategory.TwitchCommand
import commands.slashadapters.Slash
import commands.slashadapters.SlashAdapter
import commands.slashadapters.SlashMeta
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.CommandData

@Slash(command = TwitchCommand::class)
class TwitchAdapter : SlashAdapter() {

    public override fun addOptions(commandData: CommandData): CommandData {
        return commandData
            .addOption(OptionType.STRING, "twitch_channel_name", "The name of the twitch channel", true)
    }

    override fun process(event: SlashCommandEvent): SlashMeta {
        return SlashMeta(TwitchCommand::class.java, collectArgs(event))
    }

}