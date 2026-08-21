package ru.scripchenko.autovless

import android.app.LocaleManager
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import java.util.Locale

object AppLocale {

    private const val PREFS_NAME =
        "general_settings"

    private const val LANGUAGE_KEY =
        "language"

    const val ENGLISH =
        "en"

    const val RUSSIAN =
        "ru"

    fun currentLanguage(
        context: Context
    ): String =
        context
            .getSharedPreferences(
                PREFS_NAME,
                Context.MODE_PRIVATE
            )
            .getString(
                LANGUAGE_KEY,
                ENGLISH
            )
            ?: ENGLISH

    fun saveLanguage(
        context: Context,
        language: String
    ) {
        context
            .getSharedPreferences(
                PREFS_NAME,
                Context.MODE_PRIVATE
            )
            .edit()
            .putString(
                LANGUAGE_KEY,
                language
            )
            .apply()

        applyPlatformLocale(
            context = context,
            language = language
        )
    }

    fun wrap(
        context: Context
    ): Context {
        val language =
            currentLanguage(context)

        applyPlatformLocale(
            context = context,
            language = language
        )

        val locale =
            Locale.forLanguageTag(
                language
            )

        Locale.setDefault(locale)

        val configuration =
            Configuration(
                context.resources.configuration
            )

        configuration.setLocale(locale)
        configuration.setLocales(
            LocaleList(locale)
        )

        return context.createConfigurationContext(
            configuration
        )
    }

    private fun applyPlatformLocale(
        context: Context,
        language: String
    ) {
        if (
            Build.VERSION.SDK_INT <
                Build.VERSION_CODES.TIRAMISU
        ) {
            return
        }

        val localeManager =
            context.getSystemService(
                LocaleManager::class.java
            )

        val requested =
            LocaleList.forLanguageTags(
                language
            )

        if (
            localeManager
                .applicationLocales
                .toLanguageTags() !=
            requested.toLanguageTags()
        ) {
            localeManager.applicationLocales =
                requested
        }
    }
}
