package dev.sebastian.companion;

import java.net.URI;
import java.net.URISyntaxException;

public final class ConversationLink {
    private ConversationLink() {}
    public static boolean isAllowed(String value) {
        try {
            URI u = new URI(value);
            return "https".equals(u.getScheme()) && "chatgpt.com".equals(u.getHost())
                && u.getRawUserInfo() == null && u.getPort() == -1
                && u.getRawQuery() == null && u.getRawFragment() == null
                && u.getRawPath().matches("/c/[A-Za-z0-9-]{8,100}");
        } catch (URISyntaxException | NullPointerException e) { return false; }
    }
}
