package net.coreprotect.command.lookup.row;

/**
 * Typed representation of a username-change lookup result row.
 * <p>
 * Raw column layout: [time, uuid, username]
 */
public class UsernameRow {

    // Raw column indices
    private static final int COL_TIME = 0;
    private static final int COL_UUID = 1;
    private static final int COL_USERNAME = 2;

    public final int time;
    public final String uuid;
    public final String username;

    private UsernameRow(int time, String uuid, String username) {
        this.time = time;
        this.uuid = uuid;
        this.username = username;
    }

    public static UsernameRow fromRawData(String[] data) {
        return new UsernameRow(
            Integer.parseInt(data[COL_TIME]),
            data[COL_UUID],
            data[COL_USERNAME]
        );
    }
}

