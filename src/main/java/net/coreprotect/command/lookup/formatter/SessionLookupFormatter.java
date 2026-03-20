package net.coreprotect.command.lookup.formatter;

import java.sql.Connection;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import com.google.common.base.Strings;

import net.coreprotect.command.lookup.row.SessionRow;
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
    public void formatResults(List<String[]> lookupList, int timestamp, Connection connection) throws Exception {
        for (String[] data : lookupList) {
            SessionRow row = SessionRow.fromRawData(data);
            String timeAgo = ChatUtils.getTimeSince(row.time, timestamp, true);
            String leftPadding = computeLeftPadding(row.time, timestamp);

            String tag = (row.action != 0 ? Color.GREEN + "+" : Color.RED + "-");
            String selector = (row.action != 0 ? Selector.FIRST : Selector.SECOND);
            Chat.sendComponent(player, timeAgo + " " + tag + " " + Color.DARK_AQUA + Phrase.build(Phrase.LOOKUP_LOGIN, Color.DARK_AQUA + row.player + Color.WHITE, selector));
            Chat.sendComponent(player, Color.WHITE + leftPadding + Color.GREY + "^ " + ChatUtils.getCoordinates(command.getName(), row.wid, row.x, row.y, row.z, true, true));
            PluginChannelListener.getInstance().sendInfoData(player, row.time, Phrase.LOOKUP_LOGIN, selector, row.player, -1, row.x, row.y, row.z, row.wid);
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

