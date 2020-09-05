package org.imanity.framework.bukkit.player;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.imanity.framework.bukkit.Imanity;
import org.imanity.framework.bukkit.player.event.PlayerDataLoadEvent;
import org.imanity.framework.data.DataHandler;
import org.imanity.framework.data.PlayerData;
import org.imanity.framework.bukkit.util.SampleMetadata;
import org.imanity.framework.data.store.StoreDatabase;
import org.imanity.framework.events.annotation.AutoWiredListener;
import org.imanity.framework.util.entry.Entry;
import org.imanity.framework.util.entry.EntryArrayList;

@AutoWiredListener
public class PlayerListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (Imanity.SHUTTING_DOWN) {
            return;
        }

        Player player = event.getPlayer();

        long time = System.currentTimeMillis();
        Imanity.TASK_CHAIN_FACTORY
                .newChain()
                .abortIf(ignored -> !player.isOnline())
                .async(object -> {
                    EntryArrayList<PlayerData, StoreDatabase> list = new EntryArrayList<>();
                    for (StoreDatabase database : DataHandler.getPlayerDatabases()) {
                        if (!database.autoLoad()) {
                            continue;
                        }

                        PlayerData playerData = (PlayerData) database.getByUuid(player.getUniqueId());

                        if (playerData == null) {
                            playerData = (PlayerData) database.load(player);
                        }

                        list.add(playerData, database);
                    }

                    return list;
                })
                .abortIf(ignored -> !player.isOnline())
                .sync(list -> {
                    for (Entry<PlayerData, StoreDatabase> entry : list) {
                        player.setMetadata(entry.getValue().getMetadataTag(), new SampleMetadata(entry.getKey()));

                        PlayerDataLoadEvent.callEvent(player, entry.getKey());
                    }
                    return null;
                })
                .abortIf(ignored -> !player.isOnline())
                .sync(() -> {
                    if (player.getScoreboard() == Bukkit.getScoreboardManager().getMainScoreboard()) {
                        player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
                    }

                    if (Imanity.BOARD_HANDLER != null) {
                        Imanity.BOARD_HANDLER.getOrCreateScoreboard(player);
                    }

                    if (Imanity.TAB_HANDLER != null) {
                        Imanity.TAB_HANDLER.registerPlayerTablist(player);
                    }

                    Imanity.LOGGER.info("Loaded PlayerData for " + player.getName() + " with " + (System.currentTimeMillis() - time) + "ms.");
                }).execute();
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (Imanity.SHUTTING_DOWN) {
            return;
        }

        Player player = event.getPlayer();

        Imanity.TASK_CHAIN_FACTORY
                .newChain()
                .async(() -> {
                    for (StoreDatabase database : DataHandler.getPlayerDatabases()) {
                        if (!database.autoSave()) {
                            return;
                        }
                        PlayerData playerData = database.getByPlayer(player);
                        database.save(playerData);
                    }
                }).sync(() -> {
                    for (StoreDatabase database : DataHandler.getPlayerDatabases()) {
                        player.removeMetadata(database.getMetadataTag(), Imanity.PLUGIN);

                        if (!database.autoSave()) {
                            return;
                        }
                        database.delete(player.getUniqueId());
                    }
                }).sync(() -> {
                    if (Imanity.BOARD_HANDLER != null) {
                        Imanity.BOARD_HANDLER.remove(player);
                    }

                    if (Imanity.TAB_HANDLER != null) {
                        Imanity.TAB_HANDLER.removePlayerTablist(player);
                    }
                }).execute();
    }

}
