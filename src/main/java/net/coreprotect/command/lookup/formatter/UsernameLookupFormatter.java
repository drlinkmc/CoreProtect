package net.coreprotect.command.lookup.formatter;

import java.sql.Connection;
import java.util.List;

import org.bukkit.command.CommandSender;

import net.coreprotect.config.ConfigHandler;
import net.coreprotect.language.Phrase;
import net.coreprotect.listener.channel.PluginChannelListener;
import net.coreprotect.utility.Chat;
import net.coreprotect.utility.ChatUtils;
import net.coreprotect.utility.Color;

/**
 * Formats username-change lookup results (a:username).
 */
public class UsernameLookupFormatter implements LookupFormatter {

    private final CommandSender player;

    public UsernameLookupFormatter(CommandSender player) {
        this.player = player;
    }

    @Override
    public void formatResults(List<String[]> lookupList, int unixtimestamp, Connection connection) throws Exception {
        for (String[] data : lookupList) {
            String time = data[0];
            String user = ConfigHandler.uuidCacheReversed.get(data[1]);
            String username = data[2];
            String timeago = ChatUtils.getTimeSince(Integer.parseInt(time), unixtimestamp, true);

            Chat.sendComponent(player, timeago + " " + Color.WHITE + "- " + Phrase.build(Phrase.LOOKUP_USERNAME, Color.DARK_AQUA + user + Color.WHITE, Color.DARK_AQUA + username + Color.WHITE));
            PluginChannelListener.getInstance().sendUsernameData(player, Integer.parseInt(time), user, username);
        }
    }

    @Override
    public boolean needsSeparator() {
        return true;
    }
}

