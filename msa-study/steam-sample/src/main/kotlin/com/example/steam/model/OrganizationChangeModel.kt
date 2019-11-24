package com.example.steam.model

data class OrganizationChangeModel(
        val className: String,
        val action: String,
        val orgId: String
) {
}