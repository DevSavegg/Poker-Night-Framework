package studio.devsavegg.game;

import studio.devsavegg.core.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ConsolePlayerInput implements IPlayerInput {
    private final Scanner scanner = new Scanner(System.in);

    @Override
    public PlayerAction requestAction(Player player, GameContext context, List<ActionType> legalActions) {
        System.out.println("\n--- Action to " + player.getName() + " ---");
        System.out.println("Chips: $" + player.getChipStack());
        System.out.println("Hand: " + player.getHoleCards());
        System.out.println("Board: " + context.getCommunityCards());
        System.out.println("Pot: $" + context.getPotTotal() + " | To Call: $" + (context.getCurrentBet()));
        System.out.println("Legal Actions: " + legalActions);

        while (true) {
            System.out.print("Enter action: ");
            String input = scanner.nextLine().toUpperCase().trim();

            try {
                ActionType type = ActionType.valueOf(input);
                if (!legalActions.contains(type)) {
                    System.out.println("Illegal action. Try again.");
                    continue;
                }

                int amount = 0;
                if (type == ActionType.BET || type == ActionType.RAISE) {
                    System.out.print("Enter amount: ");
                    amount = Integer.parseInt(scanner.nextLine().trim());
                } else if (type == ActionType.ALL_IN) {
                    amount = player.getChipStack();
                }

                return new PlayerAction(player, type, amount);

            } catch (IllegalArgumentException e) {
                System.out.println("Invalid input. Enter " + legalActions);
            }
        }
    }

    @Override
    public List<Card> requestDiscard(Player player, GameContext context) {
        return new ArrayList<>();
    }
}