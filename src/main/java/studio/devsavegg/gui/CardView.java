package studio.devsavegg.gui;

import javafx.scene.effect.DropShadow;
import javafx.scene.effect.InnerShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.control.Label;
import studio.devsavegg.core.Card;
import studio.devsavegg.core.Suit;

import java.io.File;

public class CardView extends StackPane {
    private static final double WIDTH = 80;
    private static final double HEIGHT = 112;

    private final InnerShadow highlightEffect;

    public CardView(Card card) {
        setPrefSize(WIDTH, HEIGHT);
        setEffect(new DropShadow(10, 0, 5, Color.color(0, 0, 0, 0.4)));

        if (card != null) {
            String imagePath = "img/Cards/" + card.getSuit().name() + "/" + card.getRank().name() + ".png";
            File imgFile = new File(imagePath);

            if (imgFile.exists()) {
                Image img = new Image("file:" + imagePath);
                ImageView imgView = new ImageView(img);
                imgView.setFitWidth(WIDTH);
                imgView.setFitHeight(HEIGHT);
                imgView.setPreserveRatio(true);
                getChildren().add(imgView);
            } else {
                renderFallback(card);
            }
        } else {
            renderBack();
        }

        highlightEffect = new InnerShadow();
        highlightEffect.setColor(Color.GOLD);
        highlightEffect.setChoke(0.5);
        highlightEffect.setRadius(20);
        highlightEffect.setWidth(20);
        highlightEffect.setHeight(20);
    }

    public void setHighlight(boolean active) {
        if (active) {
            setEffect(highlightEffect);
            setTranslateY(-10);
        } else {
            setEffect(new DropShadow(5, Color.BLACK));
            setTranslateY(0);
        }
    }

    private void renderBack() {
        Rectangle bg = new Rectangle(WIDTH, HEIGHT);
        bg.setArcWidth(12);
        bg.setArcHeight(12);
        bg.setFill(Color.web("#1e293b"));
        bg.setStroke(Color.web("#334155"));
        bg.setStrokeWidth(2);

        Label ptrn = new Label("♠");
        ptrn.setFont(Font.font("Segoe UI Symbol", 40));
        ptrn.setTextFill(Color.web("#334155"));

        getChildren().addAll(bg, ptrn);
    }

    private void renderFallback(Card card) {
        Rectangle bg = new Rectangle(WIDTH, HEIGHT);
        bg.setArcWidth(12);
        bg.setArcHeight(12);
        bg.setFill(Color.WHITE);
        bg.setStroke(Color.web("#e4e4e7"));
        bg.setStrokeWidth(1);

        Color suitColor = (card.getSuit() == Suit.HEARTS || card.getSuit() == Suit.DIAMONDS)
                ? Color.web("#ef4444")
                : Color.web("#18181b");

        String rankText = getRankSymbol(card);
        String suitText = getSuitSymbol(card.getSuit());

        Label label = new Label(rankText + "\n" + suitText);
        label.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        label.setTextFill(suitColor);
        label.setStyle("-fx-text-alignment: center;");

        getChildren().addAll(bg, label);
    }

    private String getRankSymbol(Card c) {
        return switch (c.getRank()) {
            case ACE -> "A";
            case KING -> "K";
            case QUEEN -> "Q";
            case JACK -> "J";
            case TEN -> "10";
            default -> String.valueOf(c.getRank().getValue());
        };
    }

    private String getSuitSymbol(Suit s) {
        return switch (s) {
            case HEARTS -> "♥";
            case DIAMONDS -> "♦";
            case CLUBS -> "♣";
            case SPADES -> "♠";
        };
    }
}