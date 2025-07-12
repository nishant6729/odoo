package com.example.skillswaps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DelayClass: ViewModel() {
    private val _isResult = MutableStateFlow(false)
    val isResult = _isResult.asStateFlow()
    init {
        viewModelScope.launch {
            delay(3000)
            _isResult.value=true
        }
    }
}