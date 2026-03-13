package net.coreprotect.command.lookup;

/**
 * Constants for lookup action types used in the actions list
 * parsed from command arguments (e.g. a:block, a:chat, a:container).
 * <p>
 * These values correspond to the integers added by
 * {@link net.coreprotect.command.parser.ActionParser#parseAction}.
 */
public final class LookupAction {

    /** Block broken (a:-block) */
    public static final int BLOCK_BREAK = 0;

    /** Block placed (a:+block) */
    public static final int BLOCK_PLACE = 1;

    /** Player click/interaction (a:click) */
    public static final int CLICK = 2;

    /** Entity kill/death (a:kill) */
    public static final int KILL = 3;

    /** Container transaction (a:container) */
    public static final int CONTAINER = 4;

    /** Container at specific coordinates (internal, used for container rollback at a location) */
    public static final int CONTAINER_LOCATION = 5;

    /** Chat message (a:chat) */
    public static final int CHAT = 6;

    /** Command (a:command) */
    public static final int COMMAND = 7;

    /** Session login/logout (a:session) */
    public static final int SESSION = 8;

    /** Username change (a:username) */
    public static final int USERNAME = 9;

    /** Sign text (a:sign) */
    public static final int SIGN = 10;

    /** Item drop/pickup/inventory (a:item) */
    public static final int ITEM = 11;

    private LookupAction() {
        throw new IllegalStateException("Constants class");
    }
}

