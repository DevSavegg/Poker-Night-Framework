package studio.devsavegg.core;

import java.util.Objects;

/**
 * Represents a single playing card.
 * Implements Comparable to allow sorting by Rank, then Suit.
 */
public class Card implements Comparable<Card> {
    private final Rank rank;
    private final Suit suit;

    public Card(Rank rank, Suit suit) {
        this.rank = rank;
        this.suit = suit;
    }

    public Rank getRank() {
        return rank;
    }

    public Suit getSuit() {
        return suit;
    }

    @Override
    public int compareTo(Card other) {
        int rankComparison = Integer.compare(this.rank.getValue(), other.rank.getValue());
        if (rankComparison != 0) {
            return rankComparison;
        }

        return this.suit.compareTo(other.suit);
    }

    @Override
    public String toString() {
        return rank + " of " + suit;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Card card = (Card) o;
        return rank == card.rank && suit == card.suit;
    }

    @Override
    public int hashCode() {
        return Objects.hash(rank, suit);
    }
}