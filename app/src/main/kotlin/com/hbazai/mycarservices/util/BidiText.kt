package com.hbazai.mycarservices.util

/**
 * Wraps text in Unicode LTR isolates (LRI…PDI) so numbers, dates and units
 * keep their left-to-right order when rendered inside RTL (Persian) layouts.
 */
fun ltr(text: String): String = "\u2066$text\u2069"
