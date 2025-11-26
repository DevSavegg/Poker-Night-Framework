package studio.devsavegg.gui;

import studio.devsavegg.core.*;
import studio.devsavegg.game.IPlayerInput;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Implementation of IPlayerInput that delegates to the JavaFX Controller.
 */
public class GuiPlayerInput implements IPlayerInput {
    private final PokerTableController controller;

    public GuiPlayerInput(PokerTableController controller) {
        this.controller = controller;
    }

    @Override
    public PlayerAction requestAction(Player player, GameContext context, List<ActionType> legalActions) {
        try {
            return controller.promptUserForAction(player, context, legalActions).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return new PlayerAction(player, ActionType.FOLD, 0);
        }
    }

    @Override
    public List<Card> requestDiscard(Player player, GameContext context) {
        try {
            return controller.promptUserForDiscard(player, context).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
}