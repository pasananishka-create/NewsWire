package com.newswire.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.newswire.data.model.Article
import com.newswire.data.model.FeedItem
import com.newswire.data.model.FunFact
import com.newswire.data.prefs.PreferencesManager
import com.newswire.data.repository.FactsRepository
import com.newswire.data.repository.NewsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val selected: NewsCategory = NewsCategory.ALL,
    val items: List<FeedItem> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val restorePageIndex: Int = 0,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: NewsRepository,
    private val factsRepository: FactsRepository,
    private val prefs: PreferencesManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        HomeUiState(selected = prefs.loadSelectedCategory(NewsCategory.ALL))
    )
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _articleImages = MutableStateFlow<Map<String, String>>(emptyMap())
    val articleImages: StateFlow<Map<String, String>> = _articleImages.asStateFlow()

    private val seenLinks = LinkedHashSet(prefs.loadSeenLinks())
    private val seenFactIds = LinkedHashSet(prefs.loadSeenFactIds())
    private val factCache = ArrayDeque<FunFact>()
    private val pendingImages = mutableSetOf<String>()
    private val savedKey = prefs.loadCurrentKey()
    private var pendingRestore: String? = savedKey
    private var fetching = false

    init {
        prefetchFacts(3)
        load()
        viewModelScope.launch {
            while (isActive) {
                delay(AUTO_REFRESH_MS)
                refresh(silent = true)
            }
        }
    }

    fun selectCategory(category: NewsCategory) {
        if (_uiState.value.selected == category) return
        _uiState.update { it.copy(selected = category, error = null, restorePageIndex = 0) }
        pendingRestore = null
        prefs.saveSelectedCategory(category.name)
        load()
    }

    fun load() {
        if (fetching) return
        fetching = true
        val category = _uiState.value.selected
        val hasItems = _uiState.value.items.isNotEmpty()
        _uiState.update {
            it.copy(isLoading = !hasItems && !it.isRefreshing, error = null)
        }
        viewModelScope.launch {
            try {
                val news = repository.getHeadlines(category.query)
                val mixed = mixWithFacts(news)
                val filtered = mixed.filterNot { it.isSeen() }
                val restoreIdx = pendingRestore?.let { key ->
                    filtered.indexOfFirst { it.key == key }
                }?.takeIf { it >= 0 } ?: 0
                pendingRestore = null
                _uiState.update {
                    it.copy(
                        items = filtered,
                        restorePageIndex = restoreIdx,
                        isLoading = false,
                        isRefreshing = false,
                        error = null,
                    )
                }
                saveSession(filtered, restoreIdx)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        error = e.message ?: "Something went wrong",
                    )
                }
            } finally {
                fetching = false
            }
        }
    }

    fun refresh(silent: Boolean = false) {
        if (fetching) return
        fetching = true
        val category = _uiState.value.selected
        if (!silent) {
            _uiState.update { it.copy(isRefreshing = true, error = null) }
        }
        viewModelScope.launch {
            try {
                val news = repository.getHeadlines(category.query)
                val mixed = mixWithFacts(news)
                val fresh = mixed.filterNot { it.isSeen() }
                val current = _uiState.value.items
                val existingKeys = current.mapTo(hashSetOf()) { it.key }
                val added = fresh.filterNot { it.key in existingKeys }
                _uiState.update {
                    it.copy(
                        items = current + added,
                        isRefreshing = false,
                        error = null,
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isRefreshing = false, error = if (silent) null else e.message)
                }
            } finally {
                fetching = false
            }
        }
    }

    fun onPageAdvanced(advanced: Int) {
        val items = _uiState.value.items
        if (advanced <= 0 || items.isEmpty()) return
        val removed = items.take(advanced)
        val links = removed.mapNotNull { (it as? FeedItem.News)?.article?.link }
        val factIds = removed.mapNotNull { (it as? FeedItem.Fact)?.fact?.id }
        seenLinks.addAll(links)
        seenFactIds.addAll(factIds)
        prefs.addSeenLinks(links)
        prefs.addSeenFactIds(factIds)
        val remaining = items.drop(advanced)
        _uiState.update { it.copy(items = remaining) }
        saveSession(remaining, 0)
    }

    fun ensureImage(link: String) {
        if (link.isBlank() || _articleImages.value.containsKey(link)) return
        if (!pendingImages.add(link)) return
        viewModelScope.launch {
            try {
                val url = repository.fetchImageUrl(link)
                if (!url.isNullOrBlank()) {
                    _articleImages.update { it + (link to url) }
                }
            } finally {
                pendingImages.remove(link)
            }
        }
    }

    private fun saveSession(items: List<FeedItem>, index: Int) {
        val currentKey = items.getOrNull(index)?.key
        prefs.saveCurrentKey(currentKey)
        prefs.saveSelectedCategory(_uiState.value.selected.name)
    }

    private fun FeedItem.isSeen(): Boolean = when (this) {
        is FeedItem.News -> article.link in seenLinks
        is FeedItem.Fact -> fact.id in seenFactIds
    }

    private suspend fun mixWithFacts(news: List<Article>): List<FeedItem> {
        val result = mutableListOf<FeedItem>()
        var sinceFact = 0
        var factCount = 0
        for (article in news) {
            result.add(FeedItem.News(article))
            sinceFact++
            if (sinceFact >= FACT_INTERVAL && factCount < MAX_FACTS_PER_DECK) {
                val fact = nextFact()
                if (fact != null) {
                    result.add(FeedItem.Fact(fact))
                    sinceFact = 0
                    factCount++
                }
            }
        }
        return result
    }

    private suspend fun nextFact(): FunFact? {
        val cached = factCache.firstOrNull { it.id !in seenFactIds }
        if (cached != null) {
            factCache.remove(cached)
            return cached
        }
        return factsRepository.fetchRandomFact()
            ?.takeIf { it.id !in seenFactIds && it.text.isNotBlank() }
    }

    private fun prefetchFacts(count: Int) {
        repeat(count) {
            viewModelScope.launch {
                val fact = factsRepository.fetchRandomFact() ?: return@launch
                if (fact.id !in seenFactIds && fact.text.isNotBlank() && factCache.none { it.id == fact.id }) {
                    factCache.addLast(fact)
                }
            }
        }
    }

    private companion object {
        const val AUTO_REFRESH_MS = 30_000L
        const val FACT_INTERVAL = 4
        const val MAX_FACTS_PER_DECK = 6
    }
}
