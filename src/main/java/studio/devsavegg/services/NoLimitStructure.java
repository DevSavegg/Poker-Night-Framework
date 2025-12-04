package studio.devsavegg.services;

import studio.devsavegg.core.ActionType;
import studio.devsavegg.core.GameContext;
import studio.devsavegg.core.PlayerAction;

/**
 * Implementation of a No-Limit betting structure.
 * <p>
 * # Principle - Liskov Substitution Principle (LSP): Substituted for IBettingStructure to provide No-Limit specific logic.
 */
public class NoLimitStructure implements IBettingStructure {

    /**
     * Calculates the minimum legal raise amount.
     * <p>
     * # Design - Strategy: Concrete algorithm for No-Limit raise rules.
     *
     * @param context The current {@link GameContext}.
     * @return The minimum raise total.
     */
    @Override
    public int calculateMinRaise(GameContext context) {
        // CORRECTION: The minimum valid raise total is the Current Bet + The Raise Increment
        return context.getCurrentBet() + context.getMinRaise();
    }

    /**
     * Calculates the maximum legal bet amount.
     * <p>
     * In No-Limit, this is effectively infinite (bounded by player stack).
     * <p>
     * # Design - Strategy: Concrete algorithm for max bet.
     *
     * @param context The current {@link GameContext}.
     * @return {@link Integer#MAX_VALUE}.
     */
    @Override
    public int calculateMaxBet(GameContext context) {
        return Integer.MAX_VALUE;
    }

    /**
     * Validates if a specific player action is legal within No-Limit rules.
     * <p>
     * # Design - Validator: Validates input based on business rules.
     *
     * @param action The {@link PlayerAction} to validate.
     * @param context The current {@link GameContext}.
     * @return {@code true} if valid.
     */
    @Override
    public boolean isBetValid(PlayerAction action, GameContext context) {
        if (action.getType() == ActionType.FOLD) return true;

        int playerChips = action.getPlayer().getChipStack();
        int amount = action.getAmount();

        // 1. You cannot bet more than you have
        if (amount > playerChips) return false;

        // 2. Raise Logic
        if (action.getType() == ActionType.RAISE) {
            int minTotal = calculateMinRaise(context);
            // Valid if: They cover the min raise OR they are going All-In (insufficient chips for full raise)
            return amount >= minTotal || amount == playerChips;
        }
        return true;
    }
}