package studio.devsavegg.core;

import java.util.Collections;
import java.util.List;

/**
 * Represents the final strength of a poker hand (e.g., Full House, Pair).
 * <p>
 * # Principle - Single Responsibility Principle (SRP): This class encapsulates the ranking logic and data for a hand.
 */
public class HandRank implements Comparable<HandRank> {

    /**
     * Enumerates the standard poker hand categories by power.
     * <p>
     * # Principle - SRP: Defines hierarchy of hand types.
     */
    public enum RankType {
        HIGH_CARD(1),
        PAIR(2),
        TWO_PAIR(3),
        THREE_OF_A_KIND(4),
        STRAIGHT(5),
        FLUSH(6),
        FULL_HOUSE(7),
        FOUR_OF_A_KIND(8),
        STRAIGHT_FLUSH(9),
        ROYAL_FLUSH(10);

        private final int power;
        RankType(int power) { this.power = power; }
        public int getPower() { return power; }
    }

    private final RankType rankType;
    private final List<Rank> kickers; // Used to break ties within the same RankType
    private final List<Card> bestFive;

    /**
     * Constructs a HandRank with full details.
     * <p>
     * # Design - Constructor: Fully initializes the rank object.
     *
     * @param rankType The category of the hand.
     * @param kickers The list of kickers for tie-breaking.
     * @param bestFive The specific cards comprising the hand.
     */
    public HandRank(RankType rankType, List<Rank> kickers, List<Card> bestFive) {
        this.rankType = rankType;
        this.kickers = kickers;
        this.bestFive = bestFive != null ? bestFive : Collections.emptyList();
    }

    /**
     * Constructs a HandRank without specifying the best five cards.
     * <p>
     * # Design - Adapter: Provides a simplified interface for initialization (adapts inputs).
     *
     * @param rankType The category of the hand.
     * @param kickers The list of kickers for tie-breaking.
     */
    public HandRank(RankType rankType, List<Rank> kickers) {
        this(rankType, kickers, Collections.emptyList());
    }

    public RankType getRankType() { return rankType; }
    public List<Rank> getKickers() { return kickers; }
    public List<Card> getBestFive() { return bestFive; }

    /**
     * Compares this hand rank with another to determine the winner.
     * <p>
     * This method implements a specific comparison algorithm (Category then Kickers).
     * <p>
     * # Design - Strategy: Implements the comparison strategy.
     *
     * @param other The HandRank to compare against.
     * @return A negative integer, zero, or a positive integer as this hand is less than, equal to, or greater than the specified hand.
     */
    @Override
    public int compareTo(HandRank other) {
        // Compare the broad category
        int typeComparison = Integer.compare(this.rankType.getPower(), other.rankType.getPower());
        if (typeComparison != 0) {
            return typeComparison;
        }

        // If categories are same, compare kickers/high cards in order
        int minSize = Math.min(this.kickers.size(), other.kickers.size());
        for (int i = 0; i < minSize; i++) {
            int kickerComparison = Integer.compare(
                    this.kickers.get(i).getValue(),
                    other.kickers.get(i).getValue()
            );
            if (kickerComparison != 0) {
                return kickerComparison;
            }
        }

        return 0; // Truly equal hands (split pot)
    }

    /**
     * Returns a string representation of the hand rank.
     * <p>
     * # Design - String Representation: Summarizes the rank.
     *
     * @return A descriptive string.
     */
    @Override
    public String toString() {
        return rankType + " " + kickers;
    }
}