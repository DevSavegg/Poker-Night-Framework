package studio.devsavegg.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Stores the complete log of a specific hand for archival or replay.
 */
public class HandHistory {
    private final String handId;
    private final List<PlayerAction> actions;
    private final Map<Player, HandRank> showdowns;
    private String winnerLog;

    public HandHistory(String handId) {
        this.handId = handId;
        this.actions = new ArrayList<>();
        this.showdowns = new HashMap<>();
    }

    public void addAction(PlayerAction action) {
        this.actions.add(action);
    }

    public void addShowdown(Player player, HandRank rank) {
        this.showdowns.put(player, rank);
    }

    public void setWinnerLog(String log) {
        this.winnerLog = log;
    }

    public String getHandId() { return handId; }
    public List<PlayerAction> getActions() { return actions; }
    public Map<Player, HandRank> getShowdowns() { return showdowns; }
    public String getWinnerLog() { return winnerLog; }
}