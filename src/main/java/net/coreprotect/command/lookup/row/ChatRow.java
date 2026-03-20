package net.coreprotect.command.lookup.row;

/**
 * Typed representation of a chat/command lookup result row.
 * <p>
 * Raw column layout: [time, player, message, wid?, x?, y?, z?]
 * The wid/x/y/z fields are only present when the sender is a plugin channel player.
 */
public class ChatRow {

    // Raw column indices
    private static final int COL_TIME = 0;
    private static final int COL_PLAYER = 1;
    private static final int COL_MESSAGE = 2;
    private static final int COL_WID = 3;
    private static final int COL_X = 4;
    private static final int COL_Y = 5;
    private static final int COL_Z = 6;

    /** Minimum array length when coordinates are present. */
    private static final int MIN_LENGTH_WITH_COORDS = 7;

    public final int time;
    public final String player;
    public final String message;
    public final int wid;
    public final int x;
    public final int y;
    public final int z;
    public final boolean hasCoordinates;

    private ChatRow(int time, String player, String message, int wid, int x, int y, int z, boolean hasCoordinates) {
        this.time = time;
        this.player = player;
        this.message = message;
        this.wid = wid;
        this.x = x;
        this.y = y;
        this.z = z;
        this.hasCoordinates = hasCoordinates;
    }

    public static ChatRow fromRawData(String[] data) {
        int time = Integer.parseInt(data[COL_TIME]);
        String player = data[COL_PLAYER];
        String message = data[COL_MESSAGE];

        if (data.length >= MIN_LENGTH_WITH_COORDS) {
            int wid = Integer.parseInt(data[COL_WID]);
            int x = Integer.parseInt(data[COL_X]);
            int y = Integer.parseInt(data[COL_Y]);
            int z = Integer.parseInt(data[COL_Z]);
            return new ChatRow(time, player, message, wid, x, y, z, true);
        }

        return new ChatRow(time, player, message, 0, 0, 0, 0, false);
    }
}

