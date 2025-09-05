package com.masecla.api;

import java.util.List;
import java.util.UUID;
import org.bukkit.inventory.ItemStack;

public interface HeadDatabaseAPI {
    ItemStack getItemHead(String id);
    List<ItemStack> getHeads();
    List<String> getCategoryNames();
    List<ItemStack> getHeads(String category);
    List<ItemStack> getHeads(UUID player);
    List<ItemStack> getHeadsByTag(String tag);
    String getHeadID(ItemStack item);
    boolean isHead(ItemStack item);
    void search(UUID player, String query);
}
