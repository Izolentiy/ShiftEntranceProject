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

class CurrencyAdapter : ListAdapter<Currency, CurrencyAdapter.ViewHolder>(CurrencyComparator) {
    inner class ViewHolder(private val binding: Binding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(currency: Currency) {
            binding.apply {
                textViewCharCode.text = currency.charCode
                textViewName.text = currency.name
                textViewExchangeRate.text = binding.root.resources
                    .getString(R.string.base_currency_format, currency.value, BASE_CURRENCY)
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
