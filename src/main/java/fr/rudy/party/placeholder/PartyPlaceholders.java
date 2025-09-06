package fr.rudy.party.placeholder;

import fr.rudy.party.manager.PartyManager;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PartyPlaceholders extends PlaceholderExpansion {

    private final PartyManager partyManager;

    public PartyPlaceholders(PartyManager partyManager) {
        this.partyManager = partyManager;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "party";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Rudy";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) return "";

        List<Player> members = partyManager.getPartyMembers(player);

        if (params.equalsIgnoreCase("count")) {
            return String.valueOf(members.size());
        }

        if (params.toLowerCase().startsWith("member")) {
            try {
                int index = Integer.parseInt(params.substring(6)) - 1;
                if (index >= 0 && index < members.size()) {
                    return members.get(index).getName();
                } else {
                    return "";
                }
            } catch (NumberFormatException e) {
                return "";
            }
        }

        return null;
    }
}
