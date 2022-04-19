package org.izolentiy.shiftentrance.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
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
    private var displayedSnackbar: Snackbar? = null

    private val onCurrencyClick: (Currency) -> Unit = { currency ->
        Log.d(TAG, "${currency.charCode}: is clicked")
        val args = Bundle().apply {
            putString(CHAR_CODE, currency.charCode)
            putDouble(RATE, currency.value / currency.nominal)
            putInt(NOMINAL, currency.nominal)
        }
        displayedSnackbar?.dismiss()
        // TODO: Investigate fragment manager mechanism and navigation libraries later
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, CurrencyFragment.newInstance(args))
            .addToBackStack(null).commit()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCurrencyListBinding.inflate(inflater, container, false)

        val currencyNames = mutableMapOf<String, String>()
        // Populate currencyNames from string array in resources
        resources.getStringArray(R.array.currency_names).forEach { item ->
            // item = "key:value"
            val keyValuePair = item.split(":")
            with(keyValuePair) { currencyNames[first()] = last() }
        }
        val currencyAdapter = CurrencyAdapter(onCurrencyClick, currencyNames).also {
            Log.e(TAG, "onCreateView: CREATE A NEW CURRENCY ADAPTER")
        }
        binding.apply {
            toolbarList.title = resources.getString(R.string.app_name)
            swipeRefreshLayout.setOnRefreshListener {
                viewModel.reloadData()
                viewModel.errorShowedOnce = false
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

    private fun handleResource(resource: Resource<ExchangeRate>, adapter: CurrencyAdapter) {
        val rate = when (resource) {
            is Resource.Error -> resource.data
            is Resource.Success -> resource.data
            else -> null
        }
        with(binding.layoutError) {
            if (resource is Resource.Error && rate == null) {
                ErrorTarget(
                    messageTarget = textViewErrorMessage,
                    detailTarget = textViewErrorDetail,
                    actionTarget = buttonRetry,
                    requireContext()
                ).handleError(resource.error)

                buttonRetry.setOnClickListener { viewModel.reloadData() }
                root.isVisible = true
            } else {
                root.isVisible = false
            }
        }
        if (rate != null) {
            val message = if (rate.currencies.isEmpty())
                getString(R.string.empty_data)
            else getString(R.string.data_loaded, MESSAGE_FORMAT.format(rate.loaded))

            showSnackBar(message, MESSAGE_TIMEOUT)
            adapter.submitList(rate.currencies)
        }
        when (resource) {
            is Resource.Error -> {
                if (!viewModel.errorShowedOnce) {
                    viewModel.errorShowedOnce = true
                    val message = getString(R.string.unable_to_download)
                    showErrorSnackBar(message, rate)
                }
                Log.e(TAG, "handleResource: ERROR")
            }
            is Resource.Loading -> Log.e(TAG, "handleResource: LOADING")
            is Resource.Success -> Log.e(TAG, "handleResource: SUCCESS")
        }
        binding.swipeRefreshLayout.isRefreshing = resource is Resource.Loading
    }

    private fun showSnackBar(string: String, time: Int) {
        val view: View = activity?.findViewById(binding.root.id)!!
        Snackbar.make(view, string, time)
            .also { displayedSnackbar = it }
            .show()
    }

    private fun showErrorSnackBar(string: String, rate: ExchangeRate?) {
        val view: View = activity?.findViewById(binding.root.id)!!
        Snackbar.make(view, string, Snackbar.LENGTH_INDEFINITE)
            .also { displayedSnackbar = it }
            .setAction("OK") {
                if (rate?.currencies?.isNotEmpty() == true) {
                    val resId = R.string.local_data_loaded
                    val message = getString(resId, MESSAGE_FORMAT.format(rate.loaded))
                    showSnackBar(message, MESSAGE_TIMEOUT)
                } else {
                    val message = getString(R.string.empty_data)
                    showSnackBar(message, Snackbar.LENGTH_INDEFINITE)
                }
            }.show()
    }

    companion object {
        private val TAG = "${CurrencyListFragment::class.java.simpleName}_TAG"
    }

}