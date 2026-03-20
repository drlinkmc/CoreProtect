package net.coreprotect.command.lookup.row;

import java.nio.charset.StandardCharsets;

/**
 * Typed representation of a block/container/item/interaction lookup result row.
 * <p>
 * Raw column layout: [time, player, x, y, z, type, data, action, rolledBack, wid, amount, meta, blockData, table?]
 */
public class BlockRow {

    // Raw column indices
    private static final int COL_TIME = 0;
    private static final int COL_PLAYER = 1;
    private static final int COL_X = 2;
    private static final int COL_Y = 3;
    private static final int COL_Z = 4;
    private static final int COL_TYPE = 5;
    private static final int COL_DATA = 6;
    private static final int COL_ACTION = 7;
    private static final int COL_ROLLED_BACK = 8;
    private static final int COL_WID = 9;
    private static final int COL_AMOUNT = 10;
    private static final int COL_META = 11;
    private static final int COL_BLOCK_DATA = 12;
    private static final int COL_TABLE = 13;

    public final int time;
    public final String player;
    public final int x;
    public final int y;
    public final int z;
    public final int type;
    public final int data;
    public final int action;
    public final int rolledBack;
    public final int wid;
    public final int amount;
    public final byte[] meta;
    public final String blockData;
    public final int table;

    private BlockRow(int time, String player, int x, int y, int z, int type, int data,
                     int action, int rolledBack, int wid, int amount, byte[] meta,
                     String blockData, int table) {
        this.time = time;
        this.player = player;
        this.x = x;
        this.y = y;
        this.z = z;
        this.type = type;
        this.data = data;
        this.action = action;
        this.rolledBack = rolledBack;
        this.wid = wid;
        this.amount = amount;
        this.meta = meta;
        this.blockData = blockData;
        this.table = table;
    }

    /**
     * Whether this row's block change has been rolled back.
     */
    public boolean isBlockRolledBack() {
        return RolledBackState.isBlockRolledBack(rolledBack);
    }

    /**
     * Whether this row's inventory change has been rolled back.
     */
    public boolean isInventoryRolledBack() {
        return RolledBackState.isInventoryRolledBack(rolledBack);
    }

    /**
     * Returns the strikethrough format string if the block is rolled back, or empty otherwise.
     */
    public String getBlockRolledBackFormat(String strikethroughCode) {
        return isBlockRolledBack() ? strikethroughCode : "";
    }

    /**
     * Returns the strikethrough format string if the inventory is rolled back, or empty otherwise.
     */
    public String getInventoryRolledBackFormat(String strikethroughCode) {
        return isInventoryRolledBack() ? strikethroughCode : "";
    }

    public static BlockRow fromRawData(String[] rawData) {
        byte[] meta = rawData[COL_META] == null ? null : rawData[COL_META].getBytes(StandardCharsets.ISO_8859_1);
        int table = rawData.length > COL_TABLE ? Integer.parseInt(rawData[COL_TABLE]) : 0;

        return new BlockRow(
            Integer.parseInt(rawData[COL_TIME]),
            rawData[COL_PLAYER],
            Integer.parseInt(rawData[COL_X]),
            Integer.parseInt(rawData[COL_Y]),
            Integer.parseInt(rawData[COL_Z]),
            Integer.parseInt(rawData[COL_TYPE]),
            Integer.parseInt(rawData[COL_DATA]),
            Integer.parseInt(rawData[COL_ACTION]),
            Integer.parseInt(rawData[COL_ROLLED_BACK]),
            Integer.parseInt(rawData[COL_WID]),
            Integer.parseInt(rawData[COL_AMOUNT]),
            meta,
            rawData[COL_BLOCK_DATA],
            table
        );
    }
}

