package studio.devsavegg.core;

/**
 * Represents the rank of a playing card.
 * <p>
 * Values are assigned for comparison (2 being lowest, Ace being highest).
 * <p>
 * # Principle - Single Responsibility Principle (SRP): This enum encapsulates rank identity and its associated value.
 */
public enum Rank {
    TWO(2),
    THREE(3),
    FOUR(4),
    FIVE(5),
    SIX(6),
    SEVEN(7),
    EIGHT(8),
    NINE(9),
    TEN(10),
    JACK(11),
    QUEEN(12),
    KING(13),
    ACE(14);

    private final int value;

    Rank(int value) {
        this.value = value;
    }

    /**
     * Retrieves the numerical value of the rank.
     * <p>
     * # Design - Accessor: Exposes the internal value attribute safely.
     *
     * @return The int value (2-14) representing the card's strength.
     */
    public int getValue() {
        return value;
    }

    /**
     * Returns the capitalized name of the rank.
     * <p>
     * # Design - String Representation: Converts the enum constant to a readable string format.
     *
     * @return A string representing the rank (e.g., "Ace").
     */
    @Override
    public String toString() {
        return name().charAt(0) + name().substring(1).toLowerCase();
    }
}