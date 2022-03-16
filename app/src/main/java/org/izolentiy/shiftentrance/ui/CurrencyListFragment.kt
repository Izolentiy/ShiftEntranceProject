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
import org.izolentiy.shiftentrance.ui.CurrencyFragment.Companion.CHAR_CODE
import org.izolentiy.shiftentrance.ui.CurrencyFragment.Companion.RATE

@AndroidEntryPoint
class CurrencyListFragment : Fragment() {

    private var _binding: FragmentCurrencyListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CurrencyListViewModel by viewModels()

    private val onCurrencyClick: (Currency) -> Unit = { currency ->
        Log.d(TAG, "${currency.charCode}: is clicked")
        val args = Bundle().apply {
            putString(CHAR_CODE, currency.charCode)
            putDouble(RATE, currency.value / currency.nominal)
        }
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
            }
            recyclerViewCurrencies.apply {
                adapter = currencyAdapter
                layoutManager = LinearLayoutManager(requireContext())
                addItemDecoration(DividerItemDecoration(requireContext(), RecyclerView.VERTICAL))
            }
        }

        viewModel.exchangeRate.observe(viewLifecycleOwner) { rate ->
            if (rate != null) {
                currencyAdapter.submitList(rate.currencies)

                val message = getString(R.string.data_loaded, MESSAGE_FORMAT.format(rate.loaded))
                showSnackBar(message, MESSAGE_TIMEOUT)
            }
            binding.swipeRefreshLayout.isRefreshing = false
            Log.d(TAG, "onCreateView: CURRENCIES_SUBMITTED")
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showSnackBar(message: String, time: Int) {
        Snackbar.make(
            activity?.findViewById(R.id.fragment_container)!!,
            message, time
        ).show()
    }

    companion object {
        private val TAG = "${CurrencyListFragment::class.java.simpleName}_TAG"
    }

}