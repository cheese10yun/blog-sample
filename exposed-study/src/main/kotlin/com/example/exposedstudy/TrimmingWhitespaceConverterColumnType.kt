package com.example.exposedstudy

import org.jetbrains.exposed.sql.Expression
import org.jetbrains.exposed.sql.ExpressionWithColumnType
import org.jetbrains.exposed.sql.Function
import org.jetbrains.exposed.sql.IColumnType
import org.jetbrains.exposed.sql.QueryBuilder
import org.jetbrains.exposed.sql.VarCharColumnType
import org.jetbrains.exposed.sql.append


/**
 * TrimmingWhitespace
 */
class TrimmingWhitespaceConverterColumnType(length: Int) : VarCharColumnType(colLength = length) {

    override fun valueToDB(value: Any?): String? {
        return when (value) {
            is String -> value.trim()
            null -> null
            else -> throw IllegalArgumentException("${value::class.java.typeName} 타입은 Exposed 기반 컨버터에서 지원하지 않습니다.")
        }
    }

    override fun valueFromDB(value: Any): String {
        return when (value) {
            is String -> value.trim()
            else -> throw IllegalArgumentException("${value::class.java.typeName} 타입은 Exposed 기반 컨버터에서 지원하지 않습니다.")
        }
    }
}


fun <T : Any> ExpressionWithColumnType<T>.groupConcat(distinct: Boolean = false): CustomFunction<T> = CustomFunction("group_concat", columnType, distinct, this)


open class CustomFunction<T>(
    val functionName: String,
    _columnType: IColumnType,
    val distinct: Boolean = false,
    vararg val expr: Expression<*>
) : Function<T>(_columnType) {
    override fun toQueryBuilder(queryBuilder: QueryBuilder): Unit = queryBuilder {
        append(functionName, '(')
        if (distinct) {
            append("DISTINCT ")
        }
        expr.appendTo { +it }
        append(')')
    }
}


