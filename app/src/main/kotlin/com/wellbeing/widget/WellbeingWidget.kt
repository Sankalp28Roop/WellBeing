package com.wellbeing.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import com.wellbeing.R

class WellbeingWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(context.packageName, R.layout.wellbeing_widget)
            // Just a placeholder update
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
