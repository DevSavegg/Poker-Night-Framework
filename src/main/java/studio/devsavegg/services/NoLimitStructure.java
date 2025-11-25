package studio.devsavegg.services;

import studio.devsavegg.core.ActionType;
import studio.devsavegg.core.GameContext;
import studio.devsavegg.core.PlayerAction;

public class NoLimitStructure implements IBettingStructure {

    @Override
    public int calculateMinRaise(GameContext context) {
        // CORRECTION: The minimum valid raise total is the Current Bet + The Raise Increment
        return context.getCurrentBet() + context.getMinRaise();
    }

    @Override
    public int calculateMaxBet(GameContext context) {
        return Integer.MAX_VALUE;
    }

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