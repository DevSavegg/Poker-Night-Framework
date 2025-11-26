package studio.devsavegg.modes;

import studio.devsavegg.core.GameContext;
import studio.devsavegg.core.Player;
import studio.devsavegg.game.GamePhaseConfig;
import studio.devsavegg.game.IGameMode;
import studio.devsavegg.game.TableManager;
import studio.devsavegg.services.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Concrete implementation of No-Limit Texas Hold'em.
 */
public class TexasHoldemMode implements IGameMode {
    private final int smallBlind;
    private final int bigBlind;
    private final IHandEvaluator evaluator;
    private final IBettingStructure bettingStructure;

    public TexasHoldemMode(int smallBlind, int bigBlind) {
        this.smallBlind = smallBlind;
        this.bigBlind = bigBlind;
        this.evaluator = new StandardHandEvaluator();
        this.bettingStructure = new NoLimitStructure();
    }

    @Override
    public String getName() {
        return "No Limit Texas Hold'em";
    }

    @Override
    public List<GamePhaseConfig> getStructure() {
        List<GamePhaseConfig> phases = new ArrayList<>();

        // Pre-Flop: Deal 2 hole cards, 0 community, Betting round
        phases.add(new GamePhaseConfig("Pre-Flop", 0, 2, true, false, false));

        // Flop: Deal 3 community cards, 0 hole, Betting round
        phases.add(new GamePhaseConfig("The Flop", 3, 0, true, false, false));

        // Turn: Deal 1 community card, 0 hole, Betting round
        phases.add(new GamePhaseConfig("The Turn", 1, 0, true, false, false));

        // River: Deal 1 community card, 0 hole, Betting round (triggers Showdown)
        phases.add(new GamePhaseConfig("The River", 1, 0, true, true, false));

        return phases;
    }

    @Override
    public IHandEvaluator getEvaluator() {
        return evaluator;
    }

    @Override
    public IBettingStructure getBettingStructure() {
        return bettingStructure;
    }

    @Override
    public void executeForcedBets(TableManager table, IPotManager potManager, GameContext context, Map<Player, Integer> roundBetsTracker) {
        // --- Small Blind ---
        Player sbPlayer = table.getPlayerAt(table.getSmallBlindPos());
        int sbAmount = Math.min(sbPlayer.getChipStack(), smallBlind);

        if (sbAmount > 0) {
            sbPlayer.bet(sbAmount);
            potManager.processBet(sbPlayer, sbAmount);
            // Register this bet in the engine's tracker so they don't have to pay it again
            roundBetsTracker.put(sbPlayer, sbAmount);
        }

        // --- Big Blind ---
        Player bbPlayer = table.getPlayerAt(table.getBigBlindPos());
        int bbAmount = Math.min(bbPlayer.getChipStack(), bigBlind);

        if (bbAmount > 0) {
            bbPlayer.bet(bbAmount);
            potManager.processBet(bbPlayer, bbAmount);
            roundBetsTracker.put(bbPlayer, bbAmount);
        }

        // --- Update Game Context ---
        context.setCurrentBet(bigBlind);
        context.setMinRaise(bigBlind);
        context.setPotTotal(potManager.getCurrentTotal());

        System.out.println("\n[Blinds] SB: " + sbPlayer.getName() + " ($" + sbAmount + "), BB: " + bbPlayer.getName() + " ($" + bbAmount + ")");
    }
}