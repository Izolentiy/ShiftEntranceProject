package org.izolentiy.shiftentrance.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.izolentiy.shiftentrance.BASE_CURRENCY
import org.izolentiy.shiftentrance.R
import org.izolentiy.shiftentrance.model.Currency
import org.izolentiy.shiftentrance.databinding.ItemCurrencyBinding as Binding

class CurrencyAdapter(
    private val onCurrencyClick: (Currency) -> Unit,
    private val currencyNames: Map<String, String>
) : ListAdapter<Currency, CurrencyAdapter.ViewHolder>(CurrencyComparator) {

    inner class ViewHolder(
        private val binding: Binding
    ) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener { onCurrencyClick(getItem(adapterPosition)) }
        }

        fun bind(currency: Currency) = with(currency) {
            binding.apply {
                textViewNominalCharCode.text = root.resources
                    .getString(R.string.currency_format, charCode, nominal)
                textViewName.text = currencyNames[charCode]
                textViewExchangeRate.text = root.resources
                    .getString(R.string.base_currency_format, value, BASE_CURRENCY)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = ViewHolder(
        Binding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    object CurrencyComparator : DiffUtil.ItemCallback<Currency>() {
        override fun areItemsTheSame(oldItem: Currency, newItem: Currency) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Currency, newItem: Currency) =
            oldItem == newItem
    }
}
