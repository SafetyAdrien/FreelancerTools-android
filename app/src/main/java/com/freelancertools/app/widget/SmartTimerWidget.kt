package com.freelancertools.app.widget

import android.content.Context
import android.content.Intent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.freelancertools.app.MainActivity
import com.freelancertools.app.ui.tools.smarttimer.TimerEngine
import com.freelancertools.app.ui.tools.smarttimer.TimerForegroundService
import java.text.NumberFormat
import java.util.Locale

/** Home screen widget mirroring the Smart Timer: elapsed time, earnings, and a play/pause toggle. */
class SmartTimerWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val state = TimerEngine.state.value
            val minutes = state.elapsedSeconds / 60
            val seconds = state.elapsedSeconds % 60
            val earned = NumberFormat.getCurrencyInstance(Locale.FRANCE).format(state.earned)

            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(Color(0xFF1A1A1A))
                    .padding(16.dp)
                    .clickable(actionStartActivity<MainActivity>()),
            ) {
                Text(
                    text = "Smart Timer",
                    style = TextStyle(color = ColorProvider(Color.White), fontWeight = FontWeight.Bold, fontSize = 14.sp),
                )
                Text(
                    text = "%02d:%02d".format(minutes, seconds),
                    style = TextStyle(color = ColorProvider(Color(0xFFFF3B5C)), fontWeight = FontWeight.Bold, fontSize = 28.sp),
                )
                Text(
                    text = earned,
                    style = TextStyle(color = ColorProvider(Color(0xFF2ECC71)), fontSize = 14.sp),
                )
                Row(
                    modifier = GlanceModifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = if (state.isRunning) "❚❚ Pause" else "▶ Reprendre",
                        style = TextStyle(color = ColorProvider(Color.White), fontSize = 13.sp),
                        modifier = GlanceModifier.clickable(actionRunCallback<ToggleTimerAction>()),
                    )
                }
            }
        }
    }
}

class ToggleTimerAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val running = TimerEngine.state.value.isRunning
        val action = if (running) TimerForegroundService.ACTION_PAUSE else TimerForegroundService.ACTION_START
        val intent = Intent(context, TimerForegroundService::class.java).setAction(action)
        ContextCompat.startForegroundService(context, intent)
        SmartTimerWidget().updateAll(context)
    }
}
