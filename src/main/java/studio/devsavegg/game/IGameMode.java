package studio.devsavegg.game;

import studio.devsavegg.core.GameContext;
import studio.devsavegg.core.Player;
import studio.devsavegg.services.IBettingStructure;
import studio.devsavegg.services.IHandEvaluator;
import studio.devsavegg.services.IPotManager;

import java.util.List;
import java.util.Map;

/**
 * Defines the ruleset for a specific poker variant.
 * <p>
 * # Principle - Strategy Pattern: Encapsulates the varying algorithms (rules) for different game types.
 */
public interface IGameMode {
    /**
     * Retrieves the name of the game mode.
     * <p>
     * # Design - Accessor: Gets the mode name.
     *
     * @return The name (e.g., "Texas Hold'em").
     */
    String getName();

    /**
     * Retrieves the list of phases that define the game structure.
     * <p>
     * # Design - Template Method / Strategy: Provides the sequence of steps for the game engine.
     *
     * @return A list of {@link GamePhaseConfig}.
     */
    List<GamePhaseConfig> getStructure();

    /**
     * Retrieves the hand evaluator used by this game mode.
     * <p>
     * # Design - Abstract Factory / Strategy: Provides the specific strategy for hand evaluation.
     *
     * @return An {@link IHandEvaluator} instance.
     */
    IHandEvaluator getEvaluator();

    /**
     * Retrieves the betting structure rules (Limit, No-Limit, etc.).
     * <p>
     * # Design - Abstract Factory / Strategy: Provides the specific strategy for betting limits.
     *
     * @return An {@link IBettingStructure} instance.
     */
    IBettingStructure getBettingStructure();

    /**
     * Executes forced bets (Blinds, Antes) required by this game mode.
     * <p>
     * # Design - Command / Strategy: Executes specific logic for forced money contributions.
     *
     * @param table The table manager to identify positions (SB/BB).
     * @param potManager The pot manager to process bets.
     * @param context The current game context.
     * @param roundBetsTracker The map tracking bets for the current round.
     */
    void executeForcedBets(TableManager table, IPotManager potManager, GameContext context, Map<Player, Integer> roundBetsTracker);
}