package fr.rudy.party;

import fr.rudy.party.command.PartyCommand;
import fr.rudy.party.manager.PartyManager;
import fr.rudy.party.menu.PartyMenu;
import fr.rudy.party.placeholder.PartyPlaceholders;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {

    private static Main instance;
    private PartyManager partyManager;
    private PartyMenu partyMenu;

    public static Main get() {
        return instance;
    }

    public PartyManager getPartyManager() {
        return partyManager;
    }

    public PartyMenu getPartyMenu() {
        return partyMenu;
    }

    @Override
    public void onEnable() {
        instance = this;

        // Initialisation des composants
        partyManager = new PartyManager(this);
        partyMenu = new PartyMenu(partyManager);

        // Enregistrement de la commande /party
        if (getCommand("party") != null) {
            getCommand("party").setExecutor(new PartyCommand(partyManager, partyMenu));
        } else {
            //getLogger().warning("La commande 'party' n'est pas définie dans le plugin.yml !");
        }

        // Enregistrement des placeholders si PlaceholderAPI est présent
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new PartyPlaceholders(partyManager).register();
            //getLogger().info("✅ PlaceholderAPI détecté, les placeholders sont enregistrés !");
        } else {
            //getLogger().warning("⚠ PlaceholderAPI n’est pas installé, les placeholders ne fonctionneront pas.");
        }

        getLogger().info("✅ Plugin PartySystem activé !");
    }

    @Override
    public void onDisable() {
        getLogger().info("🛑 Plugin PartySystem désactivé proprement.");
    }
}
