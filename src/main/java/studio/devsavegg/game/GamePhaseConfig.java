package studio.devsavegg.game;

/**
 * Defines the configuration for a specific phase of the game (e.g., "The Flop").
 * <p>
 * # Principle - Open/Closed Principle (OCP): Encapsulates phase behavior data, allowing new game modes without changing engine logic.
 */
public class GamePhaseConfig {
    public final String phaseName;
    public final int communityCardsToDeal;
    public final int holeCardsToDeal;
    public final boolean isBettingRound;
    public final boolean triggersShowdown;
    public final boolean isDrawRound;

    /**
     * Constructs a new GamePhaseConfig.
     * <p>
     * # Design - Data Transfer Object (DTO): Immutable configuration object.
     *
     * @param phaseName The display name of the phase.
     * @param communityCardsToDeal Number of community cards to deal.
     * @param holeCardsToDeal Number of hole cards to deal.
     * @param isBettingRound Whether a betting round occurs in this phase.
     * @param triggersShowdown Whether this phase triggers a showdown immediately.
     * @param isDrawRound Whether this phase allows drawing/discarding cards.
     */
    public GamePhaseConfig(String phaseName, int communityCardsToDeal, int holeCardsToDeal,
                           boolean isBettingRound, boolean triggersShowdown, boolean isDrawRound) {
        this.phaseName = phaseName;
        this.communityCardsToDeal = communityCardsToDeal;
        this.holeCardsToDeal = holeCardsToDeal;
        this.isBettingRound = isBettingRound;
        this.triggersShowdown = triggersShowdown;
        this.isDrawRound = isDrawRound;
    }
}