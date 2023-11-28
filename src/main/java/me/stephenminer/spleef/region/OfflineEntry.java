package me.stephenminer.spleef.region;

import me.stephenminer.spleef.Items;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

/**
 * Record of player who quit while a game is running.
 * @param uuid Uuid of player
 * @param items Inventory of player with uuid
 */
public record OfflineEntry(UUID uuid, ItemStack[] items) {
}
