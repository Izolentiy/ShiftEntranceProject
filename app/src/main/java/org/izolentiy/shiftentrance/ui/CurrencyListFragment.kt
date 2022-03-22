package org.izolentiy.shiftentrance.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import org.izolentiy.shiftentrance.MESSAGE_FORMAT
import org.izolentiy.shiftentrance.MESSAGE_TIMEOUT
import org.izolentiy.shiftentrance.R
import org.izolentiy.shiftentrance.databinding.FragmentCurrencyListBinding
import org.izolentiy.shiftentrance.model.Currency
import org.izolentiy.shiftentrance.model.ExchangeRate
import org.izolentiy.shiftentrance.repository.Resource
import org.izolentiy.shiftentrance.ui.CurrencyFragment.Companion.CHAR_CODE
import org.izolentiy.shiftentrance.ui.CurrencyFragment.Companion.NOMINAL
import org.izolentiy.shiftentrance.ui.CurrencyFragment.Companion.RATE

@AndroidEntryPoint
class CurrencyListFragment : Fragment() {

    private var _binding: FragmentCurrencyListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CurrencyListViewModel by viewModels()
    private var errorShowedOnce = false
    private var shownSnackbar: Snackbar? = null

    private val onCurrencyClick: (Currency) -> Unit = { currency ->
        Log.d(TAG, "${currency.charCode}: is clicked")
        val args = Bundle().apply {
            putString(CHAR_CODE, currency.charCode)
            putDouble(RATE, currency.value / currency.nominal)
            putInt(NOMINAL, currency.nominal)
        }
        shownSnackbar?.dismiss()
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, CurrencyFragment.newInstance(args))
            .addToBackStack(null).commit()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val currencyAdapter = CurrencyAdapter(onCurrencyClick)

        _binding = FragmentCurrencyListBinding.inflate(inflater, container, false)
        binding.apply {
            swipeRefreshLayout.setOnRefreshListener {
                viewModel.reloadData()
                errorShowedOnce = false
            }
            recyclerViewCurrencies.apply {
                adapter = currencyAdapter
                layoutManager = LinearLayoutManager(requireContext())
                addItemDecoration(DividerItemDecoration(requireContext(), RecyclerView.VERTICAL))
            }
        }

        viewModel.exchangeRate.observe(viewLifecycleOwner) { resource ->
            handleResource(resource, currencyAdapter)
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun handleResource(result: Resource<out ExchangeRate?>, adapter: CurrencyAdapter) {
        val rate = result.data
        when (result.status) {
            Resource.Status.ERROR -> {
                if (!errorShowedOnce) {
                    errorShowedOnce = true
                    val message = getString(R.string.unable_to_download)
                    showSnackBar(message, Snackbar.LENGTH_INDEFINITE, true)
                } else if (rate != null) {
                    val message =
                        getString(R.string.data_loaded, MESSAGE_FORMAT.format(rate.loaded))
                    showSnackBar(message, MESSAGE_TIMEOUT, false)
                } else {
                    val message = getString(R.string.empty_data)
                    showSnackBar(message, Snackbar.LENGTH_INDEFINITE, false)
                }
                Log.e(TAG, "handleResource: ERROR")
            }
            Resource.Status.LOADING -> {
                Log.e(TAG, "handleResource: LOADING")
            }
            Resource.Status.SUCCESS -> {
                val message = if (rate != null)
                    getString(R.string.data_loaded, MESSAGE_FORMAT.format(rate.loaded))
                else getString(R.string.empty_data)

                Log.e(TAG, "handleResource: SUCCESS")
                showSnackBar(message, MESSAGE_TIMEOUT, false)
            }
        }
        if (rate != null)
            processData(rate, adapter)
        binding.swipeRefreshLayout.isRefreshing = false
        Log.d(TAG, "onCreateView: CURRENCIES_SUBMITTED")
    }

    private fun processData(rate: ExchangeRate, adapter: CurrencyAdapter) {
        adapter.submitList(rate.currencies)
    }

    private fun showSnackBar(message: String, time: Int, isError: Boolean) {
        val view = activity?.findViewById<View>(R.id.fragment_container)!!
        val snackbar: Snackbar = Snackbar.make(view, message, time)
        shownSnackbar = snackbar

        if (isError) snackbar.setAction("OK") {
            val latestRate = viewModel.exchangeRate.value?.data
            Log.e(TAG, "showSnackBar: latestRate $latestRate")
            if (latestRate?.currencies?.isNotEmpty() == true) {
                val stringRes = R.string.local_data_loaded
                val msg = getString(stringRes, MESSAGE_FORMAT.format(latestRate.loaded))
                showSnackBar(msg, MESSAGE_TIMEOUT, false)
            } else {
                val msg = getString(R.string.empty_data)
                showSnackBar(msg, Snackbar.LENGTH_INDEFINITE, false)
            }
        }
        snackbar.show()
    }

    companion object {
        private val TAG = "${CurrencyListFragment::class.java.simpleName}_TAG"
    }

}