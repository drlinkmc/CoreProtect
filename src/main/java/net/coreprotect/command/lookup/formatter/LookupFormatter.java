package net.coreprotect.command.lookup.formatter;

import java.sql.Connection;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import net.coreprotect.command.lookup.LookupAction;

/**
 * Interface for formatting lookup results for display to the player.
 * Each implementation handles a specific type of lookup action
 * (chat, session, username, sign, inventory, block).
 */
public interface LookupFormatter {

    /**
     * Format and send lookup results to the player.
     *
     * @param lookupList the raw lookup data rows
     * @param unixtimestamp the current unix timestamp for computing "time ago"
     * @param connection the database connection (needed by some formatters for lazy loading)
     */
    void formatResults(List<String[]> lookupList, int unixtimestamp, Connection connection) throws Exception;

    /**
     * Whether this formatter's output needs a separator line ("-----")
     * before pagination controls.
     *
     * @return true if a separator should be shown before page navigation
     */
    boolean needsSeparator();

    /**
     * Select the appropriate formatter based on the requested actions.
     *
     * @param player the command sender to receive formatted output
     * @param command the command being executed
     * @param actions the list of action type IDs being looked up
     * @return the formatter that handles the given action types
     */
    static LookupFormatter selectFormatter(CommandSender player, Command command, List<Integer> actions) {
        if (actions.contains(LookupAction.CHAT) || actions.contains(LookupAction.COMMAND)) {
            return new ChatLookupFormatter(player);
        }
        else if (actions.contains(LookupAction.SESSION)) {
            return new SessionLookupFormatter(player, command);
        }
        else if (actions.contains(LookupAction.USERNAME)) {
            return new UsernameLookupFormatter(player);
        }
        else if (actions.contains(LookupAction.SIGN)) {
            return new SignLookupFormatter(player, command);
        }
        else if (actions.contains(LookupAction.CONTAINER) && actions.contains(LookupAction.ITEM)) {
            return new InventoryLookupFormatter(player);
        }
        else {
            return new BlockLookupFormatter(player, command, actions);
        }
    }
}

