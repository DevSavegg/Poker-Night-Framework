package studio.devsavegg.core;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the current mutable state of the game table.
 * Used by services to determine legal moves and by UI to render the table.
 */
public class GameContext {
    private int potTotal;
    private int currentBet;
    private int minRaise;
    private List<Card> communityCards;
    private List<Player> activePlayers;
    private Player actingPlayer;

    public GameContext() {
        this.potTotal = 0;
        this.currentBet = 0;
        this.minRaise = 0;
        this.communityCards = new ArrayList<>();
        this.activePlayers = new ArrayList<>();
        this.actingPlayer = null;
    }

    private GameContext(GameContext original) {
        this.potTotal = original.potTotal;
        this.currentBet = original.currentBet;
        this.minRaise = original.minRaise;
        this.communityCards = new ArrayList<>(original.communityCards);
        this.activePlayers = new ArrayList<>(original.activePlayers);
        this.actingPlayer = original.actingPlayer;
    }

    /**
     * Creates a deep copy (snapshot) of the current game context.
     */
    public GameContext getSnapshot() {
        return new GameContext(this);
    }

    // -- Getters & Setters --

    public int getPotTotal() { return potTotal; }
    public void setPotTotal(int potTotal) { this.potTotal = potTotal; }

    public int getCurrentBet() { return currentBet; }
    public void setCurrentBet(int currentBet) { this.currentBet = currentBet; }

    public int getMinRaise() { return minRaise; }
    public void setMinRaise(int minRaise) { this.minRaise = minRaise; }

    public List<Card> getCommunityCards() { return communityCards; }
    public void setCommunityCards(List<Card> communityCards) { this.communityCards = communityCards; }

    public List<Player> getActivePlayers() { return activePlayers; }
    public void setActivePlayers(List<Player> activePlayers) { this.activePlayers = activePlayers; }

    public Player getActingPlayer() { return actingPlayer; }
    public void setActingPlayer(Player actingPlayer) { this.actingPlayer = actingPlayer; }
}