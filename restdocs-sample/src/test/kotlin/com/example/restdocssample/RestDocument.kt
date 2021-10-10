package com.example.restdocssample

import org.springframework.restdocs.operation.Operation
import org.springframework.restdocs.payload.AbstractFieldsSnippet
import org.springframework.restdocs.payload.FieldDescriptor
import org.springframework.restdocs.snippet.Attributes

class RestDocument {

    fun customFieldsFields(
        vararg descriptors: FieldDescriptor
    ) =
        CustomFieldsSnippet(
            descriptors = mutableListOf(*descriptors),
            ignoreUndocumentedFields = false,
            snippetName = "slip-document"
        )

    class CustomFieldsSnippet(
        descriptors: MutableList<FieldDescriptor>,
        ignoreUndocumentedFields: Boolean = false,
        snippetName: String,
        attributes: MutableMap<String, Any> = Attributes.attributes(
            Attributes.key("title").value("title")
        )
    ) : AbstractFieldsSnippet(
        snippetName,
        descriptors,
        attributes,
        ignoreUndocumentedFields
    ) {

        override fun getContentType(operation: Operation) = operation.response.headers.contentType

        override fun getContent(operation: Operation) = operation.response.content!!
    }
}