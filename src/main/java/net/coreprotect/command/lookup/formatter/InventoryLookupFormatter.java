package net.coreprotect.command.lookup.formatter;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.util.List;
import java.util.Locale;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;

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
            String time = data[0];
            String dplayer = data[1];
            int dtype = Integer.parseInt(data[5]);
            int ddata = Integer.parseInt(data[6]);
            int daction = Integer.parseInt(data[7]);
            int amount = Integer.parseInt(data[10]);
            int wid = Integer.parseInt(data[9]);
            int dataX = Integer.parseInt(data[2]);
            int dataY = Integer.parseInt(data[3]);
            int dataZ = Integer.parseInt(data[4]);
            String rbd = ((Integer.parseInt(data[8]) == 2 || Integer.parseInt(data[8]) == 3) ? Color.STRIKETHROUGH : "");
            String timeago = ChatUtils.getTimeSince(Integer.parseInt(time), unixtimestamp, true);
            Material blockType = ItemUtils.itemFilter(MaterialUtils.getType(dtype), (Integer.parseInt(data[13]) == 0));
            String dname = StringUtils.nameFilter(blockType.name().toLowerCase(Locale.ROOT), ddata);
            byte[] metadata = data[11] == null ? null : data[11].getBytes(StandardCharsets.ISO_8859_1);
            String tooltip = ItemUtils.getEnchantments(metadata, dtype, amount);

            String[] tagAndSelector = resolveTagAndSelector(daction);
            String tag = tagAndSelector[0];
            String selector = tagAndSelector[1];

            Chat.sendComponent(player, timeago + " " + tag + " " + Phrase.build(Phrase.LOOKUP_CONTAINER, Color.DARK_AQUA + rbd + dplayer + Color.WHITE + rbd, "x" + amount, ChatUtils.createTooltip(Color.DARK_AQUA + rbd + dname, tooltip) + Color.WHITE, selector));
            PluginChannelListener.getInstance().sendData(player, Integer.parseInt(time), Phrase.LOOKUP_CONTAINER, selector, dplayer, dname, amount, dataX, dataY, dataZ, wid, rbd, true, tag.contains("+"));
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

