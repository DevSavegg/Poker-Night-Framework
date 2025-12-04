package studio.devsavegg.services;

import studio.devsavegg.core.GameContext;
import studio.devsavegg.core.PlayerAction;

/**
 * Defines the rules for betting limits (e.g., No-Limit, Fixed-Limit).
 * <p>
 * # Principle - Strategy Pattern: Encapsulates valid betting behavior.
 */
public interface IBettingStructure {

    /**
     * Calculates the minimum legal raise amount based on the current game context.
     * <p>
     * # Design - Strategy: Helper method for the betting strategy.
     *
     * @param context The current {@link GameContext}.
     * @return The minimum total chips required to raise.
     */
    int calculateMinRaise(GameContext context);

    /**
     * Calculates the maximum legal bet amount.
     * <p>
     * # Design - Strategy: Helper method for the betting strategy.
     *
     * @param context The current {@link GameContext}.
     * @return The maximum bet allowed (Integer.MAX_VALUE for No-Limit).
     */
    int calculateMaxBet(GameContext context);

    /**
     * Validates if a specific player action is legal within this betting structure.
     * <p>
     * # Design - Validator / Strategy: Validation logic for the strategy.
     *
     * @param action The {@link PlayerAction} to validate.
     * @param context The current {@link GameContext}.
     * @return {@code true} if the action is valid, {@code false} otherwise.
     */
    boolean isBetValid(PlayerAction action, GameContext context);
}