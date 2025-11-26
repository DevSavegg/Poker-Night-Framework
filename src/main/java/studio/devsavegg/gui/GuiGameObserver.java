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
 */
public class GuiGameObserver implements IGameObserver {
    private final PokerTableController controller;

    private final List<Card> boardCards = new ArrayList<>();

    private Map<Player, HandRank> lastShowdown;

    public GuiGameObserver(PokerTableController controller) {
        this.controller = controller;
    }

    @Override
    public void onGameStarted(GameContext context) {
        boardCards.clear();

        Platform.runLater(() -> {
            controller.initializeSeats(context.getActivePlayers());
            controller.updateCommunityCards(boardCards);
            controller.log("Game Started.");
        });
    }

    @Override
    public void onPhaseStart(String phaseName) {
        Platform.runLater(() -> controller.updatePhase(phaseName));
    }

    @Override
    public void onPlayerAction(PlayerAction action) {
        Platform.runLater(() -> {
            controller.updatePlayerState(action.getPlayer());
            controller.log(action.getPlayer().getName() + ": " + action.getType() + " " + action.getAmount());
        });
    }

    @Override
    public void onDealHoleCards(Player player, int count) {
        Platform.runLater(() -> controller.dealHoleCards(player));
    }

    @Override
    public void onDealCommunity(List<Card> newCards) {
        boardCards.addAll(newCards);
        Platform.runLater(() -> {
            controller.updateCommunityCards(boardCards);
        });
    }

    @Override
    public void onPotUpdate(int total) {
        Platform.runLater(() -> controller.updatePot(total));
    }

    @Override
    public void onShowdown(Map<Player, HandRank> showdowns) {
        this.lastShowdown = showdowns;
        Platform.runLater(() -> {
            controller.log("--- Showdown ---");
            showdowns.forEach((p, rank) -> controller.log(p.getName() + " has " + rank));
        });
    }

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

    @Override
    public void onPlayerTimeout(Player player) {
        Platform.runLater(() -> controller.log(player.getName() + " timed out!"));
    }

    @Override
    public void onGameEnded(Player winner) {
        Platform.runLater(() -> {
            controller.log("!!! GAME OVER !!!");
            controller.log("Winner: " + winner.getName());
            controller.showGameWinner(winner);
        });
    }
}