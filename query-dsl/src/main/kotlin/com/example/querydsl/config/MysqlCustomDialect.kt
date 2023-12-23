//package com.example.querydsl.config
//
//import org.hibernate.dialect.MySQL57Dialect
//import org.hibernate.dialect.function.StandardSQLFunction
//import org.hibernate.type.StandardBasicTypes
//
//class MysqlCustomDialect : MySQL57Dialect() {
//    init {
//        registerFunction(
//            "GROUP_CONCAT",
//            StandardSQLFunction("group_concat", StandardBasicTypes.STRING)
//        )
//    }
//}
