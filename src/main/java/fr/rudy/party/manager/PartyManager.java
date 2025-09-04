package fr.rudy.party.manager;

import fr.rudy.party.Main;
import fr.rudy.party.NewHorizonParty;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.*;

public class PartyManager {

    private final Plugin plugin;
    private final Main main = Main.get();

    private final Map<UUID, NewHorizonParty> activeParties = new HashMap<>();
    private final Map<UUID, UUID> invitations = new HashMap<>();

    public PartyManager(Plugin plugin) {
        this.plugin = plugin;
    }

    public void invite(Player leader, Player target) {
        if (leader.getUniqueId().equals(target.getUniqueId())) return;

        invitations.put(target.getUniqueId(), leader.getUniqueId());
        target.sendMessage("<glyph:info> §b" + leader.getName() + " vous a invité à rejoindre son groupe.");
        target.sendMessage("<glyph:info> §bUtilisez /party join " + leader.getName() + " pour accepter.");
    }

    public boolean join(Player joiner, Player leader) {
        UUID leaderId = leader.getUniqueId();
        UUID joinerId = joiner.getUniqueId();

        if (!invitations.containsKey(joinerId) || !invitations.get(joinerId).equals(leaderId)) {
            return false;
        }

        leave(joiner);

        NewHorizonParty party = activeParties.computeIfAbsent(leaderId, id -> new NewHorizonParty(leader, plugin));
        party.addPlayer(joiner);
        invitations.remove(joinerId);

        for (Player member : party.getPlayers()) {
            member.sendMessage("<glyph:info> §b" + joiner.getName() + " a rejoint le groupe.");
        }

        return true;
    }

    public void leave(Player player) {
        UUID playerId = player.getUniqueId();

        Iterator<Map.Entry<UUID, NewHorizonParty>> it = activeParties.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, NewHorizonParty> entry = it.next();
            UUID leaderId = entry.getKey();
            NewHorizonParty party = entry.getValue();

            if (leaderId.equals(playerId)) {
                for (Player member : new ArrayList<>(party.getPlayers())) {
                    if (!member.getUniqueId().equals(playerId)) {
                        member.sendMessage("<glyph:error> §cLe groupe a été dissous car le chef l’a quitté.");
                    }
                }
                it.remove();
            } else if (party.getPlayers().contains(player)) {
                party.removePlayer(player);
                player.sendMessage("<glyph:info> §bTu as quitté le groupe.");
                for (Player member : party.getPlayers()) {
                    member.sendMessage("<glyph:info> §b" + player.getName() + " a quitté le groupe.");
                }
                if (party.getPlayers().isEmpty()) {
                    it.remove();
                }
            }
        }

        invitations.entrySet().removeIf(e -> e.getKey().equals(playerId) || e.getValue().equals(playerId));
    }

    public List<Player> getPartyMembers(Player player) {
        return activeParties.values().stream()
                .filter(p -> p.getPlayers().contains(player))
                .findFirst()
                .map(NewHorizonParty::getPlayers)
                .orElse(List.of());
    }

    public boolean isLeader(Player player) {
        return activeParties.containsKey(player.getUniqueId());
    }

    public boolean createParty(Player leader) {
        if (!isLeader(leader)) {
            activeParties.put(leader.getUniqueId(), new NewHorizonParty(leader, plugin));
            //leader.sendMessage("<glyph:info> §bGroupe créé avec succès.");
            return true;
        } else {
            leader.sendMessage("<glyph:error> §cTu fais déjà partie d'un groupe.");
            return false;
        }
    }
}
