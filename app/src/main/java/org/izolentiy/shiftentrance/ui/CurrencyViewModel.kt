package org.izolentiy.shiftentrance.ui

import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import org.izolentiy.shiftentrance.model.ExchangeRate
import org.izolentiy.shiftentrance.repository.Repository
import org.izolentiy.shiftentrance.repository.Resource
import javax.inject.Inject

@HiltViewModel
class CurrencyViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {

    val latestRates: LiveData<Resource<List<ExchangeRate>?>> =
        repository.latestRates.flowOn(Dispatchers.IO).asLiveData()

    val baseSum = MutableLiveData(0.0f)

    fun loadLatestRates(count: Int = 8) {
        repository.loadRates(count)
    }

    companion object {
        private val TAG = "${CurrencyViewModel::class.java.simpleName}_TAG"
    }

}