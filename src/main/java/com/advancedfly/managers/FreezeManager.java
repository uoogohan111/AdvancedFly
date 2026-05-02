package com.advancedfly.managers;

import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * FreezeManager tracks which players are currently frozen.
 *
 * A frozen player:
 *  - Cannot move (velocity zeroed on PlayerMoveEvent)
 *  - Cannot interact or place/break blocks (handled by FreezeListener)
 */
public class FreezeManager {

    private final Set<UUID> frozenPlayers = new HashSet<>();

    /** Toggle freeze state. Returns true if player is now frozen. */
    public boolean toggleFreeze(Player player) {
        UUID uuid = player.getUniqueId();
        if (frozenPlayers.contains(uuid)) {
            frozenPlayers.remove(uuid);
            return false;
        } else {
            frozenPlayers.add(uuid);
            return true;
        }
    }

    /** Returns true if the player is frozen. */
    public boolean isFrozen(Player player) {
        return frozenPlayers.contains(player.getUniqueId());
    }

    /** Removes freeze on disconnect so state doesn't persist incorrectly. */
    public void unfreeze(UUID uuid) {
        frozenPlayers.remove(uuid);
    }

    /** Returns an unmodifiable view of all frozen UUIDs. */
    public Set<UUID> getFrozenPlayers() {
        return Collections.unmodifiableSet(frozenPlayers);
    }
}
