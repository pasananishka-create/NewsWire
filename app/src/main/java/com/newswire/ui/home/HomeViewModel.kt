package com.newswire.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.newswire.data.model.Article
import com.newswire.data.repository.NewsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val selected: NewsCategory = NewsCategory.ALL,
    val articles: List<Article> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: NewsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val cache = mutableMapOf<NewsCategory, List<Article>>()
    private var fetching = false

    init {
        load()
    }

    fun selectCategory(category: NewsCategory) {
        if (_uiState.value.selected == category) return
        _uiState.update { it.copy(selected = category, error = null) }
        cache[category]?.let { cached ->
            _uiState.update { it.copy(articles = cached, isLoading = false, error = null) }
        } ?: run {
            _uiState.update { it.copy(articles = emptyList(), isLoading = true, error = null) }
            load()
        }
    }

    fun load() {
        if (fetching) return
        fetching = true
        val category = _uiState.value.selected
        val hasCache = cache.containsKey(category)
        _uiState.update {
            it.copy(isLoading = !hasCache && !it.isRefreshing, error = null)
        }
        viewModelScope.launch {
            try {
                val articles = repository.getHeadlines(category.query)
                cache[category] = articles
                _uiState.update {
                    it.copy(
                        articles = articles,
                        isLoading = false,
                        isRefreshing = false,
                        error = null,
                    )
                }
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

    fun refresh() {
        if (fetching) return
        fetching = true
        val category = _uiState.value.selected
        _uiState.update { it.copy(isRefreshing = true, error = null) }
        viewModelScope.launch {
            try {
                val articles = repository.getHeadlines(category.query)
                cache[category] = articles
                _uiState.update {
                    it.copy(
                        articles = articles,
                        isRefreshing = false,
                        error = null,
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isRefreshing = false, error = e.message ?: "Something went wrong")
                }
            } finally {
                fetching = false
            }
        }
    }
}
