package studio.devsavegg.services;

import studio.devsavegg.core.HandRank;
import studio.devsavegg.core.Player;
import java.util.List;
import java.util.Map;

/**
 * Interface for managing the collection and distribution of chips.
 * <p>
 * # Principle - Dependency Inversion Principle (DIP): Game Engine depends on this interface to handle money logic.
 */
public interface IPotManager {
    /**
     * Resets internal state for a new hand.
     * <p>
     * # Design - State: Transition to initial state.
     *
     * @param activePlayers The list of players in the hand.
     */
    void startNewHand(List<Player> activePlayers);

    /**
     * Processes a bet from a player, updating their contribution to the current pot.
     * <p>
     * # Design - Command: Execution of a betting command.
     *
     * @param player The player betting.
     * @param amount The amount bet.
     */
    void processBet(Player player, int amount);

    /**
     * Forces the manager to recalculate side pots based on current contributions.
     * <p>
     * This must be called at the end of betting rounds or before resolving pots
     * to ensure "All-In" splits are handled correctly.
     * <p>
     * # Design - Algorithm: Logic for splitting pots.
     */
    void calculateSidePots();

    /**
     * Distributes the pots to winners based on hand ranks.
     * <p>
     * Handles main pot and all side pots.
     * <p>
     * # Design - Algorithm: Logic for winner payout.
     *
     * @param showdownResults The results of the hand comparison.
     * @return A map of Player to the amount of chips won.
     */
    Map<Player, Integer> resolvePots(Map<Player, HandRank> showdownResults);

    /**
     * Gets the total chips currently in all pots.
     * <p>
     * # Design - Accessor: Gets total chips.
     *
     * @return The total chips.
     */
    int getCurrentTotal();
}