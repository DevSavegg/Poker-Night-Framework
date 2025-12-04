package studio.devsavegg.core;

/**
 * A record of a specific action taken by a player at a specific time.
 * <p>
 * # Principle - Single Responsibility Principle (SRP): This class captures the immutable data of a single game event.
 */
public class PlayerAction {
    private final Player player;
    private final ActionType type;
    private final int amount; // For Check/Fold, this is 0
    private final long timestamp;

    /**
     * Creates a new immutable PlayerAction record.
     * <p>
     * # Design - Command: Encapsulates the request/action as a standalone object.
     *
     * @param player The {@link Player} performing the action.
     * @param type The {@link ActionType} of action.
     * @param amount The amount involved (0 for non-betting actions).
     */
    public PlayerAction(Player player, ActionType type, int amount) {
        this.player = player;
        this.type = type;
        this.amount = amount;
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * Retrieves the player who performed the action.
     * <p>
     * # Design - Accessor: Gets the player.
     *
     * @return The {@link Player} instance.
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * Retrieves the type of action performed.
     * <p>
     * # Design - Accessor: Gets the action type.
     *
     * @return The {@link ActionType}.
     */
    public ActionType getType() {
        return type;
    }

    /**
     * Retrieves the chip amount associated with the action.
     * <p>
     * # Design - Accessor: Gets the amount.
     *
     * @return The amount (chips).
     */
    public int getAmount() {
        return amount;
    }

    /**
     * Retrieves the timestamp of the action.
     * <p>
     * # Design - Accessor: Gets the timestamp.
     *
     * @return The time in milliseconds.
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Returns a string representation of the action.
     * <p>
     * # Design - String Representation: Formats the action for logs.
     *
     * @return A formatted string "[Time] Player: Action Amount".
     */
    @Override
    public String toString() {
        return String.format("[%tT] %s: %s %d", timestamp, player.getName(), type, amount);
    }
}