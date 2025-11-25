package studio.devsavegg.core;

/**
 * Represents the four standard suits in a deck of cards.
 */
public enum Suit {
    HEARTS,
    DIAMONDS,
    CLUBS,
    SPADES;

    @Override
    public String toString() {
        return name().charAt(0) + name().substring(1).toLowerCase();
    }
}