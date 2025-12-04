package studio.devsavegg.game;

import studio.devsavegg.core.Player;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages the seating arrangement and dealer button position.
 * <p>
 * # Principle - Single Responsibility Principle (SRP): Solely responsible for player positions and rotation logic.
 */
public class TableManager {
    private final List<Player> seats;
    private int buttonPosition;

    /**
     * Initializes the table with a list of players.
     * <p>
     * # Design - Constructor: Sets up the initial seating.
     *
     * @param players The list of players in seating order.
     */
    public TableManager(List<Player> players) {
        this.seats = new ArrayList<>(players);
        this.buttonPosition = 0;
    }

    /**
     * Moves the dealer button to the next position.
     * <p>
     * # Design - Iterator / State: Advances the game state marker.
     */
    public void moveButton() {
        if (seats.isEmpty()) return;
        buttonPosition = (buttonPosition + 1) % seats.size();
    }

    /**
     * Retrieves the player currently on the button.
     * <p>
     * # Design - Accessor: Gets the button player.
     *
     * @return The {@link Player} on the button.
     */
    public Player getButtonPlayer() {
        return seats.get(buttonPosition);
    }

    /**
     * Retrieves the index of the button position.
     * <p>
     * # Design - Accessor: Gets the button index.
     *
     * @return The integer index of the button.
     */
    public int getButtonPosition() {
        return buttonPosition;
    }

    /**
     * Calculates the position of the Small Blind.
     * <p>
     * Handles the special Heads-Up case (2 players).
     * <p>
     * # Design - Algorithm: Logic for positional calculation.
     *
     * @return The integer index of the Small Blind.
     */
    public int getSmallBlindPos() {
        // Heads up (2 players) is a special case: Button is SB.
        if (seats.size() == 2) return buttonPosition;
        return (buttonPosition + 1) % seats.size();
    }

    /**
     * Calculates the position of the Big Blind.
     * <p>
     * # Design - Algorithm: Logic for positional calculation.
     *
     * @return The integer index of the Big Blind.
     */
    public int getBigBlindPos() {
        if (seats.size() == 2) return (buttonPosition + 1) % seats.size();
        return (buttonPosition + 2) % seats.size();
    }

    /**
     * Retrieves the player at a specific absolute position.
     * <p>
     * Wraps around if the position exceeds the seat count.
     * <p>
     * # Design - Accessor: Gets a player by index.
     *
     * @param pos The index to retrieve.
     * @return The {@link Player} at that seat.
     */
    public Player getPlayerAt(int pos) {
        return seats.get(pos % seats.size());
    }

    /**
     * Finds the next active player starting AFTER the given position.
     * <p>
     * Skips folded, all-in, or sitting-out players.
     * <p>
     * # Design - Iterator: Searches for the next valid element in the sequence.
     *
     * @param currentPos The position to start searching after.
     * @return The next active {@link Player}, or null if none found.
     */
    public Player getNextActivePlayer(int currentPos) {
        for (int i = 1; i < seats.size(); i++) {
            Player p = seats.get((currentPos + i) % seats.size());
            if (!p.isFolded() && !p.isAllIn() && !p.isSittingOut()) {
                return p;
            }
        }
        return null; // Should not happen if game is running
    }

    /**
     * Retrieves the list of all players at the table.
     * <p>
     * # Design - Accessor: Gets all players.
     *
     * @return A list of {@link Player} objects.
     */
    public List<Player> getAllPlayers() {
        return seats;
    }

    /**
     * Retrieves the seat index of a specific player.
     * <p>
     * # Design - Accessor: Finds index of object.
     *
     * @param p The player to find.
     * @return The index of the player, or -1 if not found.
     */
    public int getPlayerPosition(Player p) {
        return seats.indexOf(p);
    }
}