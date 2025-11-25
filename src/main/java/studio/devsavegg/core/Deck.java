package studio.devsavegg.core;

import java.util.Collections;
import java.util.Stack;

/**
 * Manages a standard deck of 52 cards.
 */
public class Deck {
    private final Stack<Card> cards;

    public Deck() {
        this.cards = new Stack<>();
        reset();
    }

    /**
     * Clears the current deck and repopulates it with 52 cards.
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
     */
    public void shuffle() {
        Collections.shuffle(cards);
    }

    /**
     * Deals a single card from the top of the deck.
     * @return Card
     * @throws IllegalStateException if the deck is empty
     */
    public Card deal() {
        if (cards.isEmpty()) {
            throw new IllegalStateException("Deck is empty");
        }
        return cards.pop();
    }

    public int remainingCards() {
        return cards.size();
    }
}