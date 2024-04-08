package fr.funetdelire.simplealert

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import fr.funetdelire.simplealert.service.SimpleAlertService
import kotlinx.coroutines.launch

class SimpleAlertActivity : ComponentActivity() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!SimpleAlertService.started) {
            val serviceIntent = Intent(this, SimpleAlertService::class.java)
            startForegroundService(serviceIntent)
        }

        val preferences = AlertPreferences.getInstance(applicationContext)

        var initialValue = 0f;
        lifecycleScope.launch {
            initialValue = preferences.getTime().toFloat()
        }

        setContent {
            Box(
                modifier = Modifier
                    .padding(40.dp)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                Options(initialValue) {
                    lifecycleScope.launch {
                        preferences.setTime(it.toInt())
                    }
                }
            }
        }
    }


    @Composable
    fun Options(initialValue : Float, onTimeUpdate : (Float) -> Unit) {
        var sliderPosition by remember { mutableFloatStateOf(initialValue) }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row (
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ){
                Text(
                    text = "Get Alert every " + sliderPosition.toInt() + " seconds",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                )
            }
            Slider(
                valueRange = 1f.rangeTo(60f),
                steps = 60,
                value = sliderPosition,
                onValueChange = {
                    sliderPosition = it
                    onTimeUpdate(it)
                }
            )
        }
    }
}