package hr.unipu.journals.usecase

import org.jsoup.Jsoup
import org.jsoup.safety.Safelist

fun sanitize(input: String?, safelist: Safelist = Safelist.none()) = Jsoup.clean(input ?: "", safelist)