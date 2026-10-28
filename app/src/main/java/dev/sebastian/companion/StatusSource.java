package dev.sebastian.companion;

/** Future supported backend adapters should implement this boundary. Null means disconnected. */
public interface StatusSource {
    StatusSnapshot latest();
    String description();
}
