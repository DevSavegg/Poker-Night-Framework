package studio.devsavegg.services;

import studio.devsavegg.core.Card;
import studio.devsavegg.core.HandRank;
import java.util.List;

/**
 * Interface for evaluating the strength of a poker hand.
 * <p>
 * # Principle - Single Responsibility Principle (SRP): Solely responsible for ranking cards.
 */
public interface IHandEvaluator {
    /**
     * Evaluates the strength of a hand given hole cards and community cards.
     * <p>
     * Typically, selects the best 5 cards out of 7.
     * <p>
     * # Design - Strategy: Defines the interface for the evaluation algorithm.
     *
     * @param hole The list of hole cards.
     * @param board The list of community cards.
     * @return The calculated {@link HandRank}.
     */
    HandRank evaluate(List<Card> hole, List<Card> board);
}