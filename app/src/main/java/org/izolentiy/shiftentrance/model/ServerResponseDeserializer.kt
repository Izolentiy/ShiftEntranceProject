package org.izolentiy.shiftentrance.model

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import org.izolentiy.shiftentrance.DATE_FORMAT
import java.lang.reflect.Type

class ServerResponseDeserializer : JsonDeserializer<ExchangeRate> {
    override fun deserialize(
        json: JsonElement?,
        typeOfT: Type?,
        context: JsonDeserializationContext?
    ): ExchangeRate {
        val data = json?.asJsonObject ?: return ExchangeRate()
        val valute = data.get("Valute").asJsonObject
        val currencies = mutableListOf<Currency>()
        for (currency in currencyList) {
            val jsonCurrency = valute.get(currency).asJsonObject
            val element = Currency(
                id = jsonCurrency.get("ID").asString,
                numCode = jsonCurrency.get("NumCode").asString,
                charCode = jsonCurrency.get("CharCode").asString,
                nominal = jsonCurrency.get("Nominal").asInt,
                name = jsonCurrency.get("Name").asString,
                value = jsonCurrency.get("Value").asDouble,
                previous = jsonCurrency.get("Previous").asDouble
            )
            currencies.add(element)
        }

        // See explanation below
        val urlWithExtraSymbols = data.get("PreviousURL").asString
            .replace("/", "//") // <- this have to be used now

        return ExchangeRate(
            date = DATE_FORMAT.parse(data.get("Date").asString)!!,
            previousDate = DATE_FORMAT.parse(data.get("PreviousDate").asString)!!,
//            previousURL = data.get("PreviousURL").asString, // <- works not on all devices
            previousURL = urlWithExtraSymbols,
            timestamp = data.get("Timestamp").asString,
            currencies = currencies
        )
    }

    private val currencyList = listOf(
        "AMD", "AUD", "AZN", "BGN", "BRL", "BYN", "CAD", "CHF", "CNY",
        "CZK", "DKK", "EUR", "GBP", "HKD", "HUF", "INR", "JPY", "KGS",
        "KRW", "KZT", "MDL", "NOK", "PLN", "RON", "SEK", "SGD", "TJS",
        "TMT", "TRY", "UAH", "USD", "UZS", "XDR", "ZAR"
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
-----><-----
Explanation:

The url string parsed by gson from json sent from server looks like this:
"//www.cbr-xml-daily.ru/archive/2022/03/29/daily_json.js" <- now return 404

It worked fine earlier, but now you have to place extra symbol "/" inside
"//www.cbr-xml-daily.ru/archive/2022/03/29//daily_json.js" <- behave as earlier

Only one symbol of difference. And before the code worked without
ridiculous workaround like adding extra "/" symbol.
The funniest thing is that it doesn't matter where to place extra symbol or
how much of them is in url. They are all valid...

"//www.cbr-xml-daily.ru//archive/2022/03/29/daily_json.js"
"//www.cbr-xml-daily.ru/archive//2022/03/29/daily_json.js"
"//www.cbr-xml-daily.ru/archive/2022//03/29/daily_json.js"
"//www.cbr-xml-daily.ru/archive/2022/03/29//daily_json.js"
"////www.cbr-xml-daily.ru//archive///////2022///03/29////daily_json.js"

Edit:
I found that this url (with https:)"//www.cbr-xml-daily.ru/archive/2022/03/29/daily_json.js"
works fine on [Pixel 4 API 27], but don't on [Galaxy S9 API 29].
In browser it didn't work yesterday, but today it started to work again...
Anyway url with extra symbols works great on all devices

 */