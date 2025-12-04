package studio.devsavegg.game;

import studio.devsavegg.core.*;
import studio.devsavegg.events.IGameObserver;
import studio.devsavegg.services.IGameStateService;
import studio.devsavegg.services.IPotManager;

import java.util.*;
import java.util.stream.Collectors;

/**
 * The central controller of the poker game.
 * <p>
 * Manages the flow of the game, including phases, betting rounds, and state transitions.
 * <p>
 * # Principle - Mediator Pattern / SRP: Coordinators the interaction between Players, Deck, Pot, and Rules without them coupled directly.
 */
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

    /**
     * Constructs the GameEngine with necessary dependencies.
     * <p>
     * # Design - Mediator: Initializes the central hub with its colleagues.
     *
     * @param mode The {@link IGameMode} ruleset to use.
     * @param potManager The service for managing pot calculations.
     * @param stateService The service for persistence (optional).
     * @param inputService The service for retrieving player input.
     * @param players The list of participants.
     */
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

    /**
     * Registers an observer to receive game events.
     * <p>
     * # Design - Observer: Adds a listener to the subject.
     *
     * @param observer The {@link IGameObserver} implementation.
     */
    public void registerObserver(IGameObserver observer) {
        this.observers.add(observer);
    }

    /**
     * Starts the main game loop.
     * <p>
     * Runs continuously until fewer than 2 players have chips remaining.
     * <p>
     * # Design - Template Method (Loose variation): Defines the high-level skeleton of the game lifecycle.
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

    /**
     * Executes a single hand of poker.
     * <p>
     * Iterates through the phases defined by the current {@link IGameMode}.
     * <p>
     * # Design - Command / Sequencer: Executes a sequence of game phases.
     */
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

    /**
     * Resets the game state for a new hand.
     * <p>
     * # Design - State: Resets transient state variables.
     */
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

    /**
     * Executes a specific phase of the game (e.g., Flop, Turn).
     * <p>
     * # Design - Strategy: Executes logic based on the configuration of the phase.
     *
     * @param phase The {@link GamePhaseConfig} to execute.
     */
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

    /**
     * Deals hole cards to all eligible players.
     * <p>
     * # Design - Distributor: Distributes resources (cards) to participants.
     *
     * @param count The number of cards to deal to each player.
     */
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

    /**
     * Deals community cards to the board.
     * <p>
     * # Design - Distributor: Adds shared resources to the common context.
     *
     * @param count The number of cards to deal.
     */
    private void dealCommunityCards(int count) {
        List<Card> newCards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            newCards.add(deck.deal());
        }
        context.getCommunityCards().addAll(newCards);
        notifyObservers(o -> o.onDealCommunity(newCards));
    }

    /**
     * Handles the draw phase where players can discard and replace cards.
     * <p>
     * # Design - Algorithm: Logic for the card exchange process.
     */
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

    /**
     * Resets betting metrics for the start of a new betting round.
     * <p>
     * # Design - State: Prepares state for a specific sub-routine.
     *
     * @param phaseName The name of the phase.
     */
    private void prepareBettingRound(String phaseName) {
        // Reset betting metrics for new streets
        if (!phaseName.equalsIgnoreCase("Pre-Flop")) {
            context.setCurrentBet(0);
            context.setMinRaise(0);
            roundBets.replaceAll((p, v) -> 0);
        }
    }

    // -- Betting Loop --

    /**
     * Executes the betting logic for a round.
     * <p>
     * Cycles through players until betting is settled (everyone checks, folds, or matches the highest bet).
     * <p>
     * # Design - State Machine: Manages the states of a betting round (Bet, Call, Raise, Fold).
     *
     * @param phaseName The name of the phase.
     */
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

    /**
     * Validates and executes a betting action (Bet, Raise, Call).
     * <p>
     * # Design - Command: Executes the financial transaction of a bet.
     *
     * @param player The betting player.
     * @param amount The amount to bet.
     * @param type The type of action.
     * @param isBlind Whether this is a forced blind bet.
     */
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

    /**
     * Resolves the hand when multiple players remain at the end.
     * <p>
     * # Design - Algorithm: Logic for showdown evaluation and winner determination.
     */
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

    /**
     * Resolves the hand when all players but one have folded.
     * <p>
     * # Design - Algorithm: Logic for walkover victory.
     */
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

    /**
     * Distributes winnings to players and updates persistence.
     * <p>
     * # Design - Command: Executes the payout.
     *
     * @param winnings A map of players to their won amounts.
     */
    private void distributeWinnings(Map<Player, Integer> winnings) {
        winnings.forEach(Player::addChips);
        notifyObservers(o -> o.onHandEnded(winnings));

        if (stateService != null) {
            stateService.savePlayerChips(tableManager.getAllPlayers());
        }
    }

    // -- Helpers & Validation --

    /**
     * Calculates the list of legal actions for a player in the current context.
     * <p>
     * # Design - Strategy/Validator: Determines valid moves based on game rules.
     *
     * @param p The player to check.
     * @return A list of {@link ActionType}.
     */
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

    /**
     * Checks if the betting round is settled.
     * <p>
     * Betting is settled when all active players have acted and matched the highest bet.
     * <p>
     * # Design - State Inspector: Checks convergence of the betting state.
     *
     * @param actedThisStreet The set of players who have acted at least once.
     * @return {@code true} if betting is closed, {@code false} otherwise.
     */
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

    /**
     * Retrieves list of active players who are not All-In.
     * <p>
     * # Design - Filter: Helper method to filter stream.
     *
     * @return List of {@link Player}.
     */
    private List<Player> getActivePlayersList() {
        return tableManager.getAllPlayers().stream()
                .filter(p -> !p.isFolded() && !p.isAllIn())
                .collect(Collectors.toList());
    }

    /**
     * Counts how many players can still take actions (not folded, not all-in).
     * <p>
     * # Design - Aggregator: Helper calculation.
     *
     * @return The count of actionable players.
     */
    private int countActionablePlayers() {
        return (int) tableManager.getAllPlayers().stream()
                .filter(p -> !p.isFolded() && !p.isAllIn())
                .count();
    }

    /**
     * Checks if the hand should end immediately (e.g., only 1 player left).
     * <p>
     * # Design - State Inspector: Checks termination condition.
     *
     * @return {@code true} if the hand should end.
     */
    private boolean shouldEndHand() {
        long activeCount = tableManager.getAllPlayers().stream()
                .filter(p -> !p.isFolded())
                .count();
        return activeCount < 2;
    }

    /**
     * Notifies all registered observers of an event.
     * <p>
     * # Design - Observer: Multicasts events to subscribers.
     *
     * @param action The action to perform on each observer.
     */
    private void notifyObservers(java.util.function.Consumer<IGameObserver> action) {
        observers.forEach(action);
    }
}