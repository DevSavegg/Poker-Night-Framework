package studio.devsavegg.game;

import studio.devsavegg.core.*;
import studio.devsavegg.events.IGameObserver;
import studio.devsavegg.services.IGameStateService;
import studio.devsavegg.services.IPotManager;

import java.util.*;
import java.util.stream.Collectors;

public class GameEngine {
    private final Deck deck;
    private final IGameMode currentMode;
    private final IPotManager potManager;
    private final IGameStateService stateService;
    private final IPlayerInput inputService;
    private final TableManager tableManager;
    private final GameContext context;
    private final List<IGameObserver> observers = new ArrayList<>();
    private final Map<Player, Integer> roundBets = new HashMap<>();

    public GameEngine(IGameMode mode, IPotManager potManager, IGameStateService stateService,
                      IPlayerInput inputService, List<Player> players) {
        this.currentMode = mode;
        this.potManager = potManager;
        this.stateService = stateService;
        this.inputService = inputService;
        this.tableManager = new TableManager(players);
        this.deck = new Deck();
        this.context = new GameContext();
        this.context.setActivePlayers(players);
    }

    public void registerObserver(IGameObserver observer) {
        this.observers.add(observer);
    }

    /**
     * Main Game Loop
     */
    public void startGame() {
        // Continue as long as at least 2 players have chips
        while (tableManager.getAllPlayers().stream().filter(p -> p.getChipStack() > 0).count() > 1) {
            playHand();
            tableManager.moveButton();
        }

        // --- Handle Game End ---
        Player winner = tableManager.getAllPlayers().stream()
                .filter(p -> p.getChipStack() > 0)
                .findFirst()
                .orElse(null);

        if (winner != null) {
            notifyObservers(o -> o.onGameEnded(winner));
        }
    }

    private void playHand() {
        initializeHand();

        // Abstracted Forced Bets (Blinds/Antes handled by GameMode)
        currentMode.executeForcedBets(tableManager, potManager, context, roundBets);

        List<GamePhaseConfig> structure = currentMode.getStructure();
        boolean handEndedEarly = false;

        for (GamePhaseConfig phase : structure) {
            if (shouldEndHand()) { handEndedEarly = true; break; }

            executePhase(phase);

            if (shouldEndHand()) { handEndedEarly = true; break; }
        }

        if (handEndedEarly) resolveWalk();
        else resolveShowdown();
    }

    private void initializeHand() {
        deck.reset();
        potManager.startNewHand(tableManager.getAllPlayers());

        context.setCommunityCards(new ArrayList<>());
        context.setPotTotal(0);
        context.setCurrentBet(0);
        context.setMinRaise(0);

        roundBets.clear();
        for (Player p : tableManager.getAllPlayers()) {
            p.clearHand();
            roundBets.put(p, 0);
        }

        notifyObservers(o -> o.onGameStarted(context.getSnapshot()));
    }

    private void executePhase(GamePhaseConfig phase) {
        notifyObservers(o -> o.onPhaseStart(phase.phaseName));

        if (phase.holeCardsToDeal > 0) {
            dealHoleCards(phase.holeCardsToDeal);
        }

        if (phase.communityCardsToDeal > 0) {
            dealCommunityCards(phase.communityCardsToDeal);
        }

        if (phase.isDrawRound) {
            handleDrawPhase();
        }

        if (phase.isBettingRound) {
            prepareBettingRound(phase.phaseName);
            runBettingLoop(phase.phaseName);
        }
    }

    // -- Phase Helpers --

    private void dealHoleCards(int count) {
        for (Player p : tableManager.getAllPlayers()) {
            if (p.getChipStack() > 0 && !p.isSittingOut()) {
                for (int i = 0; i < count; i++) {
                    p.receiveCard(deck.deal());
                }
                notifyObservers(o -> o.onDealHoleCards(p, count));
            }
        }
    }

    private void dealCommunityCards(int count) {
        List<Card> newCards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            newCards.add(deck.deal());
        }
        context.getCommunityCards().addAll(newCards);
        notifyObservers(o -> o.onDealCommunity(newCards));
    }

    private void handleDrawPhase() {
        // Start from player after button
        int startPos = (tableManager.getButtonPosition() + 1) % tableManager.getAllPlayers().size();
        int playerCount = tableManager.getAllPlayers().size();

        for (int i = 0; i < playerCount; i++) {
            Player p = tableManager.getPlayerAt((startPos + i) % playerCount);

            if (!p.isFolded() && !p.isSittingOut() && p.getChipStack() > 0) {
                // Request discards from UI/AI
                List<Card> toDiscard = inputService.requestDiscard(p, context.getSnapshot());

                if (toDiscard != null && !toDiscard.isEmpty()) {
                    // Logic to swap cards:
                    // Identify kept cards
                    List<Card> keptCards = new ArrayList<>(p.getHoleCards());
                    keptCards.removeAll(toDiscard);

                    // Clear hand
                    p.clearHand();

                    // Restore kept cards
                    for (Card c : keptCards) {
                        p.receiveCard(c);
                    }

                    // Deal replacements
                    int drawCount = toDiscard.size();
                    for (int k = 0; k < drawCount; k++) {
                        p.receiveCard(deck.deal());
                    }

                    // Notify observers
                    notifyObservers(o -> o.onPlayerAction(new PlayerAction(p, ActionType.CHECK, 0)));
                }
            }
        }
    }

    private void prepareBettingRound(String phaseName) {
        // Reset betting metrics for new streets
        if (!phaseName.equalsIgnoreCase("Pre-Flop")) {
            context.setCurrentBet(0);
            context.setMinRaise(0);
            roundBets.replaceAll((p, v) -> 0);
        }
    }

    // -- Betting Loop --

    private void runBettingLoop(String phaseName) {
        // Determine starting player
        boolean isPreFlop = phaseName.equalsIgnoreCase("Pre-Flop");
        int startPos;

        if (isPreFlop) {
            // UTG: The player AFTER the Big Blind
            startPos = (tableManager.getBigBlindPos() + 1) % tableManager.getAllPlayers().size();
        } else {
            // Post-flop: Small Blind
            startPos = (tableManager.getButtonPosition() + 1) % tableManager.getAllPlayers().size();
        }

        Player currentPlayer = tableManager.getPlayerAt(startPos);

        // Find first VALID active player starting from calculated position
        if (currentPlayer.isFolded() || currentPlayer.isAllIn()) {
            currentPlayer = tableManager.getNextActivePlayer(tableManager.getPlayerPosition(currentPlayer));
        }

        // If only 1 or 0 players can act, skip betting
        if (currentPlayer == null || countActionablePlayers() < 2) {
            return;
        }

        boolean bettingClosed = false;
        Set<Player> actedThisStreet = new HashSet<>();

        while (!bettingClosed) {
            // Prepare Context
            context.setActingPlayer(currentPlayer);
            List<ActionType> legalActions = getLegalActions(currentPlayer);

            // Request Action
            PlayerAction action = inputService.requestAction(currentPlayer, context.getSnapshot(), legalActions);
            if (!legalActions.contains(action.getType())) {
                // Fallback for illegal moves: Auto Fold
                action = new PlayerAction(currentPlayer, ActionType.FOLD, 0);
            }

            // Process Action
            boolean isAggressiveAction = false;

            switch (action.getType()) {
                case FOLD:
                    currentPlayer.fold();
                    break;
                case CHECK:
                    // Only valid if currentBet == roundBet
                    break;
                case CALL:
                    int callAmt = context.getCurrentBet() - roundBets.getOrDefault(currentPlayer, 0);
                    placeBet(currentPlayer, callAmt, ActionType.CALL, false);
                    break;
                case BET:
                case RAISE:
                case ALL_IN:
                    int amount = action.getAmount();
                    placeBet(currentPlayer, amount, action.getType(), false);

                    // If this was a raise, reopen betting for others
                    if (roundBets.get(currentPlayer) > context.getCurrentBet()) {
                        isAggressiveAction = true;
                    } else if (action.getType() == ActionType.ALL_IN && roundBets.get(currentPlayer) > context.getCurrentBet()) {
                        isAggressiveAction = true;
                    }
                    break;
            }

            PlayerAction finalAction = action;
            notifyObservers(o -> o.onPlayerAction(finalAction));

            // Update Loop State
            actedThisStreet.add(currentPlayer);

            if (isAggressiveAction) {
                // Someone raised. Everyone else needs to act again
                actedThisStreet.clear();
                actedThisStreet.add(currentPlayer);
            }

            // Check Termination Condition
            if (shouldEndHand()) {
                bettingClosed = true;
            } else if (isBettingSettled(actedThisStreet)) {
                bettingClosed = true;
            } else {
                // Move to next
                currentPlayer = tableManager.getNextActivePlayer(tableManager.getPlayerPosition(currentPlayer));
                if (currentPlayer == null) bettingClosed = true;
            }
        }
    }

    private void placeBet(Player player, int amount, ActionType type, boolean isBlind) {
        // Cap bet at player's stack first
        int actualAmount = Math.min(amount, player.getChipStack());

        PlayerAction checkAction = new PlayerAction(player, type, actualAmount);

        if (!currentMode.getBettingStructure().isBetValid(checkAction, context)) {
            throw new IllegalArgumentException("Invalid bet amount for this structure: " + actualAmount);
        }

        // Modifying State
        player.bet(actualAmount);
        potManager.processBet(player, actualAmount);

        // Update Round Tracker
        int oldRoundTotal = roundBets.getOrDefault(player, 0);
        int newRoundTotal = oldRoundTotal + actualAmount;
        roundBets.put(player, newRoundTotal);

        // Update Context (High Bet / Min Raise)
        if (newRoundTotal > context.getCurrentBet()) {
            int increase = newRoundTotal - context.getCurrentBet();

            // Only increase min-raise if it's a "full" raise
            if (increase >= context.getMinRaise()) {
                context.setMinRaise(increase);
            }
            context.setCurrentBet(newRoundTotal);
        }

        context.setPotTotal(potManager.getCurrentTotal());
        notifyObservers(o -> o.onPotUpdate(context.getPotTotal()));
    }

    // -- Resolution --

    private void resolveShowdown() {
        // Finalize pot structure (Handle all-in side pots)
        potManager.calculateSidePots();

        // Evaluate Hands
        Map<Player, HandRank> hands = new HashMap<>();
        List<Player> active = tableManager.getAllPlayers().stream()
                .filter(p -> !p.isFolded())
                .toList();

        for (Player p : active) {
            HandRank rank = currentMode.getEvaluator().evaluate(p.getHoleCards(), context.getCommunityCards());
            hands.put(p, rank);
        }

        notifyObservers(o -> o.onShowdown(hands));

        // Distribute
        Map<Player, Integer> winnings = potManager.resolvePots(hands);
        distributeWinnings(winnings);
    }

    private void resolveWalk() {
        // Everyone folded except one
        Player winner = tableManager.getAllPlayers().stream()
                .filter(p -> !p.isFolded())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No active players in resolveWalk"));

        Map<Player, Integer> winnings = new HashMap<>();
        winnings.put(winner, potManager.getCurrentTotal());
        distributeWinnings(winnings);
    }

    private void distributeWinnings(Map<Player, Integer> winnings) {
        winnings.forEach(Player::addChips);
        notifyObservers(o -> o.onHandEnded(winnings));

        if (stateService != null) {
            stateService.savePlayerChips(tableManager.getAllPlayers());
        }
    }

    // -- Helpers & Validation --

    private List<ActionType> getLegalActions(Player p) {
        int maxBet = currentMode.getBettingStructure().calculateMaxBet(context);
        List<ActionType> actions = new ArrayList<>();
        actions.add(ActionType.FOLD);

        int currentHighBet = context.getCurrentBet();
        int myContribution = roundBets.getOrDefault(p, 0);
        int toCall = currentHighBet - myContribution;

        // Check vs Call
        if (toCall == 0) {
            actions.add(ActionType.CHECK);
            actions.add(ActionType.BET); // Initiating a bet
        } else {
            actions.add(ActionType.CALL);
            actions.add(ActionType.RAISE); // Raising the bet
        }

        actions.add(ActionType.ALL_IN); // Always an option (unless limit poker)
        return actions;
    }

    private boolean isBettingSettled(Set<Player> actedThisStreet) {
        List<Player> active = getActivePlayersList(); // Non-folded, Non-All-in

        // Has everyone acted?
        if (!actedThisStreet.containsAll(active)) return false;

        // Has everyone matched the bet?
        for (Player p : active) {
            if (roundBets.getOrDefault(p, 0) < context.getCurrentBet()) {
                return false;
            }
        }
        return true;
    }

    private List<Player> getActivePlayersList() {
        return tableManager.getAllPlayers().stream()
                .filter(p -> !p.isFolded() && !p.isAllIn())
                .collect(Collectors.toList());
    }

    private int countActionablePlayers() {
        return (int) tableManager.getAllPlayers().stream()
                .filter(p -> !p.isFolded() && !p.isAllIn())
                .count();
    }

    private boolean shouldEndHand() {
        long activeCount = tableManager.getAllPlayers().stream()
                .filter(p -> !p.isFolded())
                .count();
        return activeCount < 2;
    }

    private void notifyObservers(java.util.function.Consumer<IGameObserver> action) {
        observers.forEach(action);
    }
}