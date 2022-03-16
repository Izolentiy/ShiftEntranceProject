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
import dagger.hilt.android.AndroidEntryPoint
import org.izolentiy.shiftentrance.databinding.FragmentCurrencyListBinding

@AndroidEntryPoint
class CurrencyListFragment : Fragment() {

    private var _binding: FragmentCurrencyListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CurrencyListViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val currencyAdapter = CurrencyAdapter()

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
            if (rate != null) currencyAdapter.submitList(rate.currencies)
            binding.swipeRefreshLayout.isRefreshing = false
            Log.d(TAG, "onCreateView: CURRENCIES_SUBMITTED")
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private val TAG = "${CurrencyListFragment::class.java.simpleName}_TAG"
    }

}