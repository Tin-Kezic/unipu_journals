package hr.unipu.journals.usecase

import org.jsoup.Jsoup
import org.jsoup.safety.Safelist

fun sanitize(input: String?) = Jsoup.clean(input ?: "", Safelist.none())