package dev.sebastian.companion;

import org.junit.Test;
import static org.junit.Assert.*;

public class SnapshotImportTest {
    @Test public void parsesAValidTransportSnapshot() throws Exception {
        StatusSnapshot s=SnapshotStore.parse("{\"schemaVersion\":1,\"state\":\"THINKING\",\"message\":\"Working\",\"updatedAt\":1000000}");
        assertEquals(CompanionState.THINKING,s.state);assertEquals("Working",s.message);assertEquals(1000000,s.updatedAt);
    }
    @Test public void rejectsUnknownSchema() { rejects("{\"schemaVersion\":2,\"state\":\"IDLE\",\"message\":\"\",\"updatedAt\":1}"); }
    @Test public void rejectsUnknownState() { rejects("{\"schemaVersion\":1,\"state\":\"REAL_SEBASTIAN\",\"message\":\"\",\"updatedAt\":1}"); }
    @Test public void rejectsBrokenOrMissingFields() { for(String value:new String[]{"not JSON","{}","[]","{\"schemaVersion\":1,\"state\":\"IDLE\"}"}) rejects(value); }
    private void rejects(String input) { try { SnapshotStore.parse(input);fail("Expected invalid snapshot"); }catch(org.json.JSONException|IllegalArgumentException expected){} }
}
