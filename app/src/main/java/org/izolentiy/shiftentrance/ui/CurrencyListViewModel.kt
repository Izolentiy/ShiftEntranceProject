package org.izolentiy.shiftentrance.ui

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.izolentiy.shiftentrance.model.Currency
import org.izolentiy.shiftentrance.repository.Repository
import javax.inject.Inject

@HiltViewModel
class CurrencyListViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {

    private val _currencies = MutableLiveData<List<Currency>>()
    val currencies: LiveData<List<Currency>> get() = _currencies

    fun fetchCurrencies() = viewModelScope.launch(Dispatchers.IO) {
        _currencies.postValue(repository.fetchCurrencies())
        Log.d(TAG, "fetchCurrencies: CURRENCIES_FETCHED")
    }

    companion object {
        private val TAG = "${CurrencyListViewModel::class.java.simpleName}_TAG"
    }

}