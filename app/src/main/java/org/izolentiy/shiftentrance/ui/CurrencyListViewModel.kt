package org.izolentiy.shiftentrance.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import org.izolentiy.shiftentrance.model.ExchangeRate
import org.izolentiy.shiftentrance.repository.RateRepository
import org.izolentiy.shiftentrance.repository.Resource
import javax.inject.Inject

@HiltViewModel
class CurrencyListViewModel @Inject constructor(
    private val rateRepository: RateRepository
) : ViewModel() {

    val exchangeRate: LiveData<Resource<ExchangeRate>> =
        rateRepository.exchangeRate.flowOn(Dispatchers.IO).asLiveData()

    fun reloadData() {
        rateRepository.reloadRate()
    }

    companion object {
        private val TAG = "${CurrencyListViewModel::class.java.simpleName}_TAG"
    }

}