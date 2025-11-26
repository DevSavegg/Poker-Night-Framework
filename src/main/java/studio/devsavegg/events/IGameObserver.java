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
 * Implementations can register with the engine to receive real-time updates
 * about the game state, suitable for UI rendering or logging.
 */
public interface IGameObserver {

    /**
     * Called when the game engine starts a new session.
     * @param context The initial state of the game.
     */
    void onGameStarted(GameContext context);

    /**
     * Called when a new betting round or game phase begins (e.g., "Pre-Flop", "River").
     * @param phaseName The name of the new phase.
     */
    void onPhaseStart(String phaseName);

    /**
     * Called immediately after a player performs an action (Bet, Fold, etc.).
     * @param action The details of the action performed.
     */
    void onPlayerAction(PlayerAction action);

    /**
     * Called when hole cards are dealt to a specific player.
     * Note: For security, the actual Card objects might be masked for other players in a real UI,
     * but the observer receives the event knowing cards were dealt.
     * @param player The player receiving cards.
     * @param count The number of cards dealt.
     */
    void onDealHoleCards(Player player, int count);

    /**
     * Called when community cards (Flop, Turn, River) are dealt to the board.
     * @param cards The list of new cards dealt to the board.
     */
    void onDealCommunity(List<Card> cards);

    /**
     * Called when the pot total changes (usually after a betting round is consolidated).
     * @param total The new total amount in the pot.
     */
    void onPotUpdate(int total);

    /**
     * Called at the end of a hand if players reveal their cards.
     * @param showdowns A map of players to their calculated hand ranks.
     */
    void onShowdown(Map<Player, HandRank> showdowns);

    /**
     * Called when a hand concludes and chips are distributed.
     * @param winnings A map of players to the amount of chips they won.
     */
    void onHandEnded(Map<Player, Integer> winnings);

    /**
     * Called when a player fails to act within the time limit.
     * @param player The player who timed out.
     */
    void onPlayerTimeout(Player player);

    /**
     * Called when only one player remains with chips.
     */
    void onGameEnded(Player winner);
}