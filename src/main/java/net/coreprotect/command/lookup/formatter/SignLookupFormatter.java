package net.coreprotect.command.lookup.formatter;

import java.sql.Connection;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

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
    public void formatResults(List<String[]> lookupList, int unixtimestamp, Connection connection) throws Exception {
        for (String[] data : lookupList) {
            String time = data[0];
            String dplayer = data[1];
            int wid = Integer.parseInt(data[2]);
            int dataX = Integer.parseInt(data[3]);
            int dataY = Integer.parseInt(data[4]);
            int dataZ = Integer.parseInt(data[5]);
            String message = data[6];
            String timeago = ChatUtils.getTimeSince(Integer.parseInt(time), unixtimestamp, true);

            String leftPadding = SessionLookupFormatter.computeLeftPadding(Integer.parseInt(time), unixtimestamp);

            Chat.sendComponent(player, timeago + " " + Color.WHITE + "- " + Color.DARK_AQUA + dplayer + ": " + Color.WHITE, message);
            Chat.sendComponent(player, Color.WHITE + leftPadding + Color.GREY + "^ " + ChatUtils.getCoordinates(command.getName(), wid, dataX, dataY, dataZ, true, true) + "");
            PluginChannelListener.getInstance().sendMessageData(player, Integer.parseInt(time), dplayer, message, true, dataX, dataY, dataZ, wid);
        }
    }

    @Override
    public boolean needsSeparator() {
        return false;
    }
}

