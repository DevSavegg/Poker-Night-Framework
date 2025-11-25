package studio.devsavegg.services;

import studio.devsavegg.core.GameContext;
import studio.devsavegg.core.PlayerAction;

public interface IBettingStructure {
    int calculateMinRaise(GameContext context);
    int calculateMaxBet(GameContext context);
    boolean isBetValid(PlayerAction action, GameContext context);
}