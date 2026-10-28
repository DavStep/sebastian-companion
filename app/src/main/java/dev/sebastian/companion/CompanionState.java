package dev.sebastian.companion;

public enum CompanionState {
    IDLE("Idle"), THINKING("Thinking"), SPEAKING("Speaking"), HAPPY("Happy"), RESTING("Resting");
    public final String label;
    CompanionState(String label) { this.label = label; }
}
