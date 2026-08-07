package com.newswire.data.prefs

import android.content.Context
import android.content.SharedPreferences
import com.newswire.ui.home.NewsCategory
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("newswire_prefs", Context.MODE_PRIVATE)

    fun saveSelectedCategory(name: String) {
        prefs.edit().putString(KEY_CATEGORY, name).apply()
    }

    fun loadSelectedCategory(default: NewsCategory): NewsCategory {
        val name = prefs.getString(KEY_CATEGORY, null) ?: return default
        return runCatching { NewsCategory.valueOf(name) }.getOrDefault(default)
    }

    fun saveCurrentKey(key: String?) {
        prefs.edit().putString(KEY_CURRENT, key ?: "").apply()
    }

    fun loadCurrentKey(): String? =
        prefs.getString(KEY_CURRENT, null)?.takeIf { it.isNotBlank() }

    fun loadSeenLinks(): Set<String> =
        prefs.getStringSet(KEY_SEEN_LINKS, emptySet()) ?: emptySet()

    fun addSeenLinks(links: Collection<String>) =
        appendBounded(KEY_SEEN_LINKS, links)

    fun loadSeenFactIds(): Set<String> =
        prefs.getStringSet(KEY_SEEN_FACTS, emptySet()) ?: emptySet()

    fun addSeenFactIds(ids: Collection<String>) =
        appendBounded(KEY_SEEN_FACTS, ids)

    private fun appendBounded(key: String, items: Collection<String>) {
        if (items.isEmpty()) return
        val set = LinkedHashSet(prefs.getStringSet(key, emptySet()) ?: emptySet())
        set.addAll(items)
        while (set.size > MAX_SEEN) {
            set.remove(set.first())
        }
        prefs.edit().putStringSet(key, set).apply()
    }

    private companion object {
        const val KEY_CATEGORY = "selected_category"
        const val KEY_CURRENT = "current_key"
        const val KEY_SEEN_LINKS = "seen_links"
        const val KEY_SEEN_FACTS = "seen_facts"
        const val MAX_SEEN = 400
    }
}
