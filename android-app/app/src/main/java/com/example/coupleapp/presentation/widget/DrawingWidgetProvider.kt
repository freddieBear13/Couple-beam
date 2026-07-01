package com.example.coupleapp.presentation.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.view.View
import android.widget.RemoteViews
import com.example.coupleapp.MainActivity
import com.example.coupleapp.R
import java.io.File

class DrawingWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        const val LAST_DRAWING_FILE = "last_drawing.png"

        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_layout)

            val file = File(context.filesDir, LAST_DRAWING_FILE)
            if (file.exists()) {
                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                if (bitmap != null) {
                    views.setImageViewBitmap(R.id.widget_image, bitmap)
                    views.setViewVisibility(R.id.widget_image, View.VISIBLE)
                    views.setViewVisibility(R.id.widget_placeholder, View.GONE)
                } else {
                    showPlaceholder(views)
                }
            } else {
                showPlaceholder(views)
            }

            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_image, pendingIntent)
            views.setOnClickPendingIntent(R.id.widget_placeholder, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        private fun showPlaceholder(views: RemoteViews) {
            views.setViewVisibility(R.id.widget_image, View.GONE)
            views.setViewVisibility(R.id.widget_placeholder, View.VISIBLE)
        }

        fun requestUpdate(context: Context) {
            val intent = Intent(context, DrawingWidgetProvider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            }
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val ids = appWidgetManager.getAppWidgetIds(
                ComponentName(context, DrawingWidgetProvider::class.java)
            )
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            context.sendBroadcast(intent)
        }
    }
}