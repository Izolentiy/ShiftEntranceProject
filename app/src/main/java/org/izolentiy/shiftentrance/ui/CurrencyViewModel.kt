package org.izolentiy.shiftentrance.ui

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.izolentiy.shiftentrance.model.ExchangeRate
import org.izolentiy.shiftentrance.repository.Repository
import javax.inject.Inject

@HiltViewModel
class CurrencyViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {
    private val _latestRates = MutableLiveData<List<ExchangeRate>>()
    val latestRates: LiveData<List<ExchangeRate>> = _latestRates

    val baseSum = MutableLiveData(0.0f)

    fun loadLatestRates(count: Int = 15) = viewModelScope.launch(Dispatchers.IO) {
        Log.i(TAG, "loadLatestRates: START LOADING $count LATEST RATES")
        val result = repository.loadLatestRates(count) ?: emptyList()
        _latestRates.postValue(result)
        Log.i(TAG, "loadLatestRates: LOADING OF $count LATEST RATES ENDED ")
    }

    companion object {
        private val TAG = "${CurrencyViewModel::class.java.simpleName}_TAG"
    }

}