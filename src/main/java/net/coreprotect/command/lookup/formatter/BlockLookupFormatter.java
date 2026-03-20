package net.coreprotect.command.lookup.formatter;

import java.sql.Connection;
import java.util.List;
import java.util.Locale;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import net.coreprotect.command.lookup.LookupAction;
import net.coreprotect.command.lookup.row.BlockRow;
import net.coreprotect.config.ConfigHandler;
import net.coreprotect.database.statement.UserStatement;
import net.coreprotect.language.Phrase;
import net.coreprotect.language.Selector;
import net.coreprotect.listener.channel.PluginChannelListener;
import net.coreprotect.utility.Chat;
import net.coreprotect.utility.ChatUtils;
import net.coreprotect.utility.Color;
import net.coreprotect.utility.EntityUtils;
import net.coreprotect.utility.ItemUtils;
import net.coreprotect.utility.MaterialUtils;
import net.coreprotect.utility.StringUtils;

/**
 * Formats the default block/item/container/interaction lookup results.
 * This is the fallback formatter when no specific action type matches.
 */
public class BlockLookupFormatter implements LookupFormatter {

    private final CommandSender player;
    private final Command command;
    private final List<Integer> actions;

    public BlockLookupFormatter(CommandSender player, Command command, List<Integer> actions) {
        this.player = player;
        this.command = command;
        this.actions = actions;
    }

    @Override
    public void formatResults(List<String[]> lookupList, int unixtimestamp, Connection connection) throws Exception {
        for (String[] data : lookupList) {
            BlockRow row = BlockRow.fromRawData(data);
            String rbd = row.getBlockRolledBackFormat(Color.STRIKETHROUGH);
            String tag = Color.WHITE + "-";

            String timeago = ChatUtils.getTimeSince(row.time, unixtimestamp, true);
            String leftPadding = SessionLookupFormatter.computeLeftPadding(row.time, unixtimestamp);

            String dname = resolveName(row.action, row.type, row.data, row.amount, connection);
            boolean isPlayer = (row.action == 3 && !actions.contains(LookupAction.ITEM) && row.amount == -1 && row.type == 0);

            if (!dname.isEmpty() && !isPlayer) {
                dname = "minecraft:" + dname.toLowerCase(Locale.ROOT);
            }
            // Hide "minecraft:" for now.
            if (dname.contains("minecraft:")) {
                dname = dname.split(":")[1];
            }

            Phrase phrase = Phrase.LOOKUP_BLOCK;
            String selector = Selector.FIRST;
            String action = "a:block";

            if (actions.contains(LookupAction.CONTAINER) || actions.contains(LookupAction.CONTAINER_LOCATION) || actions.contains(LookupAction.ITEM) || row.amount > -1) {
                String tooltip = ItemUtils.getEnchantments(row.meta, row.type, row.amount);

                if (row.action == 2 || row.action == 3) {
                    phrase = Phrase.LOOKUP_ITEM;
                    selector = (row.action != 2 ? Selector.FIRST : Selector.SECOND);
                    tag = (row.action != 2 ? Color.GREEN + "+" : Color.RED + "-");
                    action = "a:item";
                }
                else if (row.action == 4 || row.action == 5) {
                    phrase = Phrase.LOOKUP_STORAGE;
                    selector = (row.action != 4 ? Selector.FIRST : Selector.SECOND);
                    tag = (row.action != 4 ? Color.RED + "-" : Color.GREEN + "+");
                    action = "a:item";
                }
                else if (row.action == 6 || row.action == 7) {
                    phrase = Phrase.LOOKUP_PROJECTILE;
                    selector = (row.action != 7 ? Selector.FIRST : Selector.SECOND);
                    tag = Color.RED + "-";
                    action = "a:item";
                }
                else {
                    phrase = Phrase.LOOKUP_CONTAINER;
                    selector = (row.action != 0 ? Selector.FIRST : Selector.SECOND);
                    tag = (row.action != 0 ? Color.GREEN + "+" : Color.RED + "-");
                    action = "a:container";
                }

                Chat.sendComponent(player, timeago + " " + tag + " " + Phrase.build(phrase, Color.DARK_AQUA + rbd + row.player + Color.WHITE + rbd, "x" + row.amount, ChatUtils.createTooltip(Color.DARK_AQUA + rbd + dname, tooltip) + Color.WHITE, selector));
                PluginChannelListener.getInstance().sendData(player, row.time, phrase, selector, row.player, dname, (tag.contains("+") ? 1 : -1), row.x, row.y, row.z, row.wid, rbd, action.contains("container"), tag.contains("+"));
            }
            else {
                if (row.action == 2 || row.action == 3) {
                    phrase = Phrase.LOOKUP_INTERACTION;
                    selector = (row.action != 3 ? Selector.FIRST : Selector.SECOND);
                    tag = (row.action != 3 ? Color.WHITE + "-" : Color.RED + "-");
                    action = (row.action == 2 ? "a:click" : "a:kill");
                }
                else {
                    phrase = Phrase.LOOKUP_BLOCK;
                    selector = (row.action != 0 ? Selector.FIRST : Selector.SECOND);
                    tag = (row.action != 0 ? Color.GREEN + "+" : Color.RED + "-");
                }

                Chat.sendComponent(player, timeago + " " + tag + " " + Phrase.build(phrase, Color.DARK_AQUA + rbd + row.player + Color.WHITE + rbd, Color.DARK_AQUA + rbd + dname + Color.WHITE, selector));
                PluginChannelListener.getInstance().sendData(player, row.time, phrase, selector, row.player, dname, (tag.contains("+") ? 1 : -1), row.x, row.y, row.z, row.wid, rbd, false, tag.contains("+"));
            }

            action = (actions.isEmpty() ? " (" + action + ")" : "");
            Chat.sendComponent(player, Color.WHITE + leftPadding + Color.GREY + "^ " + ChatUtils.getCoordinates(command.getName(), row.wid, row.x, row.y, row.z, true, true) + Color.GREY + Color.ITALIC + action);
        }
    }

    @Override
    public boolean needsSeparator() {
        return false;
    }

    private String resolveName(int daction, int dtype, int ddata, int amount, Connection connection) throws Exception {
        if (daction == 3 && !actions.contains(LookupAction.ITEM) && amount == -1) {
            if (dtype == 0) {
                if (ConfigHandler.playerIdCacheReversed.get(ddata) == null) {
                    UserStatement.loadName(connection, ddata);
                }
                return ConfigHandler.playerIdCacheReversed.get(ddata);
            }
            else {
                return EntityUtils.getEntityType(dtype).name();
            }
        }
        else {
            String dname = MaterialUtils.getType(dtype).name().toLowerCase(Locale.ROOT);
            return StringUtils.nameFilter(dname, ddata);
        }
    }
}
