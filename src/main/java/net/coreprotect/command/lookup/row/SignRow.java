package net.coreprotect.command.lookup.row;

/**
 * Typed representation of a sign message lookup result row.
 * <p>
 * Raw column layout: [time, player, wid, x, y, z, message]
 */
public class SignRow {

    // Raw column indices
    private static final int COL_TIME = 0;
    private static final int COL_PLAYER = 1;
    private static final int COL_WID = 2;
    private static final int COL_X = 3;
    private static final int COL_Y = 4;
    private static final int COL_Z = 5;
    private static final int COL_MESSAGE = 6;

    public final int time;
    public final String player;
    public final int wid;
    public final int x;
    public final int y;
    public final int z;
    public final String message;

    private SignRow(int time, String player, int wid, int x, int y, int z, String message) {
        this.time = time;
        this.player = player;
        this.wid = wid;
        this.x = x;
        this.y = y;
        this.z = z;
        this.message = message;
    }

    public static SignRow fromRawData(String[] data) {
        return new SignRow(
            Integer.parseInt(data[COL_TIME]),
            data[COL_PLAYER],
            Integer.parseInt(data[COL_WID]),
            Integer.parseInt(data[COL_X]),
            Integer.parseInt(data[COL_Y]),
            Integer.parseInt(data[COL_Z]),
            data[COL_MESSAGE]
        );
    }
}

