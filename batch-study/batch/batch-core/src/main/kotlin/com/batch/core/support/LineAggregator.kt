package com.batch.payment.domain.core

import org.springframework.batch.item.file.mapping.DefaultLineMapper
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor
import org.springframework.batch.item.file.transform.DelimitedLineAggregator
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer
import org.springframework.batch.item.file.transform.FieldSet

interface LineAggregator<T> {
    val headerNames: Array<String>

    fun delimitedLineAggregator(
        delimiter: String = DelimitedLineTokenizer.DELIMITER_COMMA
    ) =
        object : DelimitedLineAggregator<T>() {
            init {
                setDelimiter(delimiter)
                setFieldExtractor(
                    object : BeanWrapperFieldExtractor<T>() {
                        init {
                            setNames(headerNames)
                        }
                    }
                )
            }
        }
}

interface LineMapper<T> {

    fun fieldSetMapper(fs: FieldSet): T
    val headerNames: Array<String>

    fun lineMapper(delimiter: String = DelimitedLineTokenizer.DELIMITER_COMMA): org.springframework.batch.item.file.LineMapper<T> {
        val lineMapper = DefaultLineMapper<T>()
        val lineTokenizer = DelimitedLineTokenizer(delimiter)
        lineTokenizer.setNames(*this.headerNames)
        lineMapper.setLineTokenizer(lineTokenizer)
        lineMapper.setFieldSetMapper {
            fieldSetMapper(it)
        }
        return lineMapper
    }
}
