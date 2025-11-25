package studio.devsavegg.services;

import studio.devsavegg.core.HandRank;
import studio.devsavegg.core.Player;
import java.util.List;
import java.util.Map;

public interface IPotManager {
    /**
     * Resets internal state for a new hand.
     */
    void startNewHand(List<Player> activePlayers);

    /**
     * Processes a bet from a player, updating their contribution to the current pot.
     */
    void processBet(Player player, int amount);

    /**
     * Forces the manager to recalculate side pots based on current contributions.
     * This must be called at the end of betting rounds or before resolving pots
     * to ensure "All-In" splits are handled correctly.
     */
    void calculateSidePots();

    /**
     * Distributes the pots to winners based on hand ranks.
     * Handles main pot and all side pots.
     * @return A map of Player to the amount of chips won.
     */
    Map<Player, Integer> resolvePots(Map<Player, HandRank> showdownResults);

    /**
     * Gets the total chips currently in all pots.
     */
    int getCurrentTotal();
}