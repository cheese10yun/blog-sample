package com.batch.study.core

import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor
import org.springframework.batch.item.file.transform.DelimitedLineAggregator

interface CsvLineAggregator<T> {

    val headerNames: Array<String>

    fun delimitedLineAggregator(
        delimiter: String = ","
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