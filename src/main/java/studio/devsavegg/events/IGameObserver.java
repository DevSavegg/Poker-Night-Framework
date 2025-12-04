package studio.devsavegg.events;

import studio.devsavegg.core.Card;
import studio.devsavegg.core.GameContext;
import studio.devsavegg.core.HandRank;
import studio.devsavegg.core.Player;
import studio.devsavegg.core.PlayerAction;

import java.util.List;
import java.util.Map;

/**
 * Observer interface for the Poker Game Engine.
 * <p>
 * Implementations can register with the engine to receive real-time updates
 * about the game state, suitable for UI rendering or logging.
 * <p>
 * # Principle - Dependency Inversion Principle (DIP): High-level game logic depends on this abstraction rather than concrete UI or logging implementations.
 */
public interface IGameObserver {

    /**
     * Called when the game engine starts a new session.
     * <p>
     * # Design - Observer: Standard update method triggered by a state change subject.
     *
     * @param context The initial {@link GameContext} of the game.
     */
    void onGameStarted(GameContext context);

    /**
     * Called when a new betting round or game phase begins.
     * <p>
     * # Design - Observer: Standard update method triggered by a state change subject.
     *
     * @param phaseName The name of the new phase (e.g., "Pre-Flop", "River").
     */
    void onPhaseStart(String phaseName);

    /**
     * Called immediately after a player performs an action.
     * <p>
     * # Design - Observer: Standard update method triggered by a state change subject.
     *
     * @param action The {@link PlayerAction} details containing actor, type, and amount.
     */
    void onPlayerAction(PlayerAction action);

    /**
     * Called when hole cards are dealt to a specific player.
     * <p>
     * Note: For security, the actual {@link Card} objects might be masked for other players in a real UI,
     * but the observer receives the event knowing cards were dealt.
     * <p>
     * # Design - Observer: Standard update method triggered by a state change subject.
     *
     * @param player The {@link Player} receiving cards.
     * @param count The number of cards dealt.
     */
    void onDealHoleCards(Player player, int count);

    /**
     * Called when community cards are dealt to the board.
     * <p>
     * # Design - Observer: Standard update method triggered by a state change subject.
     *
     * @param cards The list of new {@link Card} objects dealt to the board.
     */
    void onDealCommunity(List<Card> cards);

    /**
     * Called when the pot total changes.
     * <p>
     * This usually occurs after a betting round is consolidated.
     * <p>
     * # Design - Observer: Standard update method triggered by a state change subject.
     *
     * @param total The new total amount in the pot.
     */
    void onPotUpdate(int total);

    /**
     * Called at the end of a hand if players reveal their cards.
     * <p>
     * # Design - Observer: Standard update method triggered by a state change subject.
     *
     * @param showdowns A map of {@link Player} to their calculated {@link HandRank}.
     */
    void onShowdown(Map<Player, HandRank> showdowns);

    /**
     * Called when a hand concludes and chips are distributed.
     * <p>
     * # Design - Observer: Standard update method triggered by a state change subject.
     *
     * @param winnings A map of {@link Player} to the amount of chips they won.
     */
    void onHandEnded(Map<Player, Integer> winnings);

    /**
     * Called when a player fails to act within the time limit.
     * <p>
     * # Design - Observer: Standard update method triggered by a state change subject.
     *
     * @param player The {@link Player} who timed out.
     */
    void onPlayerTimeout(Player player);

    /**
     * Called when only one player remains with chips.
     * <p>
     * # Design - Observer: Standard update method triggered by a state change subject.
     *
     * @param winner The {@link Player} who won the session.
     */
    void onGameEnded(Player winner);
}