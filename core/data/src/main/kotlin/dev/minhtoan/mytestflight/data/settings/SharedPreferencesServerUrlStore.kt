package dev.minhtoan.mytestflight.data.settings

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SharedPreferencesServerUrlStore @Inject constructor(
    @ApplicationContext context: Context,
) : ServerUrlStore {

    private val preferences =
        context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE)

    override fun read(): String? = preferences.getString(KEY_SERVER_URL, null)

    override fun write(url: String) {
        preferences.edit { putString(KEY_SERVER_URL, url) }
    }

    override fun clear() {
        preferences.edit { remove(KEY_SERVER_URL) }
    }

    private companion object {
        const val PREFERENCES_FILE = "server_settings"
        const val KEY_SERVER_URL = "server_url"
    }
}
