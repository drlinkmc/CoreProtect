package net.coreprotect.command.lookup.formatter;

import java.sql.Connection;
import java.util.List;
import java.util.Locale;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;

import net.coreprotect.command.lookup.row.BlockRow;
import net.coreprotect.database.logger.ItemLogger;
import net.coreprotect.language.Phrase;
import net.coreprotect.language.Selector;
import net.coreprotect.listener.channel.PluginChannelListener;
import net.coreprotect.utility.Chat;
import net.coreprotect.utility.ChatUtils;
import net.coreprotect.utility.Color;
import net.coreprotect.utility.ItemUtils;
import net.coreprotect.utility.MaterialUtils;
import net.coreprotect.utility.StringUtils;

/**
 * Formats inventory transaction lookup results (a:inventory, i.e. actions CONTAINER + ITEM).
 */
public class InventoryLookupFormatter implements LookupFormatter {

    private final CommandSender player;

    public InventoryLookupFormatter(CommandSender player) {
        this.player = player;
    }

    @Override
    public void formatResults(List<String[]> lookupList, int unixtimestamp, Connection connection) throws Exception {
        for (String[] data : lookupList) {
            BlockRow row = BlockRow.fromRawData(data);
            String rbd = row.getInventoryRolledBackFormat(Color.STRIKETHROUGH);
            String timeago = ChatUtils.getTimeSince(row.time, unixtimestamp, true);
            Material blockType = ItemUtils.itemFilter(MaterialUtils.getType(row.type), (row.table == 0));
            String dname = StringUtils.nameFilter(blockType.name().toLowerCase(Locale.ROOT), row.data);
            String tooltip = ItemUtils.getEnchantments(row.meta, row.type, row.amount);

            String[] tagAndSelector = resolveTagAndSelector(row.action);
            String tag = tagAndSelector[0];
            String selector = tagAndSelector[1];

            Chat.sendComponent(player, timeago + " " + tag + " " + Phrase.build(Phrase.LOOKUP_CONTAINER, Color.DARK_AQUA + rbd + row.player + Color.WHITE + rbd, "x" + row.amount, ChatUtils.createTooltip(Color.DARK_AQUA + rbd + dname, tooltip) + Color.WHITE, selector));
            PluginChannelListener.getInstance().sendData(player, row.time, Phrase.LOOKUP_CONTAINER, selector, row.player, dname, row.amount, row.x, row.y, row.z, row.wid, rbd, true, tag.contains("+"));
        }
    }

    @Override
    public boolean needsSeparator() {
        return true;
    }

    private String[] resolveTagAndSelector(int daction) {
        String selector;
        String tag;

        if (daction == 2 || daction == 3) { // LOOKUP_ITEM
            selector = (daction != 2 ? Selector.FIRST : Selector.SECOND);
            tag = (daction != 2 ? Color.GREEN + "+" : Color.RED + "-");
        }
        else if (daction == 4 || daction == 5) { // LOOKUP_STORAGE
            selector = (daction == 4 ? Selector.FIRST : Selector.SECOND);
            tag = (daction == 4 ? Color.GREEN + "+" : Color.RED + "-");
        }
        else if (daction == 6 || daction == 7) { // LOOKUP_PROJECTILE
            selector = Selector.SECOND;
            tag = Color.RED + "-";
        }
        else if (daction == ItemLogger.ITEM_BREAK || daction == ItemLogger.ITEM_DESTROY || daction == ItemLogger.ITEM_CREATE) {
            selector = (daction == ItemLogger.ITEM_CREATE ? Selector.FIRST : Selector.SECOND);
            tag = (daction == ItemLogger.ITEM_CREATE ? Color.GREEN + "+" : Color.RED + "-");
        }
        else if (daction == ItemLogger.ITEM_SELL || daction == ItemLogger.ITEM_BUY) { // LOOKUP_TRADE
            selector = (daction == ItemLogger.ITEM_BUY ? Selector.FIRST : Selector.SECOND);
            tag = (daction == ItemLogger.ITEM_BUY ? Color.GREEN + "+" : Color.RED + "-");
        }
        else { // LOOKUP_CONTAINER
            selector = (daction == 0 ? Selector.FIRST : Selector.SECOND);
            tag = (daction == 0 ? Color.GREEN + "+" : Color.RED + "-");
        }

        return new String[] { tag, selector };
    }
}

