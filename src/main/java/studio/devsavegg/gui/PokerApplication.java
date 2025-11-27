package studio.devsavegg.gui;

import javafx.application.Application;
import javafx.stage.Stage;
import studio.devsavegg.core.Player;
import studio.devsavegg.game.GameEngine;
import studio.devsavegg.game.IGameMode;
import studio.devsavegg.modes.FiveCardDraw;
import studio.devsavegg.modes.Omaha;
import studio.devsavegg.modes.TexasHoldemMode;
import studio.devsavegg.services.*;

import java.util.List;
import javafx.scene.image.Image;

public class PokerApplication extends Application {
    private GameEngine gameEngine;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        PokerTableController controller = new PokerTableController();

        controller.setOnGameStartRequest((modeId, players) -> {
            startGame(modeId, players, controller);
        });

        primaryStage.setTitle("Poker Midnight - Casino Edition");

        java.net.URL iconUrl = getClass().getResource("/icon.png");
        if (iconUrl != null) {
            primaryStage.getIcons().add(new Image(iconUrl.toExternalForm()));
        } else {
            System.out.println("Warning: icon.png not found.");
        }

        primaryStage.setScene(controller.createScene());
        primaryStage.show();
    }

    private void startGame(String modeId, List<Player> players, PokerTableController controller) {
        IGameMode gameMode;
        switch (modeId) {
            case "OMAHA":
                gameMode = new Omaha();
                break;
            case "DRAW":
                gameMode = new FiveCardDraw();
                break;
            case "HOLDEM":
            default:
                gameMode = new TexasHoldemMode(5, 10);
                break;
        }

        IPotManager potManager = new DefaultPotManager();
        IGameStateService stateService = null;
        GuiPlayerInput input = new GuiPlayerInput(controller);
        GuiGameObserver observer = new GuiGameObserver(controller);

        gameEngine = new GameEngine(gameMode, potManager, stateService, input, players);
        gameEngine.registerObserver(observer);

        Thread gameThread = new Thread(() -> {
            try {
                Thread.sleep(500);
                gameEngine.startGame();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        gameThread.setDaemon(true);
        gameThread.start();
    }
}