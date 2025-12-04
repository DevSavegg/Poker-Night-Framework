package studio.devsavegg.services;

import studio.devsavegg.core.HandHistory;
import studio.devsavegg.core.Player;
import java.util.List;
import java.util.Map;

/**
 * Service interface for persisting game state and player data.
 * <p>
 * # Principle - Dependency Inversion Principle (DIP): Core logic depends on this abstraction for storage, not a database directly.
 */
public interface IGameStateService {

    /**
     * Saves the current chip counts of all players.
     * <p>
     * # Design - Data Access Object (DAO): Method for persisting data.
     *
     * @param players The list of players to save.
     */
    void savePlayerChips(List<Player> players);

    /**
     * Loads saved chip counts for players.
     * <p>
     * # Design - Data Access Object (DAO): Method for retrieving data.
     *
     * @return A map of Player IDs to chip counts.
     */
    Map<String, Integer> loadPlayerChips();

    /**
     * Logs the history of a completed hand.
     * <p>
     * # Design - Data Access Object (DAO): Method for archiving data.
     *
     * @param history The {@link HandHistory} object to save.
     */
    void logHand(HandHistory history);
}