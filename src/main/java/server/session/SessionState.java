package server.session;

/**
 * Represents the current lifecycle state of a game session.
 */
public enum SessionState {
    /** Session object created but not yet started. */
    INITIALIZING,

    /** Actively running (moves are being processed). */
    RUNNING,

    /** Temporarily paused (e.g. due to a disconnect or pause request). */
    PAUSED,

    /** Finished normally (one player won or game drew). */
    COMPLETED,

    /** Terminated early (due to timeout, error, or all players leaving). */
    CANCELLED
}

