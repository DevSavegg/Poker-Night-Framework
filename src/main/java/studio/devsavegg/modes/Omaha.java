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
 * Concrete implementation of the Omaha poker variant.
 * <p>
 * # Principle - Single Responsibility Principle (SRP): Defines rules specific to Omaha (4 hole cards, specific phases).
 */
public class Omaha implements IGameMode {

    private final int smallBlindAmount;
    private final int bigBlindAmount;
    private final StandardHandEvaluator evaluator;
    private final IBettingStructure bettingStructure;

    /**
     * Initializes Omaha with default blinds (5/10).
     * <p>
     * # Design - Constructor: Default initialization.
     */
    public Omaha() {
        this(5, 10);
    }

    /**
     * Initializes Omaha with specific blinds.
     * <p>
     * # Design - Constructor: Custom initialization.
     *
     * @param smallBlindAmount The Small Blind amount.
     * @param bigBlindAmount The Big Blind amount.
     */
    public Omaha(int smallBlindAmount, int bigBlindAmount) {
        this.smallBlindAmount = smallBlindAmount;
        this.bigBlindAmount = bigBlindAmount;
        this.evaluator = new StandardHandEvaluator();
        this.bettingStructure = new NoLimitStructure();
    }

    /**
     * Retrieves the name of this game variant.
     * <p>
     * # Design - Accessor: Gets the display name.
     *
     * @return The string name of the mode.
     */
    @Override
    public String getName() {
        return "No-Limit Texas Hold'em"; // Note: This string matches original source, though class name is Omaha.
    }

    /**
     * Retrieves the hand evaluator for this mode.
     * <p>
     * # Design - Strategy: Returns the evaluator strategy.
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
     * # Design - Strategy: Returns the betting strategy.
     *
     * @return The {@link IBettingStructure}.
     */
    @Override
    public IBettingStructure getBettingStructure() {
        return bettingStructure;
    }

    /**
     * Defines the sequential phases of an Omaha hand.
     * <p>
     * Includes Pre-Flop (4 cards), Flop, Turn, River, and a Choosing phase.
     * <p>
     * # Design - Template Method: Provides the game steps to the engine.
     *
     * @return A list of {@link GamePhaseConfig}.
     */
    @Override
    public List<GamePhaseConfig> getStructure() {
        List<GamePhaseConfig> phases = new ArrayList<>();

        // Pre-Flop: 2 Hole Cards, Betting
        phases.add(new GamePhaseConfig("Pre-Flop", 0, 4, true, false, false));

        // Flop: 3 Community Cards, Betting
        phases.add(new GamePhaseConfig("Flop", 3, 0, true, false, false));

        // Turn: 1 Community Card, Betting
        phases.add(new GamePhaseConfig("Turn", 1, 0, true, false, false));

        // River: 1 Community Card, Betting, Showdown choosing omaha card will be done in this phase too
        phases.add(new GamePhaseConfig("River", 1, 0, true, false, false));

        //choosing omaha card will be done in this phase
        phases.add(new GamePhaseConfig("Choosing", 0, 0, false, true, false));


        return phases;
    }

    /**
     * Executes forced blind bets for the round.
     * <p>
     * # Design - Command: Execution of forced financial transactions.
     *
     * @param table The table manager.
     * @param potManager The pot manager.
     * @param context The game context.
     * @param roundBets The bet tracker.
     */
    @Override
    public void executeForcedBets(TableManager table, IPotManager potManager, GameContext context, Map<Player, Integer> roundBets) {
        Player sbPlayer = table.getPlayerAt(table.getSmallBlindPos());
        Player bbPlayer = table.getPlayerAt(table.getBigBlindPos());

        // Post Small Blind
        int actualSb = Math.min(sbPlayer.getChipStack(), smallBlindAmount);
        if (actualSb > 0) {
            sbPlayer.bet(actualSb);
            potManager.processBet(sbPlayer, actualSb);
        }

        // Post Big Blind
        int actualBb = Math.min(bbPlayer.getChipStack(), bigBlindAmount);
        if (actualBb > 0) {
            bbPlayer.bet(actualBb);
            potManager.processBet(bbPlayer, actualBb);
        }

        // Setup context for Pre-Flop
        context.setCurrentBet(actualBb);
        context.setMinRaise(bigBlindAmount);
    }
}