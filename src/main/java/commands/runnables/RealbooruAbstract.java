package commands.runnables;

import java.util.Locale;
import java.util.Set;

public abstract class RealbooruAbstract extends PornPredefinedAbstract {

    public RealbooruAbstract(Locale locale, String prefix) {
        super(locale, prefix);
    }

    @Override
    protected Set<String> getAdditionalFilters() {
        return Set.of("anthro", "fisting", "pissing", "pee", "peeing", "piss", "doll", "what", "diaper");
    }

    @Override
    public String getDomain() {
        return "realbooru.com";
    }

    @Override
    public boolean isExplicit() {
        return true;
    }

}
