package studio.devsavegg.gui;

import javafx.application.Platform;
import studio.devsavegg.core.*;
import studio.devsavegg.events.IGameObserver;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * Bridges Game Engine events to the JavaFX UI thread.
 * <p>
 * Ensures all UI updates occur on the JavaFX Application Thread using {@link Platform#runLater}.
 * <p>
 * # Principle - Dependency Inversion / Interface Segregation: Decouples the game engine from JavaFX specifics.
 */
public class GuiGameObserver implements IGameObserver {
    private final PokerTableController controller;

    private final List<Card> boardCards = new ArrayList<>();

    private Map<Player, HandRank> lastShowdown;

    /**
     * Initializes the observer with the target UI controller.
     * <p>
     * # Design - Adapter: Adapts engine events to UI calls.
     *
     * @param controller The {@link PokerTableController} managing the view.
     */
    public GuiGameObserver(PokerTableController controller) {
        this.controller = controller;
    }

    /**
     * Handles the start of a new game session.
     * <p>
     * # Design - Observer: Reacts to Game Started event.
     *
     * @param context The initial game state.
     */
    @Override
    public void onGameStarted(GameContext context) {
        boardCards.clear();

        Platform.runLater(() -> {
            controller.initializeSeats(context.getActivePlayers());
            controller.updateCommunityCards(boardCards);
            controller.log("Game Started.");
        });
    }

    /**
     * Updates the UI when a new game phase begins.
     * <p>
     * # Design - Observer: Reacts to Phase Start event.
     *
     * @param phaseName The name of the phase.
     */
    @Override
    public void onPhaseStart(String phaseName) {
        Platform.runLater(() -> controller.updatePhase(phaseName));
    }

    /**
     * Updates the UI to reflect a player's action.
     * <p>
     * # Design - Observer: Reacts to Player Action event.
     *
     * @param action The action performed.
     */
    @Override
    public void onPlayerAction(PlayerAction action) {
        Platform.runLater(() -> {
            controller.updatePlayerState(action.getPlayer());
            controller.log(action.getPlayer().getName() + ": " + action.getType() + " " + action.getAmount());
        });
    }

    /**
     * Visualizes the dealing of hole cards.
     * <p>
     * # Design - Observer: Reacts to Deal Hole Cards event.
     *
     * @param player The receiving player.
     * @param count The number of cards.
     */
    @Override
    public void onDealHoleCards(Player player, int count) {
        Platform.runLater(() -> controller.dealHoleCards(player));
    }

    /**
     * Visualizes the dealing of community cards.
     * <p>
     * # Design - Observer: Reacts to Deal Community Cards event.
     *
     * @param newCards The new cards dealt.
     */
    @Override
    public void onDealCommunity(List<Card> newCards) {
        boardCards.addAll(newCards);
        Platform.runLater(() -> {
            controller.updateCommunityCards(boardCards);
        });
    }

    /**
     * Updates the pot display.
     * <p>
     * # Design - Observer: Reacts to Pot Update event.
     *
     * @param total The new total.
     */
    @Override
    public void onPotUpdate(int total) {
        Platform.runLater(() -> controller.updatePot(total));
    }

    /**
     * Logs showdown results.
     * <p>
     * # Design - Observer: Reacts to Showdown event.
     *
     * @param showdowns The map of hand ranks.
     */
    @Override
    public void onShowdown(Map<Player, HandRank> showdowns) {
        this.lastShowdown = showdowns;
        Platform.runLater(() -> {
            controller.log("--- Showdown ---");
            showdowns.forEach((p, rank) -> controller.log(p.getName() + " has " + rank));
        });
    }

    /**
     * Displays final hand results and winnings.
     * <p>
     * Blocks the game thread using a {@link CountDownLatch} until the user dismisses the results overlay.
     * <p>
     * # Design - Observer / Synchronization: Reacts to Hand End event and synchronizes threads.
     *
     * @param winnings The map of winnings.
     */
    @Override
    public void onHandEnded(Map<Player, Integer> winnings) {
        CountDownLatch latch = new CountDownLatch(1);

        Platform.runLater(() -> {
            controller.log("--- Hand Winners ---");
            winnings.forEach((p, amt) -> {
                controller.log(p.getName() + " wins $" + amt);
                controller.updatePlayerState(p);
            });

            controller.showRoundResults(lastShowdown, winnings, new ArrayList<>(boardCards), () -> {
                latch.countDown();
            });
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        this.lastShowdown = null;
    }

    /**
     * Logs player timeouts.
     * <p>
     * # Design - Observer: Reacts to Timeout event.
     *
     * @param player The timed-out player.
     */
    @Override
    public void onPlayerTimeout(Player player) {
        Platform.runLater(() -> controller.log(player.getName() + " timed out!"));
    }

    /**
     * Displays the game over screen.
     * <p>
     * # Design - Observer: Reacts to Game End event.
     *
     * @param winner The overall winner.
     */
    @Override
    public void onGameEnded(Player winner) {
        Platform.runLater(() -> {
            controller.log("!!! GAME OVER !!!");
            controller.log("Winner: " + winner.getName());
            controller.showGameWinner(winner);
        });
    }
}