package net.coreprotect.command.lookup.formatter;

import java.sql.Connection;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import net.coreprotect.command.lookup.row.SignRow;
import net.coreprotect.listener.channel.PluginChannelListener;
import net.coreprotect.utility.Chat;
import net.coreprotect.utility.ChatUtils;
import net.coreprotect.utility.Color;

/**
 * Formats sign message lookup results (a:sign).
 */
public class SignLookupFormatter implements LookupFormatter {

    private final CommandSender player;
    private final Command command;

    public SignLookupFormatter(CommandSender player, Command command) {
        this.player = player;
        this.command = command;
    }

    @Override
    public void formatResults(List<String[]> lookupList, int timestamp, Connection connection) throws Exception {
        for (String[] data : lookupList) {
            SignRow row = SignRow.fromRawData(data);
            String timeago = ChatUtils.getTimeSince(row.time, timestamp, true);
            String leftPadding = SessionLookupFormatter.computeLeftPadding(row.time, timestamp);

            Chat.sendComponent(player, timeago + " " + Color.WHITE + "- " + Color.DARK_AQUA + row.player + ": " + Color.WHITE, row.message);
            Chat.sendComponent(player, Color.WHITE + leftPadding + Color.GREY + "^ " + ChatUtils.getCoordinates(command.getName(), row.wid, row.x, row.y, row.z, true, true));
            PluginChannelListener.getInstance().sendMessageData(player, row.time, row.player, row.message, true, row.x, row.y, row.z, row.wid);
        }
    }

    @Override
    public boolean needsSeparator() {
        return false;
    }
}

