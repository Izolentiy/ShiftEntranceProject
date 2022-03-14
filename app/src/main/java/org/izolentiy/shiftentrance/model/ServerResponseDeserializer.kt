package org.izolentiy.shiftentrance.model

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

class ServerResponseDeserializer : JsonDeserializer<ServerResponse> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): ServerResponse {
        val data = json?.asJsonObject ?: return ServerResponse(
            date = "",
            previousDate = "",
            previousURL = "",
            timestamp = "",
            currencies = listOf()
        )
        val valute = data.get("Valute").asJsonObject
        val currencies = mutableListOf<Currency>()
        for (currency in currencyList) {
            val obj = valute.get(currency).asJsonObject
            val element = Currency(
                id = obj.asJsonObject.get("ID").asString,
                numCode = obj.asJsonObject.get("NumCode").asString,
                charCode = obj.asJsonObject.get("CharCode").asString,
                nominal = obj.asJsonObject.get("Nominal").asInt,
                name = obj.asJsonObject.get("Name").asString,
                value = obj.asJsonObject.get("Value").asDouble,
                previous = obj.asJsonObject.get("Previous").asDouble
            )
            currencies.add(element)
        }
        return ServerResponse(
            date = data.get("Date").asString,
            previousDate = data.get("PreviousDate").asString,
            previousURL = data.get("PreviousURL").asString,
            timestamp = data.get("Timestamp").asString,
            currencies = currencies
        )
    }

    private val currencyList = listOf(
        "AMD", "AUD", "AZN", "BGN", "BRL", "BYN", "CAD",
        "CHF", "CNY", "CZK", "DKK", "EUR", "GBP", "HKD",
        "HUF", "INR", "JPY", "KGS", "KRW", "KZT", "MDL",
        "NOK", "PLN", "RON", "SEK", "SGD", "TJS", "TMT",
        "TRY", "UAH", "USD", "UZS", "XDR", "ZAR"
    )

}

/*
Server response
{
    "Date": "2022-03-15T11:30:00+03:00",
    "PreviousDate": "2022-03-12T11:30:00+03:00",
    "PreviousURL": "\/\/www.cbr-xml-daily.ru\/archive\/2022\/03\/12\/daily_json.js",
    "Timestamp": "2022-03-14T15:00:00+03:00",
    "Valute": {
        "AUD": {
            "ID": "R01010",
            "NumCode": "036",
            "CharCode": "AUD",
            "Nominal": 1,
            "Name": "Австралийский доллар",
            "Value": 83.5173,
            "Previous": 85.6374
        }, ...
    }
}
 */