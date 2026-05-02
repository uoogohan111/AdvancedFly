package com.advancedfly.gui;

import com.advancedfly.AdvancedFlyPlugin;
import com.advancedfly.managers.ConfigManager;
import com.advancedfly.managers.FlyManager;
import com.advancedfly.managers.SpeedDataManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.UUID;

/**
 * FlyGuiMenu creates a simple 3-row chest inventory GUI that lets players:
 *  - Toggle flight on/off  (slot 11)
 *  - Decrease fly speed    (slot 13)
 *  - Increase fly speed    (slot 15)
 *
 * The menu registers itself as a temporary listener and cleans up
 * once the inventory is closed.
 */
public class FlyGuiMenu implements Listener {

    private static final int SLOT_TOGGLE        = 11;
    private static final int SLOT_SPEED_DOWN    = 13;
    private static final int SLOT_SPEED_DISPLAY = 14;
    private static final int SLOT_SPEED_UP      = 15;

    private final AdvancedFlyPlugin plugin;
    private final FlyManager        flyManager;
    private final ConfigManager     config;
    private       Inventory         inventory;
    private final UUID              ownerUuid;

    public FlyGuiMenu(AdvancedFlyPlugin plugin,
                      FlyManager flyManager,
                      ConfigManager config) {
        this.plugin     = plugin;
        this.flyManager = flyManager;
        this.config     = config;
        this.ownerUuid  = null; // set per-open in open()
    }

    /** Builds and opens the GUI for the given player. */
    public void open(Player player) {
        Inventory inv = buildInventory(player);
        // Register this instance as a listener for click events
        Bukkit.getPluginManager().registerEvents(new GuiListener(player, inv, this), plugin);
        player.openInventory(inv);
    }

    // ── Builder ────────────────────────────────────────────────────────────────

    private Inventory buildInventory(Player player) {
        Component title = Component.text("✈ AdvancedFly Menu", NamedTextColor.DARK_AQUA);
        Inventory inv   = Bukkit.createInventory(null, 27, title);

        // Fill background with grey glass
        ItemStack filler = makeItem(Material.GRAY_STAINED_GLASS_PANE,
                Component.text(" ").decoration(TextDecoration.ITALIC, false));
        for (int i = 0; i < 27; i++) inv.setItem(i, filler);

        // Toggle button
        boolean flying  = flyManager.isFlying(player);
        Material toggleMat = flying ? Material.LIME_DYE : Material.GRAY_DYE;
        Component toggleName = flying
                ? Component.text("✔ Flight ON",  NamedTextColor.GREEN)
                : Component.text("✘ Flight OFF", NamedTextColor.RED);
        inv.setItem(SLOT_TOGGLE, makeItem(toggleMat, toggleName,
                List.of(Component.text("Click to toggle", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false))));

        // Speed display
        int userSpeed = SpeedDataManager.toUserSpeed(
                plugin.getSpeedDataManager().getFlySpeed(player.getUniqueId()));
        inv.setItem(SLOT_SPEED_DISPLAY, makeItem(Material.FEATHER,
                Component.text("Fly Speed: " + userSpeed, NamedTextColor.AQUA)
                        .decoration(TextDecoration.ITALIC, false)));

        // Speed down
        inv.setItem(SLOT_SPEED_DOWN, makeItem(Material.RED_DYE,
                Component.text("◀ Speed -1", NamedTextColor.RED)
                        .decoration(TextDecoration.ITALIC, false)));

        // Speed up
        inv.setItem(SLOT_SPEED_UP, makeItem(Material.LIME_DYE,
                Component.text("Speed +1 ▶", NamedTextColor.GREEN)
                        .decoration(TextDecoration.ITALIC, false)));

        return inv;
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private ItemStack makeItem(Material mat, Component name) {
        return makeItem(mat, name, List.of());
    }

    private ItemStack makeItem(Material mat, Component name, List<Component> lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta  meta = item.getItemMeta();
        meta.displayName(name.decoration(TextDecoration.ITALIC, false));
        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    // ── Inner listener ─────────────────────────────────────────────────────────

    /**
     * Scoped listener that handles clicks only for the owner's inventory
     * and unregisters when the inventory is closed.
     */
    public class GuiListener implements Listener {

        private final Player    owner;
        private final Inventory ownedInv;
        private final FlyGuiMenu menu;

        public GuiListener(Player owner, Inventory inv, FlyGuiMenu menu) {
            this.owner    = owner;
            this.ownedInv = inv;
            this.menu     = menu;
        }

        @EventHandler
        public void onClick(InventoryClickEvent event) {
            if (!event.getInventory().equals(ownedInv)) return;
            if (!(event.getWhoClicked() instanceof Player clicker)) return;
            if (!clicker.equals(owner)) return;

            event.setCancelled(true); // prevent item theft

            int slot = event.getRawSlot();

            if (slot == SLOT_TOGGLE) {
                boolean isAdmin = owner.hasPermission("advancedfly.admin");
                flyManager.toggleFly(owner, null, isAdmin);
                // Refresh the GUI
                owner.closeInventory();
                menu.open(owner);
                return;
            }

            SpeedDataManager speedData = plugin.getSpeedDataManager();
            int currentUserSpeed = SpeedDataManager.toUserSpeed(
                    speedData.getFlySpeed(owner.getUniqueId()));

            if (slot == SLOT_SPEED_DOWN && currentUserSpeed > 1) {
                float newSpeed = speedData.setFlySpeed(owner.getUniqueId(), currentUserSpeed - 1);
                owner.setFlySpeed(newSpeed);
                owner.closeInventory();
                menu.open(owner);
            } else if (slot == SLOT_SPEED_UP && currentUserSpeed < 10) {
                float newSpeed = speedData.setFlySpeed(owner.getUniqueId(), currentUserSpeed + 1);
                owner.setFlySpeed(newSpeed);
                owner.closeInventory();
                menu.open(owner);
            }
        }

        @EventHandler
        public void onClose(org.bukkit.event.inventory.InventoryCloseEvent event) {
            if (event.getInventory().equals(ownedInv)) {
                // Unregister this scoped listener to avoid memory leaks
                org.bukkit.event.HandlerList.unregisterAll(this);
            }
        }
    }
}
