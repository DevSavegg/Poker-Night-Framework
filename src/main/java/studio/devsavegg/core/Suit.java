package studio.devsavegg.core;

/**
 * Represents the four standard suits in a deck of cards.
 * <p>
 * # Principle - Single Responsibility Principle (SRP): This enum defines the domain of card suits.
 */
public enum Suit {
    HEARTS,
    DIAMONDS,
    CLUBS,
    SPADES;

    /**
     * Returns the capitalized name of the suit.
     * <p>
     * # Design - String Representation: Provides a human-readable format of the enum constant.
     *
     * @return A string representing the suit name (e.g., "Hearts").
     */
    @Override
    public String toString() {
        return name().charAt(0) + name().substring(1).toLowerCase();
    }
}