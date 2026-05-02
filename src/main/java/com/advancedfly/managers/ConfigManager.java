package com.advancedfly.managers;

import com.advancedfly.AdvancedFlyPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Particle;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

/**
 * ConfigManager centralises all access to config.yml values.
 *
 * Messages are stored as MiniMessage strings and resolved here to
 * Adventure {@link Component}s so no other class touches raw strings.
 */
public class ConfigManager {

    private final AdvancedFlyPlugin plugin;
    private final MiniMessage        mm = MiniMessage.miniMessage();

    // Cached values (reloaded in reload())
    private List<String> disabledWorlds;
    private int          flyCooldown;
    private int          defaultFlySpeed;
    private int          defaultWalkSpeed;
    private boolean      autoDisableOnSurvival;
    private boolean      autoEnableOnCreative;
    private boolean      restoreFlyOnJoin;
    private int          fallProtectionDuration;
    private boolean      actionBarEnabled;
    private boolean      particlesEnabled;
    private Particle     particleType;
    private boolean      economyEnabled;
    private double       economyCost;
    private boolean      savePerPlayerSpeed;

    public ConfigManager(AdvancedFlyPlugin plugin) {
        this.plugin = plugin;
        reload();
    }

    /** Re-reads config.yml from disk and updates all cached values. */
    public void reload() {
        plugin.reloadConfig();
        FileConfiguration cfg = plugin.getConfig();

        disabledWorlds         = cfg.getStringList("disabled-worlds");
        flyCooldown            = cfg.getInt("fly-cooldown", 5);
        defaultFlySpeed        = cfg.getInt("default-fly-speed", 5);
        defaultWalkSpeed       = cfg.getInt("default-walk-speed", 2);
        autoDisableOnSurvival  = cfg.getBoolean("auto-disable-on-survival", true);
        autoEnableOnCreative   = cfg.getBoolean("auto-enable-on-creative", true);
        restoreFlyOnJoin       = cfg.getBoolean("restore-fly-on-join", true);
        fallProtectionDuration = cfg.getInt("fall-protection-duration", 3);
        actionBarEnabled       = cfg.getBoolean("action-bar-enabled", true);
        particlesEnabled       = cfg.getBoolean("particles-enabled", true);
        economyEnabled         = cfg.getBoolean("economy-enabled", false);
        economyCost            = cfg.getDouble("economy-cost", 100.0);
        savePerPlayerSpeed     = cfg.getBoolean("save-per-player-speed", true);

        // Parse particle type safely
        String particleStr = cfg.getString("particle-type", "CLOUD");
        try {
            particleType = Particle.valueOf(particleStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Unknown particle type '" + particleStr + "', falling back to CLOUD.");
            particleType = Particle.CLOUD;
        }
    }

    // ── Message helpers ───────────────────────────────────────────────────────

    /**
     * Resolves a message key from messages section, prepending the prefix.
     *
     * @param key      dot-less key under messages (e.g. "fly-enabled")
     * @param resolvers optional MiniMessage tag resolvers for placeholders
     */
    public Component getMessage(String key, TagResolver... resolvers) {
        String prefix = plugin.getConfig().getString("messages.prefix", "");
        String raw    = plugin.getConfig().getString("messages." + key, "<red>Missing message: " + key);
        return mm.deserialize(prefix + raw, resolvers);
    }

    /**
     * Resolves a message without the prefix (e.g. action-bar text).
     */
    public Component getMessageNoPrefix(String key, TagResolver... resolvers) {
        String raw = plugin.getConfig().getString("messages." + key, "");
        return mm.deserialize(raw, resolvers);
    }

    // ── Convenience placeholder builders ─────────────────────────────────────

    public TagResolver speedTag(int speed) {
        return Placeholder.unparsed("speed", String.valueOf(speed));
    }

    public TagResolver playerTag(String name) {
        return Placeholder.unparsed("player", name);
    }

    public TagResolver secondsTag(long seconds) {
        return Placeholder.unparsed("seconds", String.valueOf(seconds));
    }

    public TagResolver amountTag(double amount) {
        return Placeholder.unparsed("amount", String.format("%.2f", amount));
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public List<String> getDisabledWorlds()      { return disabledWorlds; }
    public int          getFlyCooldown()          { return flyCooldown; }
    public int          getDefaultFlySpeed()      { return defaultFlySpeed; }
    public int          getDefaultWalkSpeed()     { return defaultWalkSpeed; }
    public boolean      isAutoDisableOnSurvival() { return autoDisableOnSurvival; }
    public boolean      isAutoEnableOnCreative()  { return autoEnableOnCreative; }
    public boolean      isRestoreFlyOnJoin()      { return restoreFlyOnJoin; }
    public int          getFallProtectionDuration(){ return fallProtectionDuration; }
    public boolean      isActionBarEnabled()      { return actionBarEnabled; }
    public boolean      isParticlesEnabled()      { return particlesEnabled; }
    public Particle     getParticleType()         { return particleType; }
    public boolean      isEconomyEnabled()        { return economyEnabled; }
    public double       getEconomyCost()          { return economyCost; }
    public boolean      isSavePerPlayerSpeed()    { return savePerPlayerSpeed; }
}
