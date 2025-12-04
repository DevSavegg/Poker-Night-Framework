package studio.devsavegg.game;

import studio.devsavegg.core.*;

import java.util.List;

/**
 * Interface for abstracting how we get input from players (Console, GUI, Network).
 * <p>
 * # Principle - Dependency Inversion Principle (DIP): High-level modules depend on this abstraction for input, not concrete UI classes.
 */
public interface IPlayerInput {
    /**
     * Request an action from a player.
     * <p>
     * # Design - Strategy: Defines the interface for acquiring player decisions.
     *
     * @param player The player who needs to act.
     * @param context The current game state (snapshot).
     * @param legalActions A list of valid actions the player can take.
     * @return The action the player chose.
     */
    PlayerAction requestAction(Player player, GameContext context, List<ActionType> legalActions);

    /**
     * Request a discard decision from a player.
     * <p>
     * Used in draw poker variants.
     * <p>
     * # Design - Strategy: Defines the interface for acquiring discard decisions.
     *
     * @param player The player requesting the discard.
     * @param context The current game context.
     * @return A list of cards to discard.
     */
    List<Card> requestDiscard(Player player, GameContext context);
}