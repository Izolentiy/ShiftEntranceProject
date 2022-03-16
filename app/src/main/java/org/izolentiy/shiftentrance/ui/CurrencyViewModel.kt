package org.izolentiy.shiftentrance.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class CurrencyViewModel @Inject constructor() : ViewModel() {
    val baseSum = MutableLiveData(0.0f)
}