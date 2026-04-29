package com.mis.parentapp.features.services

/**
 * Converts a numeric amount to words (Philippine Peso format)
 * Example: 2000.00 -> "TWO THOUSAND PESOS ONLY"
 */
fun numberToWords(amount: Double): String {
    val ones = arrayOf("", "ONE", "TWO", "THREE", "FOUR", "FIVE", "SIX", "SEVEN", "EIGHT", "NINE",
        "TEN", "ELEVEN", "TWELVE", "THIRTEEN", "FOURTEEN", "FIFTEEN", "SIXTEEN",
        "SEVENTEEN", "EIGHTEEN", "NINETEEN")
    val tens = arrayOf("", "", "TWENTY", "THIRTY", "FORTY", "FIFTY", "SIXTY", "SEVENTY", "EIGHTY", "NINETY")

    val wholeAmount = amount.toInt()
    if (wholeAmount == 0) return "ZERO PESOS ONLY"

    var result = ""

    if (wholeAmount >= 1000) {
        val thousands = wholeAmount / 1000
        result += convertHundreds(thousands, ones, tens) + "THOUSAND "
    }

    val remainder = wholeAmount % 1000
    if (remainder > 0) {
        result += convertHundreds(remainder, ones, tens)
    }

    return result.trim() + " PESOS ONLY"
}

/**
 * Helper function to convert numbers 0-999 to words
 */
private fun convertHundreds(num: Int, ones: Array<String>, tens: Array<String>): String {
    var res = ""
    val hundreds = num / 100
    val remainder = num % 100

    if (hundreds > 0) {
        res += "${ones[hundreds]} HUNDRED "
        if (remainder > 0) res += "AND "
    }

    if (remainder >= 20) {
        res += "${tens[remainder / 10]} "
        if (remainder % 10 > 0) res += "${ones[remainder % 10]} "
    } else if (remainder > 0) {
        res += "${ones[remainder]} "
    }
    return res
}