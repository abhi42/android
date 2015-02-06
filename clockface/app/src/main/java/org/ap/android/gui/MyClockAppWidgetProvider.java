package org.ap.android.gui;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.text.format.Time;
import android.widget.ImageView;
import android.widget.RemoteViews;

/**
 * Created by abhi on 01.08.14.
 */
public class MyClockAppWidgetProvider extends AppWidgetProvider {

    private RemoteViews views;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        final int n = appWidgetIds.length;
        for(int i = 0; i < n; i++) {
            final int appWidgetId = appWidgetIds[i];
            updateWidget(context, appWidgetManager, appWidgetId);
        }
    }

    private void updateWidget(final Context context,
                              final AppWidgetManager appWidgetManager,
                              final int appWidgetId) {

//        final RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.myclock_appwidget);
//        views.setImageViewResource(R.id.MyAnalogClock, R.drawable.analogclock);
//        // clicking on the views does nothing, so no need to provide a onClickPendingIntent
//
//        appWidgetManager.updateAppWidget(appWidgetId, views);

        views = new RemoteViews(context.getPackageName(), R.layout.myclock_appwidget);
        AppWidgetManager.getInstance(context).updateAppWidget(appWidgetId, views);
    }

//    @Override
//    public void onReceive(Context context, Intent intent) {
//        final String action = intent.getAction();
//        if (AppWidgetManager.ACTION_APPWIDGET_UPDATE.equals(action)) {
//            views = new RemoteViews(context.getPackageName(), R.layout.myclock_appwidget);
//            AppWidgetManager.getInstance(context).
//                    updateAppWidget(intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS), views);
//        }
//    }
}
