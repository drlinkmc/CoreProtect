package net.coreprotect.command.lookup.formatter;

import java.sql.Connection;
import java.util.List;

import org.bukkit.command.CommandSender;

import net.coreprotect.command.lookup.row.ChatRow;
import net.coreprotect.listener.channel.PluginChannelHandshakeListener;
import net.coreprotect.listener.channel.PluginChannelListener;
import net.coreprotect.utility.Chat;
import net.coreprotect.utility.ChatUtils;
import net.coreprotect.utility.Color;

/**
 * Formats chat and command lookup results (a:chat / a:command).
 */
public class ChatLookupFormatter implements LookupFormatter {

    private final CommandSender player;

    public ChatLookupFormatter(CommandSender player) {
        this.player = player;
    }

    @Override
    public void formatResults(List<String[]> lookupList, int unixtimestamp, Connection connection) throws Exception {
        for (String[] data : lookupList) {
            ChatRow row = ChatRow.fromRawData(data);
            String timeago = ChatUtils.getTimeSince(row.time, unixtimestamp, true);

            Chat.sendComponent(player, timeago + " " + Color.WHITE + "- " + Color.DARK_AQUA + row.player + ": " + Color.WHITE, row.message);

            if (row.hasCoordinates && PluginChannelHandshakeListener.getInstance().isPluginChannelPlayer(player)) {
                PluginChannelListener.getInstance().sendMessageData(player, row.time, row.player, row.message, false, row.x, row.y, row.z, row.wid);
            }
        }
    }

    @Override
    public boolean needsSeparator() {
        return true;
    }
}

