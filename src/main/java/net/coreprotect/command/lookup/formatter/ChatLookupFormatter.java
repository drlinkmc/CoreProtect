package net.coreprotect.command.lookup.formatter;

import java.sql.Connection;
import java.util.List;

import org.bukkit.command.CommandSender;

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
            String time = data[0];
            String dplayer = data[1];
            String message = data[2];
            String timeago = ChatUtils.getTimeSince(Integer.parseInt(time), unixtimestamp, true);

            Chat.sendComponent(player, timeago + " " + Color.WHITE + "- " + Color.DARK_AQUA + dplayer + ": " + Color.WHITE, message);

            if (PluginChannelHandshakeListener.getInstance().isPluginChannelPlayer(player)) {
                int wid = Integer.parseInt(data[3]);
                int dataX = Integer.parseInt(data[4]);
                int dataY = Integer.parseInt(data[5]);
                int dataZ = Integer.parseInt(data[6]);
                PluginChannelListener.getInstance().sendMessageData(player, Integer.parseInt(time), dplayer, message, false, dataX, dataY, dataZ, wid);
            }
        }
    }

    @Override
    public boolean needsSeparator() {
        return true;
    }
}

