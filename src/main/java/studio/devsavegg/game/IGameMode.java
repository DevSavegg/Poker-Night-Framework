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
 */
public interface IGameMode {
    String getName();
    List<GamePhaseConfig> getStructure();
    IHandEvaluator getEvaluator();
    IBettingStructure getBettingStructure();

    void executeForcedBets(TableManager table, IPotManager potManager, GameContext context, Map<Player, Integer> roundBetsTracker);
}