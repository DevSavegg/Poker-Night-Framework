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
 * <p>
 * # Principle - Single Responsibility Principle (SRP): Encapsulates the specific rules, phases, and betting logic for Texas Hold'em.
 */
public class TexasHoldemMode implements IGameMode {
    private final int smallBlind;
    private final int bigBlind;
    private final IHandEvaluator evaluator;
    private final IBettingStructure bettingStructure;

    /**
     * Initializes the Texas Hold'em game mode.
     * <p>
     * # Design - Constructor: Sets up the configuration for the game.
     *
     * @param smallBlind The amount for the Small Blind.
     * @param bigBlind The amount for the Big Blind.
     */
    public TexasHoldemMode(int smallBlind, int bigBlind) {
        this.smallBlind = smallBlind;
        this.bigBlind = bigBlind;
        this.evaluator = new StandardHandEvaluator();
        this.bettingStructure = new NoLimitStructure();
    }

    /**
     * Retrieves the name of this game variant.
     * <p>
     * # Design - Accessor: Gets the display name.
     *
     * @return The string "No Limit Texas Hold'em".
     */
    @Override
    public String getName() {
        return "No Limit Texas Hold'em";
    }

    /**
     * Defines the sequential phases of a Texas Hold'em hand.
     * <p>
     * Includes Pre-Flop, Flop, Turn, and River.
     * <p>
     * # Design - Template Method: Provides the skeleton of steps for the Game Engine to execute.
     *
     * @return A list of {@link GamePhaseConfig} objects.
     */
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

    /**
     * Retrieves the hand evaluator for this mode.
     * <p>
     * # Design - Strategy: Returns the standard high-hand evaluator.
     *
     * @return The {@link IHandEvaluator}.
     */
    @Override
    public IHandEvaluator getEvaluator() {
        return evaluator;
    }

    /**
     * Retrieves the betting structure.
     * <p>
     * # Design - Strategy: Returns the No-Limit structure.
     *
     * @return The {@link IBettingStructure}.
     */
    @Override
    public IBettingStructure getBettingStructure() {
        return bettingStructure;
    }

    /**
     * Executes the mandatory blind bets for the round.
     * <p>
     * # Design - Command: Execution of forced financial transactions.
     *
     * @param table The table manager for player positions.
     * @param potManager The pot manager to process chips.
     * @param context The current game context.
     * @param roundBetsTracker The map to record forced bets.
     */
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