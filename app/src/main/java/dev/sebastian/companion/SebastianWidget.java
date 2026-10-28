package dev.sebastian.companion;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.widget.RemoteViews;
import java.text.DateFormat;
import java.util.Date;

public final class SebastianWidget extends AppWidgetProvider {
    public static void refresh(Context context) {
        AppWidgetManager m = AppWidgetManager.getInstance(context);
        new SebastianWidget().onUpdate(context, m, m.getAppWidgetIds(new ComponentName(context, SebastianWidget.class)));
    }
    @Override public void onUpdate(Context context, AppWidgetManager manager, int[] ids) {
        StatusSnapshot s = new SnapshotStore(context).latest();
        boolean fresh = s != null && s.isFresh(System.currentTimeMillis());
        CompanionState state = fresh ? s.state : CompanionState.IDLE;
        String label = s == null ? "Not connected" : "Snapshot · " + DateFormat.getTimeInstance(DateFormat.SHORT).format(new Date(s.updatedAt)) + "\n" + s.state.label;
        Bitmap image = Bitmap.createBitmap(320, 320, Bitmap.Config.ARGB_8888);
        new PetRenderer().draw(new Canvas(image), 320, 320, state, 0);
        for(int id : ids) {
            RemoteViews v = new RemoteViews(context.getPackageName(), R.layout.widget);
            v.setImageViewBitmap(R.id.widget_pet, image);
            v.setTextViewText(R.id.widget_status, label);
            v.setOnClickPendingIntent(R.id.widget_root, PendingIntent.getActivity(context, 0,
                new Intent(context, OpenChatActivity.class), PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT));
            manager.updateAppWidget(id, v);
        }
    }
}
