package studio.devsavegg.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Stores the complete log of a specific hand for archival or replay.
 * <p>
 * # Principle - Single Responsibility Principle (SRP): This class logs the chronological events of a hand.
 */
public class HandHistory {
    private final String handId;
    private final List<PlayerAction> actions;
    private final Map<Player, HandRank> showdowns;
    private String winnerLog;

    /**
     * Constructs a new HandHistory record.
     * <p>
     * # Design - Constructor: Initializes the history log.
     *
     * @param handId The unique identifier for this hand.
     */
    public HandHistory(String handId) {
        this.handId = handId;
        this.actions = new ArrayList<>();
        this.showdowns = new HashMap<>();
    }

    /**
     * Records an action taken by a player.
     * <p>
     * # Design - Command: Stores an encapsulated action object for history.
     *
     * @param action The {@link PlayerAction} to record.
     */
    public void addAction(PlayerAction action) {
        this.actions.add(action);
    }

    /**
     * Records the hand rank of a player at showdown.
     * <p>
     * # Design - Memento: Captures the final state of a player's hand.
     *
     * @param player The player involved in the showdown.
     * @param rank The {@link HandRank} achieved by the player.
     */
    public void addShowdown(Player player, HandRank rank) {
        this.showdowns.put(player, rank);
    }

    /**
     * Sets the log message describing the winner.
     * <p>
     * # Design - Mutator: Updates the winner log.
     *
     * @param log The description of the win.
     */
    public void setWinnerLog(String log) {
        this.winnerLog = log;
    }

    /**
     * Retrieves the hand ID.
     * <p>
     * # Design - Accessor: Gets the hand ID.
     *
     * @return The unique hand identifier.
     */
    public String getHandId() { return handId; }

    /**
     * Retrieves the list of recorded actions.
     * <p>
     * # Design - Accessor: Gets the action list.
     *
     * @return A list of {@link PlayerAction}.
     */
    public List<PlayerAction> getActions() { return actions; }

    /**
     * Retrieves the showdown results.
     * <p>
     * # Design - Accessor: Gets the showdown map.
     *
     * @return A map of players to their hand ranks.
     */
    public Map<Player, HandRank> getShowdowns() { return showdowns; }

    /**
     * Retrieves the winner log.
     * <p>
     * # Design - Accessor: Gets the winner log.
     *
     * @return The winner log string.
     */
    public String getWinnerLog() { return winnerLog; }
}