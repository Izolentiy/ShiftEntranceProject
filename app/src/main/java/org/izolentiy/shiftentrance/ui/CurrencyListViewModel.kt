package org.izolentiy.shiftentrance.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import org.izolentiy.shiftentrance.model.ExchangeRate
import org.izolentiy.shiftentrance.repository.Repository
import org.izolentiy.shiftentrance.repository.Resource
import javax.inject.Inject

@HiltViewModel
class CurrencyListViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {

    val exchangeRate: LiveData<Resource<out ExchangeRate?>> =
        repository.exchangeRate.flowOn(Dispatchers.IO).asLiveData()

    fun reloadData() = viewModelScope.launch(Dispatchers.IO) {
        repository.reloadRate()
    }

    companion object {
        private val TAG = "${CurrencyListViewModel::class.java.simpleName}_TAG"
    }

}