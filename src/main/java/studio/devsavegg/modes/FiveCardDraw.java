package studio.devsavegg.modes;

import studio.devsavegg.core.*;
import studio.devsavegg.game.GamePhaseConfig;
import studio.devsavegg.game.IGameMode;
import studio.devsavegg.game.TableManager;
import studio.devsavegg.services.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FiveCardDraw implements IGameMode {

    private final StandardHandEvaluator evaluator;
    private final IBettingStructure bettingStructure;
    private final int anteAmount;

    public FiveCardDraw() {
        this.evaluator = new StandardHandEvaluator();
        this.bettingStructure = new NoLimitStructure();
        this.anteAmount = 10; 
    }

    public FiveCardDraw(int anteAmount) {
        this.evaluator = new StandardHandEvaluator();
        this.bettingStructure = new NoLimitStructure();
        this.anteAmount = anteAmount;
    }

    @Override
    public String getName() {
        return "Five Card Draw";
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
    public void executeForcedBets(TableManager tableManager, IPotManager potManager, GameContext context, Map<Player, Integer> roundBetsTracker) {
       
        for (Player player : tableManager.getAllPlayers()) {
            if (player.getChipStack() > 0 && !player.isSittingOut()) {
                int antePayment = Math.min(player.getChipStack(), anteAmount);
                player.bet(antePayment);
                potManager.processBet(player, antePayment);
                roundBetsTracker.put(player, 0); 
                
                System.out.println("  " + player.getName() + " pays $" + antePayment);
            }
        }
        
        context.setPotTotal(potManager.getCurrentTotal());
        System.out.println("Pot: $" + context.getPotTotal() + "\n");
    }

    @Override
    public List<GamePhaseConfig> getStructure() {
        List<GamePhaseConfig> phases = new ArrayList<>();

        // Initial Deal: 5 Hole Cards
        phases.add(new GamePhaseConfig("Initial Deal", 0, 5, false, false, false));

        // Pre-Draw Betting Round
        phases.add(new GamePhaseConfig("Pre-Draw Betting", 0, 0, true, false, false));

        // The Draw Phase
        phases.add(new GamePhaseConfig("The Draw", 0, 0, false, false, true));

        // Post-Draw Betting Round
        phases.add(new GamePhaseConfig("Post-Draw Betting", 0, 0, true, false, false));

        // Showdown
        phases.add(new GamePhaseConfig("Showdown", 0, 0, false, true, false));

        return phases;
    }
}