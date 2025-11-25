package studio.devsavegg.core;

/**
 * A record of a specific action taken by a player at a specific time.
 */
public class PlayerAction {
    private final Player player;
    private final ActionType type;
    private final int amount; // For Check/Fold, this is 0
    private final long timestamp;

    public PlayerAction(Player player, ActionType type, int amount) {
        this.player = player;
        this.type = type;
        this.amount = amount;
        this.timestamp = System.currentTimeMillis();
    }

    public Player getPlayer() {
        return player;
    }

    public ActionType getType() {
        return type;
    }

    public int getAmount() {
        return amount;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return String.format("[%tT] %s: %s %d", timestamp, player.getName(), type, amount);
    }
}