package mysql.modules.subs;

import java.time.Duration;
import java.util.HashMap;
import java.util.Locale;
import com.google.common.cache.CacheBuilder;
import core.CustomObservableMap;
import mysql.DBDataLoad;
import mysql.MySQLManager;
import mysql.DBMapCache;

public class DBSubs extends DBMapCache<DBSubs.Command, CustomObservableMap<Long, SubSlot>> {

    public enum Command { DAILY, WORK, CLAIM, SURVEY }

    private static final DBSubs ourInstance = new DBSubs();

    public static DBSubs getInstance() {
        return ourInstance;
    }

    private DBSubs() {
    }

    @Override
    protected CacheBuilder<Object, Object> getCacheBuilder() {
        return CacheBuilder.newBuilder()
                .expireAfterWrite(Duration.ofMinutes(1));
    }

    @Override
    protected CustomObservableMap<Long, SubSlot> load(Command command) throws Exception {
        HashMap<Long, SubSlot> subMap = new DBDataLoad<SubSlot>("Subs", "memberId, locale, errors", "command = ?",
                preparedStatement -> preparedStatement.setString(1, command.name().toLowerCase())
        ).getHashMap(
                SubSlot::getUserId,
                resultSet -> {
                    return new SubSlot(
                            command,
                            resultSet.getLong(1),
                            new Locale(resultSet.getString(2)),
                            resultSet.getInt(3)
                    );
                }
        );

        return new CustomObservableMap<>(subMap)
                .addMapAddListener(this::addSub)
                .addMapUpdateListener(this::addSub)
                .addMapRemoveListener(this::removeSub);
    }

    private void addSub(SubSlot subSlot) {
        MySQLManager.asyncUpdate("REPLACE INTO Subs (command, memberId, locale, errors) VALUES (?, ?, ?, ?);", preparedStatement -> {
            preparedStatement.setString(1, subSlot.getCommand().name().toLowerCase());
            preparedStatement.setLong(2, subSlot.getUserId());
            preparedStatement.setString(3, subSlot.getLocale().getDisplayName());
            preparedStatement.setInt(4, subSlot.getErrors());
        });
    }

    private void removeSub(SubSlot subSlot) {
        MySQLManager.asyncUpdate("DELETE FROM Subs WHERE command = ? AND memberId = ?;", preparedStatement -> {
            preparedStatement.setString(1, subSlot.getCommand().name().toLowerCase());
            preparedStatement.setLong(2, subSlot.getUserId());
        });
    }

}
