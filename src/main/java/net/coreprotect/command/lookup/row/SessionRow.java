package net.coreprotect.command.lookup.row;

/**
 * Typed representation of a session (login/logout) lookup result row.
 * <p>
 * Raw column layout: [time, player, wid, x, y, z, action]
 */
public class SessionRow {

    // Raw column indices
    private static final int COL_TIME = 0;
    private static final int COL_PLAYER = 1;
    private static final int COL_WID = 2;
    private static final int COL_X = 3;
    private static final int COL_Y = 4;
    private static final int COL_Z = 5;
    private static final int COL_ACTION = 6;

    public final int time;
    public final String player;
    public final int wid;
    public final int x;
    public final int y;
    public final int z;
    public final int action;

    private SessionRow(int time, String player, int wid, int x, int y, int z, int action) {
        this.time = time;
        this.player = player;
        this.wid = wid;
        this.x = x;
        this.y = y;
        this.z = z;
        this.action = action;
    }

    public static SessionRow fromRawData(String[] data) {
        return new SessionRow(
            Integer.parseInt(data[COL_TIME]),
            data[COL_PLAYER],
            Integer.parseInt(data[COL_WID]),
            Integer.parseInt(data[COL_X]),
            Integer.parseInt(data[COL_Y]),
            Integer.parseInt(data[COL_Z]),
            Integer.parseInt(data[COL_ACTION])
        );
    }
}

