package studio.devsavegg.services;

import studio.devsavegg.core.*;
import studio.devsavegg.core.HandRank.RankType;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Standard implementation of poker hand evaluation logic.
 * <p>
 * # Principle - Single Responsibility Principle (SRP): Encapsulates the rules for determining hand strength.
 */
public class StandardHandEvaluator implements IHandEvaluator {

    /**
     * Evaluates the best possible 5-card hand from hole cards and board.
     * <p>
     * # Design - Strategy: Concrete implementation of the evaluation strategy.
     *
     * @param hole The player's hole cards.
     * @param board The community cards.
     * @return The resulting {@link HandRank}.
     */
    @Override
    public HandRank evaluate(List<Card> hole, List<Card> board) {
        List<Card> allCards = new ArrayList<>(hole);
        allCards.addAll(board);
        allCards.sort(Collections.reverseOrder()); // Descending order

        // Flush & Straight Flush
        List<Card> flushCards = getFlushCards(allCards);
        if (flushCards != null) {
            List<Rank> sfRanks = getStraightRanks(flushCards);
            if (sfRanks != null) {
                // Straight Flush: Find the 5 cards that make this straight
                List<Card> sfHand = findCardsForStraight(flushCards, sfRanks);
                if (sfRanks.get(0) == Rank.ACE) {
                    return new HandRank(RankType.ROYAL_FLUSH, Collections.emptyList(), sfHand);
                }
                return new HandRank(RankType.STRAIGHT_FLUSH, sfRanks, sfHand);
            }
            // Regular Flush: Top 5 cards of the flush suit
            List<Rank> flushKickers = flushCards.stream().limit(5).map(Card::getRank).collect(Collectors.toList());
            return new HandRank(RankType.FLUSH, flushKickers, flushCards.subList(0, 5));
        }

        // Rank-based analysis
        Map<Rank, Integer> rankCounts = getRankCounts(allCards);

        // Four of a Kind
        if (hasNOfAKind(rankCounts, 4)) {
            Rank quadRank = getRankByCount(rankCounts, 4);
            List<Rank> kickers = getKickers(allCards, 1, Collections.singletonList(quadRank));
            // Collect cards
            List<Card> hand = collectCards(allCards, Collections.singletonList(quadRank), List.of(4), kickers);
            return new HandRank(RankType.FOUR_OF_A_KIND, combine(quadRank, kickers), hand);
        }

        // Full House
        if (hasNOfAKind(rankCounts, 3) && (rankCounts.size() >= 2)) {
            Iterator<Rank> it = rankCounts.keySet().iterator();
            Rank tripsRank = it.next();
            Rank pairRank = it.next();

            // Handle edge case where we have two trips or trips+pair
            if (rankCounts.get(pairRank) >= 2) {
                List<Card> hand = collectCards(allCards, Arrays.asList(tripsRank, pairRank), Arrays.asList(3, 2), Collections.emptyList());
                return new HandRank(RankType.FULL_HOUSE, Arrays.asList(tripsRank, pairRank), hand);
            }
        }

        // Straight
        List<Rank> straightRanks = getStraightRanks(allCards);
        if (straightRanks != null) {
            List<Card> hand = findCardsForStraight(allCards, straightRanks);
            return new HandRank(RankType.STRAIGHT, straightRanks, hand);
        }

        // Three of a Kind
        if (hasNOfAKind(rankCounts, 3)) {
            Rank tripRank = getRankByCount(rankCounts, 3);
            List<Rank> kickers = getKickers(allCards, 2, Collections.singletonList(tripRank));
            List<Card> hand = collectCards(allCards, Collections.singletonList(tripRank), List.of(3), kickers);
            return new HandRank(RankType.THREE_OF_A_KIND, combine(tripRank, kickers), hand);
        }

        // Two Pair
        if (getCountOfCount(rankCounts, 2) >= 2) {
            Iterator<Rank> it = rankCounts.keySet().iterator();
            Rank pair1 = it.next();
            Rank pair2 = it.next();
            List<Rank> kickers = getKickers(allCards, 1, Arrays.asList(pair1, pair2));

            List<Card> hand = collectCards(allCards, Arrays.asList(pair1, pair2), Arrays.asList(2, 2), kickers);

            List<Rank> resultRanks = new ArrayList<>();
            resultRanks.add(pair1);
            resultRanks.add(pair2);
            resultRanks.addAll(kickers);
            return new HandRank(RankType.TWO_PAIR, resultRanks, hand);
        }

        // One Pair
        if (hasNOfAKind(rankCounts, 2)) {
            Rank pairRank = getRankByCount(rankCounts, 2);
            List<Rank> kickers = getKickers(allCards, 3, Collections.singletonList(pairRank));
            List<Card> hand = collectCards(allCards, Collections.singletonList(pairRank), List.of(2), kickers);
            return new HandRank(RankType.PAIR, combine(pairRank, kickers), hand);
        }

        // High Card
        List<Rank> kickers = getKickers(allCards, 5, Collections.emptyList());
        List<Card> hand = collectCards(allCards, Collections.emptyList(), Collections.emptyList(), kickers);
        return new HandRank(RankType.HIGH_CARD, kickers, hand);
    }

    // --- Helpers for Card Collection ---

    /**
     * Reconstructs a Straight hand from the pool of cards.
     * <p>
     * # Design - Helper: Utility to find specific card instances.
     *
     * @param pool The available cards.
     * @param targetRanks The ranks needed for the straight.
     * @return The list of cards.
     */
    private List<Card> findCardsForStraight(List<Card> pool, List<Rank> targetRanks) {
        List<Card> result = new ArrayList<>();
        // For each rank in the straight, find the FIRST card in the sorted pool that matches
        for (Rank r : targetRanks) {
            for (Card c : pool) {
                if (c.getRank() == r) {
                    result.add(c);
                    break; // Found the card for this rank, move to next rank
                }
            }
        }
        return result;
    }

    /**
     * Collects specific sets of cards (e.g., pairs, trips) and kickers.
     * <p>
     * # Design - Helper: Utility for constructing hand composition.
     *
     * @param pool The available cards.
     * @param ranksToFind The ranks of the sets to find.
     * @param counts The count needed for each rank.
     * @param kickerRanks The ranks of the kickers to find.
     * @return The collected list of cards.
     */
    private List<Card> collectCards(List<Card> pool, List<Rank> ranksToFind, List<Integer> counts, List<Rank> kickerRanks) {
        List<Card> hand = new ArrayList<>();

        // Get the sets (Trips/Pairs)
        for (int i = 0; i < ranksToFind.size(); i++) {
            Rank r = ranksToFind.get(i);
            int countNeeded = counts.get(i);
            int found = 0;
            for (Card c : pool) {
                if (c.getRank() == r && found < countNeeded) {
                    hand.add(c);
                    found++;
                }
            }
        }

        // Get the kickers
        for (Rank r : kickerRanks) {
            for (Card c : pool) {
                if (c.getRank() == r) {
                    // Check if we already added this specific card
                    if (!hand.contains(c)) {
                        hand.add(c);
                        break;
                    }
                }
            }
        }
        return hand;
    }

    /**
     * Identifies if a flush exists and returns the cards comprising it.
     * <p>
     * # Design - Filter: Filters cards by suit group.
     *
     * @param allCards The pool of cards to check.
     * @return A list of at least 5 cards of the same suit, or null if no flush exists.
     */
    private List<Card> getFlushCards(List<Card> allCards) {
        Map<Suit, List<Card>> suitMap = new HashMap<>();
        for (Card c : allCards) {
            suitMap.computeIfAbsent(c.getSuit(), k -> new ArrayList<>()).add(c);
        }
        for (List<Card> suitedCards : suitMap.values()) {
            if (suitedCards.size() >= 5) {
                suitedCards.sort(Collections.reverseOrder());
                return suitedCards;
            }
        }
        return null;
    }

    /**
     * Checks if a list of cards contains a sequential straight.
     * <p>
     * # Design - Algorithm: Sequential search for rank continuity.
     *
     * @param cards The cards to check (usually flushed cards or all cards).
     * @return A list of 5 ranks forming the straight, or null.
     */
    private List<Rank> getStraightRanks(List<Card> cards) {
        List<Rank> distinctRanks = cards.stream()
                .map(Card::getRank)
                .distinct()
                .collect(Collectors.toList());

        if (distinctRanks.size() < 5) return null;

        for (int i = 0; i <= distinctRanks.size() - 5; i++) {
            if (isSequence(distinctRanks, i)) {
                return distinctRanks.subList(i, i + 5);
            }
        }

        if (distinctRanks.contains(Rank.ACE) && distinctRanks.contains(Rank.FIVE) &&
                distinctRanks.contains(Rank.FOUR) && distinctRanks.contains(Rank.THREE) &&
                distinctRanks.contains(Rank.TWO)) {
            return Arrays.asList(Rank.FIVE, Rank.FOUR, Rank.THREE, Rank.TWO, Rank.ACE);
        }
        return null;
    }

    /**
     * Helper method to verify if 5 ranks form a numerical sequence.
     * <p>
     * # Design - Helper: Arithmetic check for sequence.
     *
     * @param ranks The list of ranks.
     * @param startIndex The index to start checking from.
     * @return {@code true} if the next 5 ranks are sequential.
     */
    private boolean isSequence(List<Rank> ranks, int startIndex) {
        int startVal = ranks.get(startIndex).getValue();
        for (int i = 1; i < 5; i++) {
            if (ranks.get(startIndex + i).getValue() != startVal - i) return false;
        }
        return true;
    }

    /**
     * Counts the occurrences of each rank in the provided cards.
     * <p>
     * # Design - Aggregator: Groups and counts items by property.
     *
     * @param cards The list of cards to analyze.
     * @return A map of Rank to Count, sorted by Count (desc) then Rank (desc).
     */
    private Map<Rank, Integer> getRankCounts(List<Card> cards) {
        Map<Rank, Integer> rawCounts = new HashMap<>();
        for (Card c : cards) rawCounts.put(c.getRank(), rawCounts.getOrDefault(c.getRank(), 0) + 1);

        return rawCounts.entrySet().stream()
                .sorted((e1, e2) -> {
                    int compareCount = e2.getValue().compareTo(e1.getValue());
                    if (compareCount != 0) return compareCount;
                    return Integer.compare(e2.getKey().getValue(), e1.getKey().getValue());
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
    }

    /**
     * Checks if any rank appears exactly N times.
     * <p>
     * # Design - Predicate: Tests a condition against the collection.
     *
     * @param counts The map of rank counts.
     * @param n The number of occurrences to find.
     * @return {@code true} if any rank appears N times.
     */
    private boolean hasNOfAKind(Map<Rank, Integer> counts, int n) {
        return counts.values().stream().anyMatch(count -> count == n);
    }

    /**
     * Counts how many ranks appear exactly N times (e.g., how many pairs exist).
     * <p>
     * # Design - Aggregator: Counts matches.
     *
     * @param counts The map of rank counts.
     * @param n The occurrence count to look for.
     * @return The number of ranks that have exactly N copies.
     */
    private long getCountOfCount(Map<Rank, Integer> counts, int n) {
        return counts.values().stream().filter(count -> count == n).count();
    }

    /**
     * Retrieves the specific Rank that appears N times.
     * <p>
     * # Design - Search: Finds the key associated with a specific value.
     *
     * @param counts The map of rank counts.
     * @param n The occurrence count to find.
     * @return The {@link Rank} matching the count, or null.
     */
    private Rank getRankByCount(Map<Rank, Integer> counts, int n) {
        return counts.entrySet().stream().filter(e -> e.getValue() == n).findFirst().map(Map.Entry::getKey).orElse(null);
    }

    /**
     * Selects kicker cards (high cards) to complete a hand.
     * <p>
     * # Design - Filter: Selects distinct elements excluding specific values.
     *
     * @param cards The source list of cards.
     * @param count The number of kickers needed.
     * @param exclude The ranks to exclude (usually the main hand components).
     * @return A list of {@link Rank} representing the kickers.
     */
    private List<Rank> getKickers(List<Card> cards, int count, List<Rank> exclude) {
        return cards.stream().map(Card::getRank).filter(r -> !exclude.contains(r)).distinct().limit(count).collect(Collectors.toList());
    }

    /**
     * Combines a main rank with a list of other ranks.
     * <p>
     * # Design - Helper: List manipulation utility.
     *
     * @param main The primary rank.
     * @param others The list of secondary ranks.
     * @return A merged list.
     */
    private List<Rank> combine(Rank main, List<Rank> others) {
        List<Rank> combined = new ArrayList<>();
        combined.add(main);
        combined.addAll(others);
        return combined;
    }
}