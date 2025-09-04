package fr.rudy.party.menu;

import fr.rudy.party.Main;
import fr.rudy.party.manager.PartyManager;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.List;

public class PartyMenu implements Listener {

    private final PartyManager partyManager;
    private final Main plugin = Main.get();

    public PartyMenu(PartyManager partyManager) {
        this.partyManager = partyManager;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void open(Player viewer) {
        String title = PlaceholderAPI.setPlaceholders(viewer, "%nexo_shift_-48%<glyph:party>");
        Inventory inv = Bukkit.createInventory(null, 45, title);

        List<Player> members = partyManager.getPartyMembers(viewer);

        ItemStack back = new ItemStack(Material.PAPER);
        ItemMeta backMeta = back.getItemMeta();
        if (backMeta != null) {
            backMeta.setDisplayName("§7Retour");
            backMeta.setCustomModelData(10233);
            back.setItemMeta(backMeta);
        }
        inv.setItem(10, back);

        int slot = 19;
        for (Player member : members) {
            if (slot >= 44) break;
            ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta sm = (SkullMeta) skull.getItemMeta();
            if (sm != null) {
                sm.setOwningPlayer(member);
                sm.setDisplayName("§f" + member.getName());
                skull.setItemMeta(sm);
            }
            inv.setItem(slot++, skull);
        }

        ItemStack invite = new ItemStack(Material.PAPER);
        ItemMeta inviteMeta = invite.getItemMeta();
        if (inviteMeta != null) {
            inviteMeta.setDisplayName(members.isEmpty()
                    ? "§aCréer un groupe"
                    : "§aInviter un joueur");
            inviteMeta.setCustomModelData(10233);
            invite.setItemMeta(inviteMeta);
        }
        inv.setItem(15, invite);

        if (!members.isEmpty() && (members.size() > 1 || partyManager.isLeader(viewer))) {
            ItemStack leave = new ItemStack(Material.PAPER);
            ItemMeta leaveMeta = leave.getItemMeta();
            if (leaveMeta != null) {
                leaveMeta.setDisplayName("§cQuitter le groupe");
                leaveMeta.setCustomModelData(10233);
                leave.setItemMeta(leaveMeta);
            }
            inv.setItem(16, leave);
        }

        viewer.openInventory(inv);
    }

    private void openInviteMenu(Player viewer) {
        if (!partyManager.isLeader(viewer)) {
            viewer.sendMessage("<glyph:error> §cSeul le chef de groupe peut inviter.");
            return;
        }

        String title = PlaceholderAPI.setPlaceholders(viewer, "%nexo_shift_-48%<glyph:party_list>");
        Inventory inv = Bukkit.createInventory(null, 45, title);

        ItemStack back = new ItemStack(Material.PAPER);
        ItemMeta backMeta = back.getItemMeta();
        if (backMeta != null) {
            backMeta.setDisplayName("§7Retour");
            backMeta.setCustomModelData(10233);
            back.setItemMeta(backMeta);
        }
        inv.setItem(10, back);

        int slot = 19;
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (slot >= 44 || online.equals(viewer)) continue;
            ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta sm = (SkullMeta) skull.getItemMeta();
            if (sm != null) {
                sm.setOwningPlayer(online);
                sm.setDisplayName("§f" + online.getName());
                skull.setItemMeta(sm);
            }
            inv.setItem(slot++, skull);
        }

        viewer.openInventory(inv);
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player viewer)) return;
        if (e.getClickedInventory() == null) return;

        String expectedTitle = PlaceholderAPI.setPlaceholders(viewer, "%nexo_shift_-48%<glyph:party>");
        if (!e.getView().getTitle().equals(expectedTitle)) return;
        if (!e.getInventory().equals(e.getClickedInventory())) return;

        ItemStack item = e.getCurrentItem();
        if (item == null || item.getItemMeta() == null) return;

        e.setCancelled(true);
        String name = item.getItemMeta().getDisplayName();

        if (name.contains("Retour")) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "dmenu open dungeons " + viewer.getName());
            viewer.closeInventory();
            return;
        }

        if (name.contains("Quitter")) {
            partyManager.leave(viewer);
            viewer.sendMessage("<glyph:info> §bTu as quitté ton groupe.");
            open(viewer);
            return;
        }

        if (item.getType() == Material.PAPER) {
            if (name.contains("Créer un groupe")) {
                boolean created = partyManager.createParty(viewer);
                viewer.sendMessage(created
                        ? "<glyph:info> §bTon groupe a été créé."
                        : "<glyph:error> §cTu fais déjà partie d'un groupe.");
                open(viewer);
            } else if (name.contains("Inviter un joueur")) {
                openInviteMenu(viewer);
            }
        }
    }

    @EventHandler
    public void onInviteClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player viewer)) return;
        if (e.getClickedInventory() == null) return;

        String expectedTitle = PlaceholderAPI.setPlaceholders(viewer, "%nexo_shift_-48%<glyph:party_list>");
        if (!e.getView().getTitle().equals(expectedTitle)) return;
        if (!e.getInventory().equals(e.getClickedInventory())) return;

        ItemStack item = e.getCurrentItem();
        if (item == null || item.getItemMeta() == null) return;

        e.setCancelled(true);
        String name = item.getItemMeta().getDisplayName();

        if (name.contains("Retour")) {
            open(viewer);
            return;
        }

        if (!partyManager.isLeader(viewer)) {
            viewer.sendMessage("<glyph:error> §cSeul le chef du groupe peut inviter.");
            viewer.closeInventory();
            return;
        }

        if (item.getType() == Material.PLAYER_HEAD) {
            String playerName = name.replace("§f", "");
            Player target = Bukkit.getPlayer(playerName);
            if (target != null) {
                partyManager.invite(viewer, target);
                viewer.sendMessage("<glyph:info> §bInvitation envoyée à " + playerName + ".");
            }
            viewer.closeInventory();
        }
    }
}
