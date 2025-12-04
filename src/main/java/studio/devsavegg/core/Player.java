package studio.devsavegg.core;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a participant in the game.
 * <p>
 * # Principle - Single Responsibility Principle (SRP): This class manages the individual state (chips, cards, status) of a single player.
 */
public class Player {
    private final String id;
    private final String name;
    private int chipStack;
    private final List<Card> holeCards;

    // Status flags
    private boolean isFolded;
    private boolean isAllIn;
    private boolean isSittingOut;

    /**
     * Initializes a new player.
     * <p>
     * # Design - Constructor: Sets up identity and initial bankroll.
     *
     * @param id The unique player ID.
     * @param name The display name.
     * @param initialChips The starting chip count.
     */
    public Player(String id, String name, int initialChips) {
        this.id = id;
        this.name = name;
        this.chipStack = initialChips;
        this.holeCards = new ArrayList<>();
        this.isFolded = false;
        this.isAllIn = false;
        this.isSittingOut = false;
    }

    /**
     * Adds a card to the player's hole cards.
     * <p>
     * # Design - Mutator: Adds an element to the internal collection.
     *
     * @param card The {@link Card} to receive.
     */
    public void receiveCard(Card card) {
        this.holeCards.add(card);
    }

    /**
     * Deducts chips from the stack for a bet.
     * <p>
     * This method modifies the player's internal state (chips and All-In status) based on valid logic.
     * <p>
     * # Design - State: Logic that causes a transition in state (Active to All-In).
     *
     * @param amount The amount to bet.
     * @throws IllegalArgumentException if player has insufficient chips.
     */
    public void bet(int amount) {
        if (amount > chipStack) {
            throw new IllegalArgumentException("Insufficient chips");
        }
        chipStack -= amount;
        if (chipStack == 0) {
            isAllIn = true;
        }
    }

    /**
     * Marks the player as folded.
     * <p>
     * # Design - State: Transitions the player to a folded state.
     */
    public void fold() {
        this.isFolded = true;
    }

    /**
     * Clears hole cards and resets round-specific flags (folded/all-in).
     * <p>
     * Note: Does not reset isSittingOut.
     * <p>
     * # Design - State: Resets transient round state while preserving persistent state.
     */
    public void clearHand() {
        this.holeCards.clear();
        this.isFolded = false;
        this.isAllIn = (chipStack == 0); // Still all-in if they have 0 chips
    }

    // -- Getters and Setters --

    public String getId() { return id; }
    public String getName() { return name; }
    public int getChipStack() { return chipStack; }
    public void addChips(int amount) { this.chipStack += amount; }
    public List<Card> getHoleCards() { return new ArrayList<>(holeCards); }
    public boolean isFolded() { return isFolded; }
    public boolean isAllIn() { return isAllIn; }
    public boolean isSittingOut() { return isSittingOut; }
    public void setSittingOut(boolean sittingOut) { isSittingOut = sittingOut; }

    @Override
    public String toString() {
        return name + " ($" + chipStack + ")";
    }
}