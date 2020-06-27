package MySQL.Modules.BannedWords;

import Core.CustomObservableList;
import MySQL.BeanWithServer;
import MySQL.Modules.Server.ServerBean;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;

public class BannedWordsBean extends BeanWithServer {

    private boolean active, strict;
    private final CustomObservableList<Long> ignoredUserIds, logReceiverUserIds;
    private final CustomObservableList<String> words;

    public BannedWordsBean(ServerBean serverBean, boolean active, boolean strict, @NonNull ArrayList<Long> ignoredUserIds, @NonNull ArrayList<Long> logReceiverUserIds, @NonNull ArrayList<String> words) {
        super(serverBean);
        this.active = active;
        this.strict = strict;
        this.ignoredUserIds = new CustomObservableList<>(ignoredUserIds);
        this.logReceiverUserIds = new CustomObservableList<>(logReceiverUserIds);
        this.words = new CustomObservableList<>(words);
    }


    /* Getters */

    public CustomObservableList<Long> getIgnoredUserIds() {
        return ignoredUserIds;
    }

    public CustomObservableList<Long> getLogReceiverUserIds() {
        return logReceiverUserIds;
    }

    public CustomObservableList<String> getWords() {
        return words;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isStrict() { return strict; }

    /* Setters */

    public void toggleActive() {
        this.active = !this.active;
        setChanged();
        notifyObservers();
    }

    public void toggleStrict() {
        this.strict = !this.strict;
        setChanged();
        notifyObservers();
    }

}
