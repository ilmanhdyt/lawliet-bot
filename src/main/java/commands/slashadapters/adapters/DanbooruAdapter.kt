package commands.slashadapters.adapters

import commands.runnables.nsfwcategory.DanbooruCommand
import commands.slashadapters.Slash

@Slash(command = DanbooruCommand::class)
class DanbooruAdapter : BooruSearchAdapterAbstract()