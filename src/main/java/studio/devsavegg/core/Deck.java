package studio.devsavegg.core;

import java.util.Collections;
import java.util.Stack;

/**
 * Manages a standard deck of 52 cards.
 * <p>
 * # Principle - Single Responsibility Principle (SRP): This class controls the lifecycle and distribution of cards.
 */
public class Deck {
    private final Stack<Card> cards;

    /**
     * Constructs a new Deck and initializes it with a full set of cards.
     * <p>
     * # Design - Constructor: Encapsulates initialization logic.
     */
    public Deck() {
        this.cards = new Stack<>();
        reset();
    }

    /**
     * Clears the current deck and repopulates it with 52 cards in random order.
     * <p>
     * # Design - State: Resets the object to its initial valid state.
     */
    public void reset() {
        cards.clear();
        for (Suit suit : Suit.values()) {
            for (Rank rank : Rank.values()) {
                cards.push(new Card(rank, suit));
            }
        }
        shuffle();
    }

    /**
     * Shuffles the current cards in the deck.
     * <p>
     * # Design - Strategy: Delegates to a randomization algorithm.
     */
    public void shuffle() {
        Collections.shuffle(cards);
    }

    /**
     * Deals a single card from the top of the deck.
     * <p>
     * # Design - Iterator: Provides sequential access to the collection of cards.
     *
     * @return The next {@link Card} in the deck.
     * @throws IllegalStateException if the deck is empty.
     */
    public Card deal() {
        if (cards.isEmpty()) {
            throw new IllegalStateException("Deck is empty");
        }
        return cards.pop();
    }

    /**
     * Retrieves the number of cards remaining in the deck.
     * <p>
     * # Design - Accessor: Exposes the state of the internal collection.
     *
     * @return The count of remaining cards.
     */
    public int remainingCards() {
        return cards.size();
    }
}