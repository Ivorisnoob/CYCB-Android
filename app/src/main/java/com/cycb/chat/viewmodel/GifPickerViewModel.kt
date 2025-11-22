package com.cycb.chat.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cycb.chat.network.TenorApiService
import com.cycb.chat.data.model.TenorGif
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class GifPickerUiState {
    object Loading : GifPickerUiState()
    data class Success(val gifs: List<TenorGif>, val hasMore: Boolean) : GifPickerUiState()
    data class Error(val message: String) : GifPickerUiState()
}

class GifPickerViewModel : ViewModel() {
    private val tenorApi = TenorApiService.create()

    private val _uiState = MutableStateFlow<GifPickerUiState>(GifPickerUiState.Loading)
    val uiState: StateFlow<GifPickerUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private var nextPosition: String? = null
    private var currentQuery: String = ""

    init {
        loadTrendingGifs()
    }

    fun loadTrendingGifs() {
        viewModelScope.launch {
            _uiState.value = GifPickerUiState.Loading
            try {
                val response = tenorApi.getTrendingGifs(
                    apiKey = TenorApiService.API_KEY,
                    limit = 20
                )
                nextPosition = response.next
                currentQuery = ""
                _uiState.value = GifPickerUiState.Success(
                    gifs = response.results,
                    hasMore = response.next != null
                )
            } catch (e: Exception) {
                _uiState.value = GifPickerUiState.Error(
                    e.message ?: "Failed to load trending GIFs"
                )
            }
        }
    }

    fun searchGifs(query: String) {
        if (query.isBlank()) {
            loadTrendingGifs()
            return
        }

        _searchQuery.value = query
        currentQuery = query

        viewModelScope.launch {
            _uiState.value = GifPickerUiState.Loading
            try {
                val response = tenorApi.searchGifs(
                    query = query,
                    apiKey = TenorApiService.API_KEY,
                    limit = 20
                )
                nextPosition = response.next
                _uiState.value = GifPickerUiState.Success(
                    gifs = response.results,
                    hasMore = response.next != null
                )
            } catch (e: Exception) {
                _uiState.value = GifPickerUiState.Error(
                    e.message ?: "Failed to search GIFs"
                )
            }
        }
    }

    fun loadMoreGifs() {

    }

    fun clearSearch() {
        _searchQuery.value = ""
        loadTrendingGifs()
    }
}
