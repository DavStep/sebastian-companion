package dev.sebastian.companion;

/** Transport-neutral boundary: no account, model, or desktop session access. */
public final class StatusSnapshot {
    public static final long MAX_AGE_MS = 5 * 60 * 1000;
    public final CompanionState state;
    public final String message;
    public final long updatedAt;
    public StatusSnapshot(CompanionState state, String message, long updatedAt) {
        if (state == null || message == null || message.length() > 280 || updatedAt <= 0)
            throw new IllegalArgumentException("Invalid status snapshot");
        this.state = state; this.message = message; this.updatedAt = updatedAt;
    }
    public boolean isFresh(long now) { return now >= updatedAt && now - updatedAt < MAX_AGE_MS; }
}
