package net.coreprotect.command.lookup;

import java.sql.Connection;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import net.coreprotect.command.lookup.formatter.LookupFormatter;
import net.coreprotect.config.ConfigHandler;
import net.coreprotect.database.Database;
import net.coreprotect.database.Lookup;
import net.coreprotect.database.lookup.PlayerLookup;
import net.coreprotect.language.Phrase;
import net.coreprotect.language.Selector;
import net.coreprotect.utility.Chat;
import net.coreprotect.utility.ChatUtils;
import net.coreprotect.utility.Color;
import net.coreprotect.utility.WorldUtils;

public class StandardLookupThread implements Runnable {
    private static final String HASHTAG_GLOBAL = "#global";
    private static final String HASHTAG_CONTAINER = "#container";
    private static final String HASHTAG_HOPPER = "#hopper";
    private final CommandSender player;
    private final Command command;
    private final List<String> rollbackUsers;
    private final List<Object> blockList;
    private final Map<Object, Boolean> excludedBlocks;
    private final List<String> excludedUsers;
    private final List<Integer> actions;
    private final Integer[] radius;
    private final Location location;
    private final int x;
    private final int y;
    private final int z;
    private final int worldId;
    private final int argWorldId;
    private final long timeStart;
    private final long timeEnd;
    private final int noisy;
    private final int excluded;
    private final int restricted;
    private final int page;
    private final int displayResults;
    private final int typeLookup;
    private final String rtime;
    private final boolean count;

    public StandardLookupThread(CommandSender player, Command command, List<String> rollbackUsers, List<Object> blockList, Map<Object, Boolean> excludedBlocks, List<String> excludedUsers, List<Integer> actions, Integer[] radius, Location location, int x, int y, int z, int worldId, int argWorldId, long timeStart, long timeEnd, int noisy, int excluded, int restricted, int page, int displayResults, int typeLookup, String rtime, boolean count) {
        this.player = player;
        this.command = command;
        this.rollbackUsers = rollbackUsers;
        this.blockList = blockList;
        this.excludedBlocks = excludedBlocks;
        this.excludedUsers = excludedUsers;
        this.actions = actions;
        this.radius = radius;
        this.location = location;
        this.x = x;
        this.y = y;
        this.z = z;
        this.worldId = worldId;
        this.argWorldId = argWorldId;
        this.timeStart = timeStart;
        this.timeEnd = timeEnd;
        this.noisy = noisy;
        this.excluded = excluded;
        this.restricted = restricted;
        this.page = page;
        this.displayResults = displayResults;
        this.typeLookup = typeLookup;
        this.rtime = rtime;
        this.count = count;
    }

    @Override
    public void run() {
        try (Connection connection = Database.getConnection(true)) {
            ConfigHandler.lookupThrottle.put(player.getName(), new Object[] { true, System.currentTimeMillis() });

            List<String> uuidList = new ArrayList<>();
            Location finalLocation = location;
            boolean exists = false;
            String bc = x + "." + y + "." + z + "." + worldId + "." + timeStart + "." + timeEnd + "." + noisy + "." + excluded + "." + restricted + "." + argWorldId + "." + displayResults;
            ConfigHandler.lookupCommand.put(player.getName(), bc);
            ConfigHandler.lookupPage.put(player.getName(), page);
            ConfigHandler.lookupTime.put(player.getName(), rtime);
            ConfigHandler.lookupType.put(player.getName(), 5);
            ConfigHandler.lookupElist.put(player.getName(), excludedBlocks);
            ConfigHandler.lookupEUserlist.put(player.getName(), excludedUsers);
            ConfigHandler.lookupBlist.put(player.getName(), blockList);
            ConfigHandler.lookupUlist.put(player.getName(), rollbackUsers);
            ConfigHandler.lookupAlist.put(player.getName(), actions);
            ConfigHandler.lookupRadius.put(player.getName(), radius);

            if (connection != null) {
                Statement statement = connection.createStatement();
                String baduser = "";
                for (String check : rollbackUsers) {
                    if ((!check.equals(HASHTAG_GLOBAL) && !check.equals(HASHTAG_CONTAINER)) || actions.contains(LookupAction.USERNAME)) {
                        exists = PlayerLookup.playerExists(connection, check);
                        if (!exists) {
                            baduser = check;
                            break;
                        }
                        else if (actions.contains(LookupAction.USERNAME) && ConfigHandler.uuidCache.get(check.toLowerCase(Locale.ROOT)) != null) {
                            String uuid = ConfigHandler.uuidCache.get(check.toLowerCase(Locale.ROOT));
                            uuidList.add(uuid);
                        }
                    }
                    else {
                        exists = true;
                    }
                }
                if (exists) {
                    for (String check : excludedUsers) {
                        if (!check.equals(HASHTAG_GLOBAL) && !check.equals(HASHTAG_HOPPER)) {
                            exists = PlayerLookup.playerExists(connection, check);
                            if (!exists) {
                                baduser = check;
                                break;
                            }
                        }
                        else if (check.equals(HASHTAG_GLOBAL)) {
                            baduser = HASHTAG_GLOBAL;
                            exists = false;
                        }
                    }
                }

                if (exists) {
                    List<String> userList = new ArrayList<>();
                    if (!actions.contains(LookupAction.USERNAME)) {
                        userList = rollbackUsers;
                    }

                    int unixtimestamp = (int) (System.currentTimeMillis() / 1000L);
                    boolean restrictWorld = false;
                    if (radius != null) {
                        restrictWorld = true;
                    }
                    if (finalLocation == null) {
                        restrictWorld = false;
                    }
                    if (argWorldId > 0) {
                        restrictWorld = true;
                        finalLocation = new Location(Bukkit.getServer().getWorld(WorldUtils.getWorldName(argWorldId)), x, y, z);
                    }
                    else if (finalLocation != null) {
                        finalLocation = new Location(Bukkit.getServer().getWorld(WorldUtils.getWorldName(worldId)), x, y, z);
                    }

                    Long[] rowData = new Long[] { 0L, 0L, 0L, 0L };
                    long rowMax = (long) page * displayResults;
                    long pageStart = rowMax - displayResults;
                    long rows = 0L;
                    boolean checkRows = true;

                    if (typeLookup == 5 && page > 1) {
                        rowData = ConfigHandler.lookupRows.get(player.getName());
                        rows = rowData[3];

                        if (pageStart < rows) {
                            checkRows = false;
                        }
                    }

                    if (checkRows) {
                        rows = Lookup.countLookupRows(statement, player, uuidList, userList, blockList, excludedBlocks, excludedUsers, actions, finalLocation, radius, rowData, timeStart, timeEnd, restrictWorld, true);
                        rowData[3] = rows;
                        ConfigHandler.lookupRows.put(player.getName(), rowData);
                    }
                    if (count) {
                        String rowFormat = NumberFormat.getInstance().format(rows);
                        Chat.sendMessage(player, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.LOOKUP_ROWS_FOUND, rowFormat, (rows == 1 ? Selector.FIRST : Selector.SECOND)));
                    }
                    else if (pageStart < rows) {
                        List<String[]> lookupList = Lookup.performPartialLookup(statement, player, uuidList, userList, blockList, excludedBlocks, excludedUsers, actions, finalLocation, radius, rowData, timeStart, timeEnd, (int) pageStart, displayResults, restrictWorld, true);

                        Chat.sendMessage(player, Color.WHITE + "----- " + Color.DARK_AQUA + Phrase.build(Phrase.LOOKUP_HEADER, "CoreProtect" + Color.WHITE + " | " + Color.DARK_AQUA) + Color.WHITE + " -----");

                        LookupFormatter formatter = LookupFormatter.selectFormatter(player, command, actions);
                        formatter.formatResults(lookupList, unixtimestamp, connection);

                        if (rows > displayResults) {
                            int totalPages = (int) Math.ceil(rows / (displayResults + 0.0));
                            if (formatter.needsSeparator()) {
                                Chat.sendMessage(player, "-----");
                            }
                            Chat.sendComponent(player, ChatUtils.getPageNavigation(command.getName(), page, totalPages));
                        }
                    }
                    else if (rows > 0) {
                        Chat.sendMessage(player, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.NO_RESULTS_PAGE, Selector.FIRST));
                    }
                    else {
                        Chat.sendMessage(player, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.NO_RESULTS));
                    }
                }
                else {
                    Chat.sendMessage(player, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.USER_NOT_FOUND, baduser));
                }
                statement.close();
            }
            else {
                Chat.sendMessage(player, Color.DARK_AQUA + "CoreProtect " + Color.WHITE + "- " + Phrase.build(Phrase.DATABASE_BUSY));
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        ConfigHandler.lookupThrottle.put(player.getName(), new Object[] { false, System.currentTimeMillis() });
    }
}
