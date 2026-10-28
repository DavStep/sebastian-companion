package dev.sebastian.companion;

import org.junit.Test;
import static org.junit.Assert.*;

public class CompanionContractTest {
    @Test public void allowsOnlyPrivateChatgptConversationUrls() {
        assertTrue(ConversationLink.isAllowed("https://chatgpt.com/c/01234567-abcd-4321-abcd-0123456789ab"));
        for(String value : new String[]{"http://chatgpt.com/c/12345678", "https://chatgpt.com.evil.test/c/12345678", "https://evil.test@chatgpt.com/c/12345678", "https://chatgpt.com:443/c/12345678", "https://chatgpt.com/share/12345678", "https://chatgpt.com/c/12345678?token=private", "https://chatgpt.com/c/12345678#x", "javascript:alert(1)", "https://chatgpt.com/c/%2e%2e", "", null}) assertFalse(value, ConversationLink.isAllowed(value));
    }
    @Test public void freshnessRejectsExpiredAndFutureStatus() {
        StatusSnapshot snapshot=new StatusSnapshot(CompanionState.THINKING,"Working",1000000);
        assertTrue(snapshot.isFresh(1000000));assertTrue(snapshot.isFresh(1299999));
        assertFalse(snapshot.isFresh(1300000));assertFalse(snapshot.isFresh(999999));
    }
    @Test public void invalidSnapshotNeverBecomesStatus() {
        for(Runnable invalid:new Runnable[]{()->new StatusSnapshot(null,"",1),()->new StatusSnapshot(CompanionState.IDLE,null,1),()->new StatusSnapshot(CompanionState.IDLE,"x".repeat(281),1),()->new StatusSnapshot(CompanionState.IDLE,"",0)}) {
            try {invalid.run();fail("Expected validation failure");}catch(IllegalArgumentException expected){}
        }
    }
}
