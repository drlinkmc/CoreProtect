package net.coreprotect.command.lookup.formatter;

import java.sql.Connection;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import com.google.common.base.Strings;

import net.coreprotect.language.Phrase;
import net.coreprotect.language.Selector;
import net.coreprotect.listener.channel.PluginChannelListener;
import net.coreprotect.utility.Chat;
import net.coreprotect.utility.ChatUtils;
import net.coreprotect.utility.Color;

/**
 * Formats session (login/logout) lookup results (a:session).
 */
public class SessionLookupFormatter implements LookupFormatter {

    private final CommandSender player;
    private final Command command;

    public SessionLookupFormatter(CommandSender player, Command command) {
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
            int action = Integer.parseInt(data[6]);
            String timeago = ChatUtils.getTimeSince(Integer.parseInt(time), unixtimestamp, true);

            String leftPadding = computeLeftPadding(Integer.parseInt(time), unixtimestamp);

            String tag = (action != 0 ? Color.GREEN + "+" : Color.RED + "-");
            Chat.sendComponent(player, timeago + " " + tag + " " + Color.DARK_AQUA + Phrase.build(Phrase.LOOKUP_LOGIN, Color.DARK_AQUA + dplayer + Color.WHITE, (action != 0 ? Selector.FIRST : Selector.SECOND)));
            Chat.sendComponent(player, Color.WHITE + leftPadding + Color.GREY + "^ " + ChatUtils.getCoordinates(command.getName(), wid, dataX, dataY, dataZ, true, true));
            PluginChannelListener.getInstance().sendInfoData(player, Integer.parseInt(time), Phrase.LOOKUP_LOGIN, (action != 0 ? Selector.FIRST : Selector.SECOND), dplayer, -1, dataX, dataY, dataZ, wid);
        }
    }

    @Override
    public boolean needsSeparator() {
        return false;
    }

    static String computeLeftPadding(int time, int unixtimestamp) {
        int timeLength = 50 + (ChatUtils.getTimeSince(time, unixtimestamp, false).replaceAll("\\D", "").length() * 6);
        String leftPadding = Color.BOLD + Strings.padStart("", 10, ' ');
        if (timeLength % 4 == 0) {
            leftPadding = Strings.padStart("", timeLength / 4, ' ');
        }
        else {
            leftPadding = leftPadding + Color.WHITE + Strings.padStart("", (timeLength - 50) / 4, ' ');
        }
        return leftPadding;
    }
}

