package fr.funetdelire.simplealert

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class AlertPreferences(private val dataStore : DataStore<Preferences>) {

    suspend fun getTime(): Int {
        return dataStore.data.map {
            it[TIME_KEY] ?: 10
        }.first()
    }

    suspend fun setTime(time: Int) {
        dataStore.edit {
            it[TIME_KEY] = time
        }
    }

    suspend fun getServer(): String {
        return dataStore.data.map {
            it[SERVER_KEY] ?: "localhost"
        }.first()
    }

    suspend fun setServer(server: String) {
        dataStore.edit {
            it[SERVER_KEY] = server
        }
    }

    companion object {
        val TIME_KEY = intPreferencesKey("update_time")
        val SERVER_KEY = stringPreferencesKey("alert_server")

        @Volatile
        private var instance: AlertPreferences? = null

        fun getInstance(context : Context) =
            instance ?: synchronized(this) {
                instance ?: AlertPreferences(
                    PreferenceDataStoreFactory.create { context.preferencesDataStoreFile("settings") }
                ).also { instance = it }
            }
    }


}