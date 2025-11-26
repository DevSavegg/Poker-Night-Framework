package studio.devsavegg.events;

import studio.devsavegg.core.Card;
import studio.devsavegg.core.GameContext;
import studio.devsavegg.core.HandRank;
import studio.devsavegg.core.Player;
import studio.devsavegg.core.PlayerAction;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A basic implementation of IGameObserver that prints game events to the standard output.
 */
public class ConsoleGameLogger implements IGameObserver {

    @Override
    public void onGameStarted(GameContext context) {
        System.out.println("\n=== NEW GAME STARTED ===");
        System.out.println("Players: " + context.getActivePlayers());
    }

    @Override
    public void onPhaseStart(String phaseName) {
        System.out.println("\n--- " + phaseName.toUpperCase() + " ---");
    }

    @Override
    public void onPlayerAction(PlayerAction action) {
        String msg = String.format(" > %s performs %s",
                action.getPlayer().getName(),
                action.getType());

        if (action.getAmount() > 0) {
            msg += " (" + action.getAmount() + ")";
        }
        System.out.println(msg);
    }

    @Override
    public void onDealHoleCards(Player player, int count) {
        System.out.println("Dealer gives " + count + " cards to " + player.getName());
    }

    @Override
    public void onDealCommunity(List<Card> cards) {
        String cardStr = cards.stream()
                .map(Card::toString)
                .collect(Collectors.joining(", "));
        System.out.println("\nBoard: [" + cardStr + "]");
    }

    @Override
    public void onPotUpdate(int total) {
        System.out.println("Pot is now: " + total);
    }

    @Override
    public void onShowdown(Map<Player, HandRank> showdowns) {
        System.out.println("\n=== SHOWDOWN ===");
        showdowns.forEach((player, rank) ->
                System.out.println(player.getName() + " has " + rank)
        );
    }

    @Override
    public void onHandEnded(Map<Player, Integer> winnings) {
        System.out.println("\n--- Hand Results ---");
        winnings.forEach((player, amount) ->
                System.out.println(player.getName() + " wins " + amount + " chips")
        );
        System.out.println("====================\n");
    }

    @Override
    public void onPlayerTimeout(Player player) {
        System.out.println("!!! " + player.getName() + " timed out !!!");
    }

    @Override
    public void onGameEnded(Player winner) {
        System.out.println("\n#################################");
        System.out.println("   WINNER: " + winner.getName());
        System.out.println("   Final Stack: " + winner.getChipStack());
        System.out.println("#################################\n");
    }
}