package org.izolentiy.shiftentrance.ui

import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.izolentiy.shiftentrance.model.Currency
import org.izolentiy.shiftentrance.model.ExchangeRate
import org.izolentiy.shiftentrance.repository.Repository
import javax.inject.Inject

@HiltViewModel
class CurrencyListViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {

    val exchangeRate: LiveData<ExchangeRate?> = repository.getExchangeRate().asLiveData()

    fun reloadData() = viewModelScope.launch(Dispatchers.IO) {
        repository.reloadDailyRate()
    }

    companion object {
        private val TAG = "${CurrencyListViewModel::class.java.simpleName}_TAG"
    }

}