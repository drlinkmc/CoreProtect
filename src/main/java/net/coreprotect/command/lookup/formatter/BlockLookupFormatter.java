package net.coreprotect.command.lookup.formatter;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.util.List;
import java.util.Locale;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import net.coreprotect.command.lookup.LookupAction;
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
            int drb = Integer.parseInt(data[8]);
            String rbd = (drb == 1 || drb == 3) ? Color.STRIKETHROUGH : "";

            String time = data[0];
            String dplayer = data[1];
            int dataX = Integer.parseInt(data[2]);
            int dataY = Integer.parseInt(data[3]);
            int dataZ = Integer.parseInt(data[4]);
            int dtype = Integer.parseInt(data[5]);
            int ddata = Integer.parseInt(data[6]);
            int daction = Integer.parseInt(data[7]);
            int wid = Integer.parseInt(data[9]);
            int amount = Integer.parseInt(data[10]);
            String tag = Color.WHITE + "-";

            String timeago = ChatUtils.getTimeSince(Integer.parseInt(time), unixtimestamp, true);
            String leftPadding = SessionLookupFormatter.computeLeftPadding(Integer.parseInt(time), unixtimestamp);

            String dname = resolveName(daction, dtype, ddata, amount, connection);
            boolean isPlayer = (daction == 3 && !actions.contains(LookupAction.ITEM) && amount == -1 && dtype == 0);

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

            if (actions.contains(LookupAction.CONTAINER) || actions.contains(LookupAction.CONTAINER_LOCATION) || actions.contains(LookupAction.ITEM) || amount > -1) {
                byte[] metadata = data[11] == null ? null : data[11].getBytes(StandardCharsets.ISO_8859_1);
                String tooltip = ItemUtils.getEnchantments(metadata, dtype, amount);

                if (daction == 2 || daction == 3) {
                    phrase = Phrase.LOOKUP_ITEM;
                    selector = (daction != 2 ? Selector.FIRST : Selector.SECOND);
                    tag = (daction != 2 ? Color.GREEN + "+" : Color.RED + "-");
                    action = "a:item";
                }
                else if (daction == 4 || daction == 5) {
                    phrase = Phrase.LOOKUP_STORAGE;
                    selector = (daction != 4 ? Selector.FIRST : Selector.SECOND);
                    tag = (daction != 4 ? Color.RED + "-" : Color.GREEN + "+");
                    action = "a:item";
                }
                else if (daction == 6 || daction == 7) {
                    phrase = Phrase.LOOKUP_PROJECTILE;
                    selector = (daction != 7 ? Selector.FIRST : Selector.SECOND);
                    tag = Color.RED + "-";
                    action = "a:item";
                }
                else {
                    phrase = Phrase.LOOKUP_CONTAINER;
                    selector = (daction != 0 ? Selector.FIRST : Selector.SECOND);
                    tag = (daction != 0 ? Color.GREEN + "+" : Color.RED + "-");
                    action = "a:container";
                }

                Chat.sendComponent(player, timeago + " " + tag + " " + Phrase.build(phrase, Color.DARK_AQUA + rbd + dplayer + Color.WHITE + rbd, "x" + amount, ChatUtils.createTooltip(Color.DARK_AQUA + rbd + dname, tooltip) + Color.WHITE, selector));
                PluginChannelListener.getInstance().sendData(player, Integer.parseInt(time), phrase, selector, dplayer, dname, (tag.contains("+") ? 1 : -1), dataX, dataY, dataZ, wid, rbd, action.contains("container"), tag.contains("+"));
            }
            else {
                if (daction == 2 || daction == 3) {
                    phrase = Phrase.LOOKUP_INTERACTION;
                    selector = (daction != 3 ? Selector.FIRST : Selector.SECOND);
                    tag = (daction != 3 ? Color.WHITE + "-" : Color.RED + "-");
                    action = (daction == 2 ? "a:click" : "a:kill");
                }
                else {
                    phrase = Phrase.LOOKUP_BLOCK;
                    selector = (daction != 0 ? Selector.FIRST : Selector.SECOND);
                    tag = (daction != 0 ? Color.GREEN + "+" : Color.RED + "-");
                }

                Chat.sendComponent(player, timeago + " " + tag + " " + Phrase.build(phrase, Color.DARK_AQUA + rbd + dplayer + Color.WHITE + rbd, Color.DARK_AQUA + rbd + dname + Color.WHITE, selector));
                PluginChannelListener.getInstance().sendData(player, Integer.parseInt(time), phrase, selector, dplayer, dname, (tag.contains("+") ? 1 : -1), dataX, dataY, dataZ, wid, rbd, false, tag.contains("+"));
            }

            action = (actions.isEmpty() ? " (" + action + ")" : "");
            Chat.sendComponent(player, Color.WHITE + leftPadding + Color.GREY + "^ " + ChatUtils.getCoordinates(command.getName(), wid, dataX, dataY, dataZ, true, true) + Color.GREY + Color.ITALIC + action);
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
