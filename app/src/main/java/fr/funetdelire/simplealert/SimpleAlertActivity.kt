package fr.funetdelire.simplealert

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import fr.funetdelire.simplealert.service.SimpleAlertService

class SimpleAlertActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val serviceIntent = Intent(this, SimpleAlertService::class.java)
        startService(serviceIntent)
    }
}