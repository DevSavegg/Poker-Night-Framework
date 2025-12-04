package studio.devsavegg.services;

import studio.devsavegg.core.HandRank;
import studio.devsavegg.core.Player;

import java.util.*;

/**
 * Standard implementation of a Pot Manager for handling chips, main pots, and side pots.
 * <p>
 * # Principle - Single Responsibility Principle (SRP): Manages the financial state of the game round (pots and splits).
 */
public class DefaultPotManager implements IPotManager {
    private final List<SidePot> pots;
    // Tracks how much each player has put into the pot for the *current hand* total.
    private final Map<Player, Integer> currentHandContributions;
    private int totalPotSize;

    /**
     * Initializes a new DefaultPotManager.
     * <p>
     * # Design - Constructor: Sets up the initial empty state.
     */
    public DefaultPotManager() {
        this.pots = new ArrayList<>();
        this.currentHandContributions = new HashMap<>();
        this.totalPotSize = 0;
    }

    /**
     * Resets internal state for a new hand.
     * <p>
     * Clears all existing pots and resets player contributions.
     * <p>
     * # Design - State: Resets the manager to a clean state for a new cycle.
     *
     * @param activePlayers The list of players participating in the new hand.
     */
    @Override
    public void startNewHand(List<Player> activePlayers) {
        pots.clear();
        currentHandContributions.clear();
        totalPotSize = 0;

        // Initialize contributions for all active players to 0
        for (Player p : activePlayers) {
            currentHandContributions.put(p, 0);
        }
    }

    /**
     * Processes a bet from a player, updating their contribution to the current pot.
     * <p>
     * # Design - Command: Executes the logic to add chips to the pool.
     *
     * @param player The player making the bet.
     * @param amount The amount of chips being added.
     */
    @Override
    public void processBet(Player player, int amount) {
        // Update the global tracker for this player's total contribution in this hand
        currentHandContributions.put(player, currentHandContributions.getOrDefault(player, 0) + amount);

        // Update total pot size
        totalPotSize += amount;
    }

    /**
     * Retrieves the total chips currently in all pots.
     * <p>
     * # Design - Accessor: Gets the total pot size.
     *
     * @return The integer total of all chips.
     */
    @Override
    public int getCurrentTotal() {
        return totalPotSize;
    }

    /**
     * Re-calculates the main pot and side pots based on total contributions.
     * <p>
     * This must be called at the end of betting rounds or before resolving pots
     * to ensure "All-In" splits are handled correctly.
     * <p>
     * # Design - Algorithm: Complex logic for distributing chips into main and side pots.
     */
    public void calculateSidePots() {
        pots.clear();

        // Create a working copy of contributions so we can decrement them as we build pots
        Map<Player, Integer> remainingContribs = new HashMap<>(currentHandContributions);

        // Remove players with 0 contribution from calculation (sat out or just started)
        remainingContribs.entrySet().removeIf(entry -> entry.getValue() == 0);

        while (!remainingContribs.isEmpty()) {
            // Find the smallest non-zero contribution among players who have NOT folded.
            //    Folded players' money is "dead" and just fills the pot with the smallest active stack.
            int minStack = Integer.MAX_VALUE;
            boolean activePlayerFound = false;

            for (Map.Entry<Player, Integer> entry : remainingContribs.entrySet()) {
                Player p = entry.getKey();
                int amount = entry.getValue();

                if (!p.isFolded()) {
                    if (amount < minStack) {
                        minStack = amount;
                    }
                    activePlayerFound = true;
                }
            }

            // Edge Case: If only folded players have money left (everyone else is all-in or covered),
            // take the max remaining folded money to finish the pot.
            if (!activePlayerFound) {
                minStack = remainingContribs.values().stream().mapToInt(Integer::intValue).max().orElse(0);
            }

            // Create a new Side Pot (or Main Pot if it's the first one)
            SidePot currentPot = new SidePot();

            // Collect chips from everyone
            List<Player> fullyProcessedPlayers = new ArrayList<>();

            for (Map.Entry<Player, Integer> entry : remainingContribs.entrySet()) {
                Player p = entry.getKey();
                int available = entry.getValue();

                // Take up to 'minStack' from this player
                int contribution = Math.min(available, minStack);

                currentPot.addChips(contribution);

                // Reduce their remaining contribution
                remainingContribs.put(p, available - contribution);

                // If they have 0 left, mark for removal from the map
                if (available - contribution == 0) {
                    fullyProcessedPlayers.add(p);
                }

                // Eligibility: If they are not folded, they can win this pot.
                //    (Folded players contribute money but are not added to eligiblePlayers)
                if (!p.isFolded() && contribution > 0) {
                    currentPot.addEligiblePlayer(p);
                }
            }

            // Remove players who have no money left to allocate
            for (Player p : fullyProcessedPlayers) {
                remainingContribs.remove(p);
            }

            // Add this pot to list
            if (currentPot.getAmount() > 0) {
                pots.add(currentPot);
            }
        }
    }

    /**
     * Distributes the pots to winners based on hand ranks.
     * <p>
     * Handles main pot and all side pots separately.
     * <p>
     * # Design - Algorithm: Logic for determining winners and splitting chips.
     *
     * @param showdownResults A map of players to their final hand ranks.
     * @return A map of {@link Player} to the amount of chips won.
     */
    @Override
    public Map<Player, Integer> resolvePots(Map<Player, HandRank> showdownResults) {
        // Ensure pots are calculated correctly before resolving
        calculateSidePots();

        Map<Player, Integer> totalWinnings = new HashMap<>();

        // Iterate through all created pots
        for (SidePot pot : pots) {
            if (pot.getAmount() == 0) continue;

            // Filter candidates: Must be in the showdown AND eligible for this specific pot
            List<Player> candidates = showdownResults.keySet().stream()
                    .filter(pot::isEligible)
                    .toList();

            if (candidates.isEmpty()) {
                continue;
            }

            // Find the strongest hand among candidates
            HandRank bestRank = showdownResults.get(candidates.get(0));
            for (Player p : candidates) {
                HandRank r = showdownResults.get(p);
                if (r.compareTo(bestRank) > 0) {
                    bestRank = r;
                }
            }

            // Find all players who match that best rank (tie handling)
            List<Player> winners = new ArrayList<>();
            for (Player p : candidates) {
                if (showdownResults.get(p).compareTo(bestRank) == 0) {
                    winners.add(p);
                }
            }

            // Distribute Chips
            int share = pot.getAmount() / winners.size();
            int remainder = pot.getAmount() % winners.size();

            for (Player winner : winners) {
                totalWinnings.put(winner, totalWinnings.getOrDefault(winner, 0) + share);
            }

            // Give remainder (odd chip) to the first winner
            if (!winners.isEmpty() && remainder > 0) {
                Player luckyOne = winners.get(0);
                totalWinnings.put(luckyOne, totalWinnings.getOrDefault(luckyOne, 0) + remainder);
            }
        }

        return totalWinnings;
    }
}