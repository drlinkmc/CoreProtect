package net.coreprotect.command.lookup.row;

/**
 * Constants for the rolled_back column values stored in the database.
 * <p>
 * The rolled_back value is a 2-bit flag field:
 * <ul>
 *   <li>Bit 0 (value 1): block has been rolled back</li>
 *   <li>Bit 1 (value 2): inventory has been rolled back</li>
 * </ul>
 */
public final class RolledBackState {

    /** No rollback has been applied. */
    public static final int NONE = 0;

    /** Only the block change has been rolled back. */
    public static final int BLOCK = 1;

    /** Only the inventory change has been rolled back. */
    public static final int INVENTORY = 2;

    /** Both block and inventory changes have been rolled back. */
    public static final int BOTH = 3;

    private RolledBackState() {
        throw new IllegalStateException("Constants class");
    }

    /**
     * Whether the block portion of this state is rolled back.
     */
    public static boolean isBlockRolledBack(int state) {
        return state == BLOCK || state == BOTH;
    }

    /**
     * Whether the inventory portion of this state is rolled back.
     */
    public static boolean isInventoryRolledBack(int state) {
        return state == INVENTORY || state == BOTH;
    }
}

