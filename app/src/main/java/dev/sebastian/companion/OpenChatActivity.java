package dev.sebastian.companion;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

public final class OpenChatActivity extends Activity {
    @Override public void onCreate(Bundle state) { super.onCreate(state); open(); finish(); }
    private void open() {
        String link = getSharedPreferences("companion", MODE_PRIVATE).getString("conversation", "");
        Intent target;
        if (ConversationLink.isAllowed(link)) {
            // Android's verified App Links decide whether ChatGPT or the browser handles it.
            target = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
        } else {
            target = getPackageManager().getLaunchIntentForPackage("com.openai.chatgpt");
            if (target == null) target = new Intent(Intent.ACTION_VIEW, Uri.parse("https://chatgpt.com/"));
        }
        try { startActivity(target); }
        catch (android.content.ActivityNotFoundException e) { Toast.makeText(this, "Install ChatGPT or a browser to open chat.", Toast.LENGTH_LONG).show(); }
    }
}
