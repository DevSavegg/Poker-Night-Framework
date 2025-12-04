package studio.devsavegg;

import studio.devsavegg.gui.PokerApplication;

/**
 * Main entry point for the application.
 * <p>
 * Serves as a workaround for JavaFX modularity issues in some build environments (Fat JARs).
 * <p>
 * # Principle - Separation of Concerns: Separates the launcher from the JavaFX Application class.
 */
public class Main {
    /**
     * Bootstraps the JavaFX application.
     * <p>
     * # Design - Bootstrap / Entry Point: Static entry point.
     *
     * @param args Command line arguments.
     */
    public static void main(String[] args) {
        PokerApplication.main(args);
    }
}