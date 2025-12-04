package studio.devsavegg.core;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the current mutable state of the game table.
 * <p>
 * Used by services to determine legal moves and by UI to render the table.
 * <p>
 * # Principle - Single Responsibility Principle (SRP): This class serves as the central data context for the game.
 */
public class GameContext {
    private int potTotal;
    private int currentBet;
    private int minRaise;
    private List<Card> communityCards;
    private List<Player> activePlayers;
    private Player actingPlayer;

    /**
     * Initializes an empty game context.
     * <p>
     * # Design - Constructor: Sets default values for the context.
     */
    public GameContext() {
        this.potTotal = 0;
        this.currentBet = 0;
        this.minRaise = 0;
        this.communityCards = new ArrayList<>();
        this.activePlayers = new ArrayList<>();
        this.actingPlayer = null;
    }

    /**
     * Copy constructor used internally for creating snapshots.
     * <p>
     * # Design - Prototype: Helper constructor for cloning state.
     *
     * @param original The GameContext to copy.
     */
    private GameContext(GameContext original) {
        this.potTotal = original.potTotal;
        this.currentBet = original.currentBet;
        this.minRaise = original.minRaise;
        this.communityCards = new ArrayList<>(original.communityCards);
        this.activePlayers = new ArrayList<>(original.activePlayers);
        this.actingPlayer = original.actingPlayer;
    }

    /**
     * Creates a deep copy (snapshot) of the current game context.
     * <p>
     * # Design - Prototype: Creates a new instance with the exact state of the current one.
     *
     * @return A new {@link GameContext} instance.
     */
    public GameContext getSnapshot() {
        return new GameContext(this);
    }

    // -- Getters & Setters --

    /**
     * Retrieves the total chips in the pot.
     * <p>
     * # Design - Accessor: Gets the pot total.
     *
     * @return The pot total.
     */
    public int getPotTotal() { return potTotal; }

    /**
     * Updates the total chips in the pot.
     * <p>
     * # Design - Mutator: Sets the pot total.
     *
     * @param potTotal The new pot total.
     */
    public void setPotTotal(int potTotal) { this.potTotal = potTotal; }

    /**
     * Retrieves the current bet amount required to call.
     * <p>
     * # Design - Accessor: Gets the current bet.
     *
     * @return The current bet amount.
     */
    public int getCurrentBet() { return currentBet; }

    /**
     * Updates the current bet amount.
     * <p>
     * # Design - Mutator: Sets the current bet.
     *
     * @param currentBet The new current bet.
     */
    public void setCurrentBet(int currentBet) { this.currentBet = currentBet; }

    /**
     * Retrieves the minimum raise amount allowed.
     * <p>
     * # Design - Accessor: Gets the minimum raise.
     *
     * @return The minimum raise amount.
     */
    public int getMinRaise() { return minRaise; }

    /**
     * Updates the minimum raise amount.
     * <p>
     * # Design - Mutator: Sets the minimum raise.
     *
     * @param minRaise The new minimum raise.
     */
    public void setMinRaise(int minRaise) { this.minRaise = minRaise; }

    /**
     * Retrieves the list of community cards on the table.
     * <p>
     * # Design - Accessor: Gets the community cards.
     *
     * @return A list of {@link Card} objects.
     */
    public List<Card> getCommunityCards() { return communityCards; }

    /**
     * Updates the list of community cards.
     * <p>
     * # Design - Mutator: Sets the community cards.
     *
     * @param communityCards The new list of cards.
     */
    public void setCommunityCards(List<Card> communityCards) { this.communityCards = communityCards; }

    /**
     * Retrieves the list of active players in the hand.
     * <p>
     * # Design - Accessor: Gets the active players.
     *
     * @return A list of {@link Player} objects.
     */
    public List<Player> getActivePlayers() { return activePlayers; }

    /**
     * Updates the list of active players.
     * <p>
     * # Design - Mutator: Sets the active players.
     *
     * @param activePlayers The new list of players.
     */
    public void setActivePlayers(List<Player> activePlayers) { this.activePlayers = activePlayers; }

    /**
     * Retrieves the player whose turn it currently is.
     * <p>
     * # Design - Accessor: Gets the acting player.
     *
     * @return The {@link Player} currently acting.
     */
    public Player getActingPlayer() { return actingPlayer; }

    /**
     * Updates the player whose turn it is.
     * <p>
     * # Design - Mutator: Sets the acting player.
     *
     * @param actingPlayer The player to set as acting.
     */
    public void setActingPlayer(Player actingPlayer) { this.actingPlayer = actingPlayer; }
}