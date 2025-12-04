package studio.devsavegg.core;

import java.util.Objects;

/**
 * Represents a single playing card.
 * <p>
 * Implements {@link Comparable} to allow sorting by Rank, then Suit.
 * <p>
 * # Principle - Single Responsibility Principle (SRP): This class acts as a pure value object for card data.
 */
public class Card implements Comparable<Card> {
    private final Rank rank;
    private final Suit suit;

    /**
     * Constructs a new Card with the specified rank and suit.
     * <p>
     * # Design - Constructor: Initializes the immutable state of the object.
     *
     * @param rank The {@link Rank} of the card.
     * @param suit The {@link Suit} of the card.
     */
    public Card(Rank rank, Suit suit) {
        this.rank = rank;
        this.suit = suit;
    }

    /**
     * Retrieves the rank of the card.
     * <p>
     * # Design - Accessor: Provides read-only access to the rank property.
     *
     * @return The {@link Rank} of the card.
     */
    public Rank getRank() {
        return rank;
    }

    /**
     * Retrieves the suit of the card.
     * <p>
     * # Design - Accessor: Provides read-only access to the suit property.
     *
     * @return The {@link Suit} of the card.
     */
    public Suit getSuit() {
        return suit;
    }

    /**
     * Compares this card with another card for order.
     * <p>
     * This method implements a comparison strategy based on Rank priority, then Suit.
     * <p>
     * # Design - Strategy: Defines a comparison strategy.
     *
     * @param other The card to be compared.
     * @return A negative integer, zero, or a positive integer as this card is less than, equal to, or greater than the specified card.
     */
    @Override
    public int compareTo(Card other) {
        int rankComparison = Integer.compare(this.rank.getValue(), other.rank.getValue());
        if (rankComparison != 0) {
            return rankComparison;
        }

        return this.suit.compareTo(other.suit);
    }

    /**
     * Returns a string representation of the card.
     * <p>
     * # Design - String Representation: Formats the card data for display.
     *
     * @return A string in the format "Rank of Suit".
     */
    @Override
    public String toString() {
        return rank + " of " + suit;
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     * <p>
     * # Design - Value Object Equality: Determines equality based on state (rank and suit) rather than reference.
     *
     * @param o The reference object with which to compare.
     * @return {@code true} if this object is the same as the obj argument; {@code false} otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Card card = (Card) o;
        return rank == card.rank && suit == card.suit;
    }

    /**
     * Returns a hash code value for the card.
     * <p>
     * # Design - Hashing Strategy: Generates a consistent hash based on immutable properties.
     *
     * @return A hash code value for this object.
     */
    @Override
    public int hashCode() {
        return Objects.hash(rank, suit);
    }
}