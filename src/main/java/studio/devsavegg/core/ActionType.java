package studio.devsavegg.core;

/**
 * Represents the specific actions a player can take during their turn.
 * <p>
 * # Principle - Single Responsibility Principle (SRP): This enum defines the valid state transitions for player turns.
 */
public enum ActionType {
    FOLD,
    CHECK,
    CALL,
    BET,
    RAISE,
    ALL_IN
}