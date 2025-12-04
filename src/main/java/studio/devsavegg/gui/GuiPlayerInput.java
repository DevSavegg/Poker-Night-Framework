package studio.devsavegg.gui;

import studio.devsavegg.core.*;
import studio.devsavegg.game.IPlayerInput;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Implementation of IPlayerInput that delegates to the JavaFX Controller.
 * <p>
 * # Principle - Single Responsibility Principle (SRP): Acts as the bridge between Engine requests and UI responses.
 */
public class GuiPlayerInput implements IPlayerInput {
    private final PokerTableController controller;

    /**
     * Initializes the input handler.
     * <p>
     * # Design - Adapter: Adapts the UI controller to the {@link IPlayerInput} interface.
     *
     * @param controller The UI controller.
     */
    public GuiPlayerInput(PokerTableController controller) {
        this.controller = controller;
    }

    /**
     * Requests an action from the player via the GUI.
     * <p>
     * Blocking call that waits for the UI Future to complete.
     * <p>
     * # Design - Future / Async Pattern: Waits for asynchronous user input.
     *
     * @param player The player acting.
     * @param context The game state.
     * @param legalActions Valid actions.
     * @return The selected {@link PlayerAction}.
     */
    @Override
    public PlayerAction requestAction(Player player, GameContext context, List<ActionType> legalActions) {
        try {
            return controller.promptUserForAction(player, context, legalActions).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return new PlayerAction(player, ActionType.FOLD, 0);
        }
    }

    /**
     * Requests card discards from the player via the GUI.
     * <p>
     * Blocking call that waits for the UI Future to complete.
     * <p>
     * # Design - Future / Async Pattern: Waits for asynchronous user input.
     *
     * @param player The player acting.
     * @param context The game state.
     * @return List of cards to discard.
     */
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