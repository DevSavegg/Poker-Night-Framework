package studio.devsavegg.services;

import studio.devsavegg.core.Player;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents a specific pot that only certain players are eligible to win.
 * <p>
 * # Principle - Single Responsibility Principle (SRP): Encapsulates data for a single pot bucket.
 */
public class SidePot {
    private int amount;
    private final Set<Player> eligiblePlayers;

    /**
     * Initializes a new empty SidePot.
     * <p>
     * # Design - Constructor: Default initialization.
     */
    public SidePot() {
        this.amount = 0;
        this.eligiblePlayers = new HashSet<>();
    }

    /**
     * Adds chips to the pot.
     * <p>
     * # Design - Mutator: Adds to the accumulator.
     *
     * @param chips The amount to add.
     */
    public void addChips(int chips) {
        this.amount += chips;
    }

    /**
     * Adds a player to the list of eligible winners.
     * <p>
     * # Design - Mutator: Updates the eligibility set.
     *
     * @param p The player to add.
     */
    public void addEligiblePlayer(Player p) {
        this.eligiblePlayers.add(p);
    }

    /**
     * Retrieves the total amount in this pot.
     * <p>
     * # Design - Accessor: Gets the amount.
     *
     * @return The pot amount.
     */
    public int getAmount() {
        return amount;
    }

    /**
     * Retrieves the set of eligible players.
     * <p>
     * # Design - Accessor: Gets the players.
     *
     * @return A set of {@link Player}.
     */
    public Set<Player> getEligiblePlayers() {
        return eligiblePlayers;
    }

    /**
     * Checks if a player is eligible to win this pot.
     * <p>
     * # Design - Validator: Checks set membership.
     *
     * @param p The player to check.
     * @return {@code true} if eligible.
     */
    public boolean isEligible(Player p) {
        return eligiblePlayers.contains(p);
    }
}