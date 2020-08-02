package io.github.spleefx.arena.api;

/**
 * Represents a game task
 */
public abstract class GameTask implements Runnable {

    /**
     * Represents the task's phase
     */
    public enum Phase {
        BEFORE,
        AFTER
    }

    /**
     * The task's phase
     */
    private final Phase phase;

    /**
     * Creates a new task
     *
     * @param phase Phase of the task
     */
    public GameTask(Phase phase) {
        this.phase = phase;
    }

    /**
     * Returns the task's phase
     *
     * @return The phase
     */
    public Phase getPhase() {
        return phase;
    }
}