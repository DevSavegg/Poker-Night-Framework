package studio.devsavegg.gui;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;
import studio.devsavegg.core.*;
import studio.devsavegg.services.StandardHandEvaluator;

import java.net.URL;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CancellationException;
import java.util.function.BiConsumer;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * Main Controller for the Poker Game UI.
 * <p>
 * Manages the JavaFX scene graph, handles user interactions, and updates the view based on game state changes.
 * Acts as the central coordinator between the visual components and the game logic.
 * <p>
 * # Principle - MVC Controller / Mediator: Mediates between the View (JavaFX nodes) and the Model (Game Engine).
 */
public class PokerTableController {
    private final StackPane mainRoot;

    private VBox homeScreen;
    private VBox settingsScreen;
    private BorderPane gameScreen;

    private final HBox communityCardsBox;
    private final Label potLabel;
    private final Label phaseLabel;
    private final TextArea gameLog;
    private final VBox controlsBox;
    private final HBox seatsContainer;

    // UI Overlay containers
    private final VBox privacyOverlay;
    private final VBox resultsOverlay;
    private final VBox winnerOverlay;

    // State Tracking
    private final Map<Player, VBox> playerSeats = new HashMap<>();

    // Future tracking for async user input
    private CompletableFuture<PlayerAction> pendingActionFuture;
    private CompletableFuture<List<Card>> pendingDiscardFuture;

    private final List<Card> selectedDiscards = new ArrayList<>();
    private Player currentActor;
    private final StandardHandEvaluator evaluator = new StandardHandEvaluator();
    private final List<String> playerNames = new ArrayList<>();
    private int initialChips = 1000;
    private BiConsumer<String, List<Player>> onGameStartRequest;

    /**
     * Constructs the controller and initializes the UI layout.
     * <p>
     * # Design - Composition: Assembles the initial visual structure from smaller UI components.
     */
    public PokerTableController() {
        mainRoot = new StackPane();

        // Load CSS safely
        URL cssResource = getClass().getResource("/styles.css");
        if (cssResource != null) {
            mainRoot.getStylesheets().add(cssResource.toExternalForm());
        } else {
            System.err.println("Error: styles.css not found in resources!");
        }

        mainRoot.getStyleClass().add("root");

        // Initialize UI components
        initHomeScreen();
        initSettingsScreen();

        communityCardsBox = new HBox(8);
        communityCardsBox.setAlignment(Pos.CENTER);

        potLabel = new Label("POT: $0");
        potLabel.getStyleClass().add("header-text");
        potLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: #fbbf24;");

        phaseLabel = new Label("WAITING FOR GAME");
        phaseLabel.getStyleClass().add("sub-header");
        phaseLabel.setStyle("-fx-text-fill: #10b981; -fx-spacing: 2px;");

        controlsBox = new VBox(15);
        controlsBox.setAlignment(Pos.CENTER);
        controlsBox.setPadding(new Insets(20));
        controlsBox.getStyleClass().add("glass-panel");
        controlsBox.setVisible(false);

        gameLog = new TextArea();
        gameLog.setEditable(false);
        gameLog.setPrefHeight(100);
        gameLog.setWrapText(true);
        gameLog.getStyleClass().add("game-log");

        seatsContainer = new HBox(15);
        seatsContainer.setAlignment(Pos.CENTER);
        seatsContainer.setPadding(new Insets(10));

        privacyOverlay = createOverlay("Pass the Device");
        resultsOverlay = createOverlay("Round Results");
        winnerOverlay = createOverlay("Game Over");

        initGameScreen();
        showHome();
    }

    /**
     * Helper to create consistent overlay containers.
     * <p>
     * # Design - Factory Method (Helper): Centralizes creation logic for overlay panes.
     */
    private VBox createOverlay(String title) {
        VBox overlay = new VBox(20);
        overlay.setAlignment(Pos.CENTER);
        overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.85);");
        overlay.setVisible(false);
        overlay.setOpacity(0);
        return overlay;
    }

    // ... [Init methods remain largely the same, skipped for brevity in diff-check, but included in full file] ...
    private void initHomeScreen() {
        homeScreen = new VBox(25);
        homeScreen.setAlignment(Pos.CENTER);

        URL logoUrl = getClass().getResource("/Logo.png");
        if (logoUrl != null) {
            ImageView logoView = new ImageView(new Image(logoUrl.toExternalForm()));
            logoView.setPreserveRatio(true);
            logoView.setFitWidth(500);
            logoView.setEffect(new javafx.scene.effect.DropShadow(20, Color.BLACK));
            homeScreen.getChildren().add(logoView);
        } else {
            Label title = new Label("MIDNIGHT POKER");
            title.getStyleClass().add("header-text");
            title.setStyle("-fx-font-size: 42px;");
            homeScreen.getChildren().add(title);
        }

        // Ensure buttons are created even if logo fails
        if (homeScreen.getChildren().isEmpty()) {
            Label title = new Label("MIDNIGHT POKER");
            title.getStyleClass().add("header-text");
            homeScreen.getChildren().add(title);
        }

        Button btnStart = new Button("NEW GAME");
        btnStart.getStyleClass().setAll("button", "button-primary");
        btnStart.setPrefWidth(220);
        btnStart.setOnAction(e -> showModeSelection());

        Button btnSettings = new Button("SETTINGS");
        btnSettings.getStyleClass().setAll("button", "button-secondary");
        btnSettings.setPrefWidth(220);
        btnSettings.setOnAction(e -> showSettings());

        Button btnExit = new Button("EXIT TO DESKTOP");
        btnExit.getStyleClass().setAll("button", "button-tertiary");
        btnExit.setPrefWidth(220);
        btnExit.setOnAction(e -> Platform.exit());

        homeScreen.getChildren().addAll(btnStart, btnSettings, btnExit);
        mainRoot.getChildren().add(homeScreen);
    }

    private void initSettingsScreen() {
        settingsScreen = new VBox(20);
        settingsScreen.setAlignment(Pos.CENTER);
        settingsScreen.setMaxSize(500, 600);
        settingsScreen.getStyleClass().add("glass-panel");
        settingsScreen.setPadding(new Insets(30));
        settingsScreen.setVisible(false);

        Label title = new Label("CONFIGURATION");
        title.getStyleClass().add("header-text");

        HBox chipBox = new HBox(10);
        chipBox.setAlignment(Pos.CENTER);
        Label chipLbl = new Label("Starting Chips:");
        chipLbl.setStyle("-fx-text-fill: white;");
        TextField chipField = new TextField("1000");
        chipField.setPrefWidth(100);
        chipBox.getChildren().addAll(chipLbl, chipField);

        ListView<String> nameList = new ListView<>();
        nameList.setPrefHeight(200);
        nameList.setStyle("-fx-control-inner-background: #27272a; -fx-background-color: transparent;");

        playerNames.addAll(Arrays.asList("Alice", "Bob", "Charlie"));
        nameList.getItems().addAll(playerNames);

        HBox editBox = new HBox(10);
        editBox.setAlignment(Pos.CENTER);
        TextField nameInput = new TextField();
        nameInput.setPromptText("New Player Name");
        Button addBtn = new Button("+");
        addBtn.getStyleClass().add("button");
        addBtn.setOnAction(e -> {
            if (!nameInput.getText().isEmpty() && playerNames.size() < 8) {
                playerNames.add(nameInput.getText());
                nameList.getItems().add(nameInput.getText());
                nameInput.clear();
            }
        });
        Button remBtn = new Button("-");
        remBtn.getStyleClass().add("button");
        remBtn.setStyle("-fx-background-color: #ef4444;");
        remBtn.setOnAction(e -> {
            String sel = nameList.getSelectionModel().getSelectedItem();
            if (sel != null && playerNames.size() > 2) {
                playerNames.remove(sel);
                nameList.getItems().remove(sel);
            }
        });
        editBox.getChildren().addAll(nameInput, addBtn, remBtn);

        Button backBtn = new Button("SAVE & BACK");
        backBtn.getStyleClass().add("button");
        backBtn.setOnAction(e -> {
            try {
                initialChips = Integer.parseInt(chipField.getText());
            } catch (Exception ex) { initialChips = 1000; }
            showHome();
        });

        settingsScreen.getChildren().addAll(title, chipBox, nameList, editBox, backBtn);
        mainRoot.getChildren().add(settingsScreen);
    }

    private void initGameScreen() {
        gameScreen = new BorderPane();
        gameScreen.setVisible(false);

        VBox topBox = new VBox(seatsContainer);
        topBox.setAlignment(Pos.CENTER);
        topBox.setPadding(new Insets(20, 0, 0, 0));
        gameScreen.setTop(topBox);

        StackPane tableFelt = new StackPane();
        tableFelt.getStyleClass().add("poker-table");
        tableFelt.setMaxSize(900, 350);

        VBox centerContent = new VBox(15);
        centerContent.setAlignment(Pos.CENTER);
        centerContent.getChildren().addAll(potLabel, communityCardsBox, phaseLabel);

        tableFelt.getChildren().add(centerContent);
        gameScreen.setCenter(tableFelt);

        VBox bottomBox = new VBox(15);
        bottomBox.setPadding(new Insets(15));

        bottomBox.getChildren().addAll(controlsBox, gameLog);
        gameScreen.setBottom(bottomBox);

        mainRoot.getChildren().addAll(gameScreen, privacyOverlay, resultsOverlay, winnerOverlay);
    }

    private void fadeIn(VBox node) {
        node.setVisible(true);
        FadeTransition ft = new FadeTransition(Duration.millis(300), node);
        ft.setFromValue(0.0);
        ft.setToValue(1.0);
        ft.play();
    }

    private void fadeOut(VBox node, Runnable onFinished) {
        FadeTransition ft = new FadeTransition(Duration.millis(200), node);
        ft.setFromValue(1.0);
        ft.setToValue(0.0);
        ft.setOnFinished(e -> {
            node.setVisible(false);
            if(onFinished != null) onFinished.run();
        });
        ft.play();
    }

    public void showHome() {
        homeScreen.setVisible(true);
        settingsScreen.setVisible(false);
        gameScreen.setVisible(false);
    }

    public void showSettings() {
        homeScreen.setVisible(false);
        fadeIn(settingsScreen);
    }

    private void showModeSelection() {
        VBox modeSelect = new VBox(20);
        modeSelect.setAlignment(Pos.CENTER);
        modeSelect.setStyle("-fx-background-color: rgba(0,0,0,0.9);");

        Label lbl = new Label("SELECT MODE");
        lbl.getStyleClass().add("header-text");

        Button btnHoldem = new Button("Texas Hold'em");
        btnHoldem.getStyleClass().add("button");
        btnHoldem.setPrefWidth(250);
        btnHoldem.setOnAction(e -> launchGame("HOLDEM", modeSelect));

        Button btnOmaha = new Button("Omaha");
        btnOmaha.getStyleClass().add("button");
        btnOmaha.setPrefWidth(250);
        btnOmaha.setOnAction(e -> launchGame("OMAHA", modeSelect));

        Button btnDraw = new Button("Five Card Draw");
        btnDraw.getStyleClass().add("button");
        btnDraw.setPrefWidth(250);
        btnDraw.setOnAction(e -> launchGame("DRAW", modeSelect));

        Button btnCancel = new Button("Cancel");
        btnCancel.getStyleClass().add("button");
        btnCancel.setStyle("-fx-background-color: transparent; -fx-text-fill: #71717a;");
        btnCancel.setOnAction(e -> mainRoot.getChildren().remove(modeSelect));

        modeSelect.getChildren().addAll(lbl, btnHoldem, btnOmaha, btnDraw, btnCancel);
        mainRoot.getChildren().add(modeSelect);
        fadeIn(modeSelect);
    }

    private void launchGame(String mode, VBox overlay) {
        mainRoot.getChildren().remove(overlay);
        homeScreen.setVisible(false);
        gameScreen.setVisible(true);

        List<Player> gamePlayers = new ArrayList<>();
        int id = 1;
        for (String name : playerNames) {
            gamePlayers.add(new Player(String.valueOf(id++), name, initialChips));
        }

        if (onGameStartRequest != null) {
            onGameStartRequest.accept(mode, gamePlayers);
        }
    }

    public void setOnGameStartRequest(BiConsumer<String, List<Player>> listener) {
        this.onGameStartRequest = listener;
    }

    public Scene createScene() {
        return new Scene(mainRoot, 1280, 800);
    }

    public void initializeSeats(List<Player> players) {
        seatsContainer.getChildren().clear();
        playerSeats.clear();

        for (Player p : players) {
            VBox seat = new VBox(4);
            seat.setAlignment(Pos.CENTER);
            seat.getStyleClass().add("player-seat");

            Label nameLbl = new Label(p.getName());
            nameLbl.setTextFill(Color.WHITE);
            nameLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));

            Label chipsLbl = new Label("$" + p.getChipStack());
            chipsLbl.setTextFill(Color.web("#fbbf24"));
            chipsLbl.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));

            HBox cardsBox = new HBox(-25);
            cardsBox.setAlignment(Pos.CENTER);
            cardsBox.setMinHeight(70);

            seat.getChildren().addAll(nameLbl, chipsLbl, cardsBox);
            seatsContainer.getChildren().add(seat);
            playerSeats.put(p, seat);
        }
    }

    public void updatePhase(String phase) {
        phaseLabel.setText(phase.toUpperCase());
        log(">>> " + phase.toUpperCase() + " <<<");
    }

    public void updatePot(int total) {
        potLabel.setText("POT: $" + total);
    }

    /**
     * Updates the community cards display.
     * <p>
     * # Optimization: Diff-based update to prevent UI flickering.
     * Only adds new cards instead of clearing and redrawing all.
     *
     * @param cards The list of community cards.
     */
    public void updateCommunityCards(List<Card> cards) {
        if (cards == null) return;

        int currentCount = communityCardsBox.getChildren().size();

        // Case 1: New Hand (fewer cards than displayed) -> Clear all
        if (cards.size() < currentCount) {
            communityCardsBox.getChildren().clear();
            currentCount = 0;
        }

        // Case 2: Append new cards only
        if (cards.size() > currentCount) {
            communityCardsBox.setSpacing(5);
            for (int i = currentCount; i < cards.size(); i++) {
                Card c = cards.get(i);
                CardView cv = new CardView(c);
                communityCardsBox.getChildren().add(cv);

                FadeTransition ft = new FadeTransition(Duration.millis(400), cv);
                ft.setFromValue(0);
                ft.setToValue(1);
                ft.play();
            }
        }
    }

    public void updatePlayerState(Player p) {
        VBox seat = playerSeats.get(p);
        if (seat == null) return;

        Label chipsLbl = (Label) seat.getChildren().get(1);
        chipsLbl.setText("$" + p.getChipStack());

        seat.getStyleClass().removeAll("player-seat-active", "player-seat-folded");

        if (p == currentActor) {
            seat.getStyleClass().add("player-seat-active");
        } else if (p.isFolded()) {
            seat.getStyleClass().add("player-seat-folded");
        }
    }

    public void dealHoleCards(Player p) {
        VBox seat = playerSeats.get(p);
        if (seat != null) {
            HBox cardsBox = (HBox) seat.getChildren().get(2);
            cardsBox.getChildren().clear();
            for (int i=0; i<p.getHoleCards().size(); i++) {
                cardsBox.getChildren().add(new CardView(null));
            }
        }
    }

    public void log(String message) {
        gameLog.appendText(message + "\n");
    }

    /**
     * Helper to cancel any pending futures to prevent zombies/leaks.
     * <p>
     * # Reliability: Ensures clean state transitions.
     */
    private void cancelActivePrompts() {
        if (pendingActionFuture != null && !pendingActionFuture.isDone()) {
            pendingActionFuture.cancel(true);
            pendingActionFuture = null;
        }
        if (pendingDiscardFuture != null && !pendingDiscardFuture.isDone()) {
            pendingDiscardFuture.cancel(true);
            pendingDiscardFuture = null;
        }
        // Force close overlays just in case
        controlsBox.setVisible(false);
        privacyOverlay.setVisible(false);
        gameScreen.setEffect(null);
    }

    public void showRoundResults(Map<Player, HandRank> showdowns, Map<Player, Integer> winnings, List<Card> communityCards, Runnable onNext) {
        // Cancel inputs if round ended unexpectedly
        cancelActivePrompts();

        resultsOverlay.getChildren().clear();
        fadeIn(resultsOverlay);

        Label title = new Label("ROUND RESULTS");
        title.getStyleClass().add("header-text");

        List<CardView> boardViews = new ArrayList<>();

        HBox boardBox = new HBox(5);
        boardBox.setAlignment(Pos.CENTER);

        // # Robustness: Null check for community cards (e.g. pre-flop win)
        List<Card> safeCommunityCards = communityCards != null ? communityCards : Collections.emptyList();

        for (Card c : safeCommunityCards) {
            CardView cv = new CardView(c);
            boardBox.getChildren().add(cv);
            boardViews.add(cv);
        }

        resultsOverlay.getChildren().addAll(title, boardBox);

        GridPane grid = new GridPane();
        grid.setHgap(30);
        grid.setVgap(15);
        grid.setAlignment(Pos.CENTER);

        int row = 0;

        Set<Player> players = new HashSet<>();
        if (showdowns != null) players.addAll(showdowns.keySet());
        if (winnings != null) players.addAll(winnings.keySet());

        for (Player p : players) {
            Label nameLbl = new Label(p.getName());
            nameLbl.setStyle("-fx-text-fill: white; -fx-font-size: 18px;");

            List<Card> winningCards = new ArrayList<>();
            // # Robustness: Null check for winnings map
            int won = (winnings != null) ? winnings.getOrDefault(p, 0) : 0;

            if (won > 0 && showdowns != null && showdowns.containsKey(p)) {
                winningCards = showdowns.get(p).getBestFive();
            }

            String rankStr;
            String descStr = "";
            if (showdowns != null && showdowns.containsKey(p)) {
                HandRank rank = showdowns.get(p);
                rankStr = rank.getRankType().toString();
                descStr = formatHandDescription(rank);
            } else if (p.isFolded()) {
                rankStr = "Folded";
            } else {
                rankStr = "Mucked";
            }
            VBox rankBox = new VBox(2);
            Label rankTypeLbl = new Label(rankStr);
            rankTypeLbl.setStyle("-fx-text-fill: #aaa; -fx-font-style: italic;");
            rankBox.getChildren().add(rankTypeLbl);
            if (!descStr.isEmpty()) {
                Label descLbl = new Label(descStr);
                descLbl.setStyle("-fx-text-fill: #f1c40f; -fx-font-size: 12px;");
                rankBox.getChildren().add(descLbl);
            }

            Label winLbl = new Label(won > 0 ? "+$" + won : "");
            winLbl.setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold; -fx-font-size: 18px;");

            HBox cardsBox = new HBox(5);
            if (!p.isFolded()) {
                for(Card c : p.getHoleCards()) {
                    CardView cv = new CardView(c);
                    cardsBox.getChildren().add(cv);

                    if (containsCard(winningCards, c)) {
                        cv.setHighlight(true);
                    }
                }
            } else {
                cardsBox.getChildren().add(new Label("(Folded)"));
                cardsBox.getChildren().get(0).setStyle("-fx-text-fill: #555;");
            }

            if (won > 0) {
                for(int i=0; i<safeCommunityCards.size(); i++) {
                    Card c = safeCommunityCards.get(i);
                    // Safe access to boardViews using i, assuming sync
                    if (i < boardViews.size() && containsCard(winningCards, c)) {
                        boardViews.get(i).setHighlight(true);
                    }
                }
            }

            grid.add(nameLbl, 0, row);
            grid.add(cardsBox, 1, row);
            grid.add(rankBox, 2, row);
            grid.add(winLbl, 3, row);
            row++;
        }

        resultsOverlay.getChildren().add(grid);

        Button nextBtn = new Button("NEXT HAND");
        nextBtn.getStyleClass().add("button");
        nextBtn.setOnAction(e -> {
            resultsOverlay.setVisible(false);
            onNext.run();
        });
        resultsOverlay.getChildren().add(nextBtn);
    }

    public void showGameWinner(Player winner) {
        cancelActivePrompts(); // Clear any pending UI state
        winnerOverlay.getChildren().clear();

        Label title = new Label("VICTORY");
        title.getStyleClass().add("header-text");
        title.setStyle("-fx-font-size: 56px; -fx-text-fill: #fbbf24; -fx-effect: dropshadow(gaussian, #fbbf24, 20, 0.5, 0, 0);");

        Label name = new Label(winner.getName());
        name.setStyle("-fx-font-size: 32px; -fx-text-fill: white; -fx-font-weight: bold;");

        Label sub = new Label("Final Stack: $" + winner.getChipStack());
        sub.setStyle("-fx-text-fill: #a1a1aa; -fx-font-size: 18px;");

        Button homeBtn = new Button("RETURN TO LOBBY");
        homeBtn.getStyleClass().add("button");
        homeBtn.setPrefWidth(250);
        homeBtn.setOnAction(e -> {
            fadeOut(winnerOverlay, this::showHome);
        });

        winnerOverlay.getChildren().addAll(title, name, sub, homeBtn);
        fadeIn(winnerOverlay);
    }

    public CompletableFuture<PlayerAction> promptUserForAction(Player player, GameContext context, List<ActionType> legal) {
        // # Robustness: Cancel old futures so they don't hang indefinitely
        cancelActivePrompts();

        CompletableFuture<PlayerAction> future = new CompletableFuture<>();

        Platform.runLater(() -> {
            this.currentActor = player;
            this.pendingActionFuture = future;
            updatePlayerState(player);

            showPrivacyScreen(player, () -> revealActionUI(player, context, legal));
        });

        return future;
    }

    private void showPrivacyScreen(Player player, Runnable onConfirm) {
        privacyOverlay.getChildren().clear();
        fadeIn(privacyOverlay);

        gameScreen.setEffect(new GaussianBlur(10));

        Label turnLbl = new Label(player.getName() + "'s Turn");
        turnLbl.getStyleClass().add("header-text");

        Label instrLbl = new Label("Please pass the device to " + player.getName());
        instrLbl.getStyleClass().add("sub-header");

        Button btnIdentify = new Button("I am " + player.getName());
        btnIdentify.getStyleClass().add("button");
        btnIdentify.setOnAction(e -> {
            privacyOverlay.getChildren().remove(btnIdentify);

            Button btnReveal = new Button("REVEAL HAND");
            btnReveal.getStyleClass().add("button");
            btnReveal.setStyle("-fx-background-color: #e74c3c;");
            btnReveal.setOnAction(ev -> {
                privacyOverlay.setVisible(false);
                gameScreen.setEffect(null);
                onConfirm.run();
            });

            privacyOverlay.getChildren().add(btnReveal);
        });

        privacyOverlay.getChildren().addAll(turnLbl, instrLbl, btnIdentify);
    }

    private void revealActionUI(Player player, GameContext context, List<ActionType> legal) {
        controlsBox.getChildren().clear();

        HandRank rank = evaluator.evaluate(player.getHoleCards(), context.getCommunityCards());
        Label rankLabel = new Label(rank.toString());
        rankLabel.setStyle("-fx-text-fill: #fbbf24; -fx-font-size: 18px; -fx-font-weight: 800;");

        HBox myCards = new HBox(10);
        myCards.setAlignment(Pos.CENTER);
        for(Card c : player.getHoleCards()) myCards.getChildren().add(new CardView(c));

        HBox btnContainer = new HBox(15);
        btnContainer.setAlignment(Pos.CENTER);

        // Feedback label for errors
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 14px;");

        VBox layout = new VBox(15);
        layout.setAlignment(Pos.CENTER);

        for (ActionType type : legal) {
            if (type == ActionType.BET || type == ActionType.RAISE) {
                VBox actionGroup = new VBox(5);
                actionGroup.setAlignment(Pos.CENTER);

                TextField amt = new TextField();
                amt.setPromptText("Amt");
                amt.setPrefWidth(80);
                amt.setAlignment(Pos.CENTER);

                // Allow submitting with Enter key
                amt.setOnAction(e -> handleActionSubmit(player, type, amt.getText(), errorLabel));

                Button btn = new Button(type.name());
                btn.getStyleClass().addAll("button", "action-button-raise");
                btn.setOnAction(e -> handleActionSubmit(player, type, amt.getText(), errorLabel));

                actionGroup.getChildren().addAll(btn, amt);
                btnContainer.getChildren().add(actionGroup);
            } else {
                Button btn = new Button(type.name());
                btn.getStyleClass().add("button");
                if (type == ActionType.FOLD) btn.getStyleClass().add("action-button-fold");
                if (type == ActionType.CHECK || type == ActionType.CALL) btn.getStyleClass().add("action-button-check");

                btn.setOnAction(e -> handleActionSubmit(player, type, "0", errorLabel));
                btnContainer.getChildren().add(btn);
            }
        }

        layout.getChildren().addAll(rankLabel, myCards, btnContainer, errorLabel);
        controlsBox.getChildren().add(layout);
        fadeIn(controlsBox);
    }

    /**
     * Handles action submission with input validation.
     * <p>
     * # Robustness: Validates input BEFORE resolving the future.
     * Does not close the UI if input is invalid.
     */
    private void handleActionSubmit(Player player, ActionType type, String amountStr, Label errorLabel) {
        try {
            int amount = 0;
            if (!amountStr.isEmpty()) amount = Integer.parseInt(amountStr);
            if (type == ActionType.ALL_IN) amount = player.getChipStack();

            // Validation Logic
            if (amount < 0) {
                errorLabel.setText("Cannot bet negative amount!");
                return;
            }
            if ((type == ActionType.BET || type == ActionType.RAISE) && amount > player.getChipStack()) {
                errorLabel.setText("Not enough chips!");
                return;
            }
            if ((type == ActionType.BET || type == ActionType.RAISE) && amount == 0) {
                errorLabel.setText("Amount required!");
                return;
            }

            // Valid input - Proceed
            PlayerAction action = new PlayerAction(player, type, amount);

            fadeOut(controlsBox, () -> {
                this.currentActor = null;
                updatePlayerState(player);
                dealHoleCards(player);
                if (pendingActionFuture != null) pendingActionFuture.complete(action);
            });

        } catch (NumberFormatException e) {
            // # UX: User feedback instead of silent failure
            errorLabel.setText("Invalid number format!");
        }
    }

    public CompletableFuture<List<Card>> promptUserForDiscard(Player player, GameContext context) {
        cancelActivePrompts(); // Clear previous inputs

        CompletableFuture<List<Card>> future = new CompletableFuture<>();
        Platform.runLater(() -> {
            this.currentActor = player;
            this.pendingDiscardFuture = future;
            this.selectedDiscards.clear();
            updatePlayerState(player);

            showPrivacyScreen(player, () -> {
                controlsBox.getChildren().clear();

                Label prompt = new Label("TAP CARDS TO DISCARD");
                prompt.getStyleClass().add("header-text");
                prompt.setStyle("-fx-font-size: 20px;");

                HBox cardRow = new HBox(15);
                cardRow.setAlignment(Pos.CENTER);

                Button confirmBtn = new Button("KEEP ALL");
                confirmBtn.getStyleClass().add("button");
                confirmBtn.setOnAction(e -> handleDiscardSubmit(player));

                for (Card c : player.getHoleCards()) {
                    CardView cv = new CardView(c);
                    cv.setOnMouseClicked(ev -> {
                        toggleDiscard(c, cv);
                        confirmBtn.setText(selectedDiscards.isEmpty() ? "KEEP ALL" : "DISCARD ("+selectedDiscards.size()+")");
                    });
                    cardRow.getChildren().add(cv);
                }

                controlsBox.getChildren().addAll(prompt, cardRow, confirmBtn);
                fadeIn(controlsBox);
            });
        });
        return future;
    }

    private void toggleDiscard(Card c, CardView cv) {
        if (selectedDiscards.contains(c)) {
            selectedDiscards.remove(c);
            cv.setTranslateY(0);
            cv.setEffect(new javafx.scene.effect.DropShadow(5, Color.BLACK));
        } else {
            selectedDiscards.add(c);
            cv.setTranslateY(-20);
            ColorAdjust dim = new ColorAdjust();
            dim.setBrightness(-0.3);
            cv.setEffect(dim);
        }
    }

    private void handleDiscardSubmit(Player player) {
        fadeOut(controlsBox, () -> {
            this.currentActor = null;
            dealHoleCards(player);
            if (pendingDiscardFuture != null) pendingDiscardFuture.complete(new ArrayList<>(selectedDiscards));
        });
    }

    private boolean containsCard(List<Card> list, Card target) {
        if (list == null) return false;
        for (Card c : list) {
            if (c.equals(target)) return true;
        }
        return false;
    }

    private String formatHandDescription(HandRank rank) {
        List<Rank> k = rank.getKickers();
        if (k.isEmpty()) return "";

        return switch (rank.getRankType()) {
            case HIGH_CARD -> "High Card: " + k.get(0);
            case PAIR -> "Pair of " + pluralize(k.get(0));
            case TWO_PAIR -> "Two Pair: " + pluralize(k.get(0)) + " & " + pluralize(k.get(1));
            case THREE_OF_A_KIND -> "Three of a Kind: " + pluralize(k.get(0));
            case STRAIGHT -> k.get(0) + "-High Straight";
            case FLUSH -> k.get(0) + "-High Flush";
            case FULL_HOUSE -> pluralize(k.get(0)) + " full of " + pluralize(k.get(1));
            case FOUR_OF_A_KIND -> "Four of a Kind: " + pluralize(k.get(0));
            case STRAIGHT_FLUSH -> k.get(0) + "-High Straight Flush";
            case ROYAL_FLUSH -> "Royal Flush";
            default -> "";
        };
    }

    private String pluralize(Rank rank) {
        return rank.toString() + "s";
    }
}