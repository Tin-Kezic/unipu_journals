package hr.unipu.journals.data.domain.converter

import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter
import org.springframework.data.convert.WritingConverter
import java.net.URI

@WritingConverter
class UriToStringConverter : Converter<URI, String> {
    override fun convert(source: URI): String = source.toString()
}

@ReadingConverter
class StringToUriConverter : Converter<String, URI> {
    override fun convert(source: String): URI = URI.create(source)
}