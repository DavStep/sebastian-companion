package dev.sebastian.companion;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONException;
import org.json.JSONObject;

/** Explicitly imported local snapshots. This is not a live ChatGPT connection. */
public final class SnapshotStore implements StatusSource {
    private final SharedPreferences prefs;
    public SnapshotStore(Context context) { prefs = context.getSharedPreferences("companion", Context.MODE_PRIVATE); }
    public static StatusSnapshot parse(String input) throws JSONException {
        JSONObject j = new JSONObject(input);
        if (j.getInt("schemaVersion") != 1) throw new IllegalArgumentException("Unsupported schema");
        return new StatusSnapshot(CompanionState.valueOf(j.getString("state")), j.getString("message"), j.getLong("updatedAt"));
    }
    public void importJson(String input) throws JSONException {
        StatusSnapshot snapshot = parse(input);
        if (!snapshot.isFresh(System.currentTimeMillis())) throw new IllegalArgumentException("Snapshot must be from the past five minutes");
        prefs.edit().putString("snapshot", input).apply();
    }
    @Override public StatusSnapshot latest() {
        try { String data = prefs.getString("snapshot", ""); return data.isEmpty() ? null : parse(data); }
        catch (JSONException | IllegalArgumentException e) { return null; }
    }
    @Override public String description() { return "Imported snapshot · manual"; }
    public void clear() { prefs.edit().remove("snapshot").apply(); }
}
