package com.example.restdocssample.plugin

import com.epages.restdocs.apispec.gradle.ApiSpecTask
import com.epages.restdocs.apispec.gradle.OpenApi3Extension
import com.epages.restdocs.apispec.gradle.OpenApi3Task
import com.epages.restdocs.apispec.gradle.OpenApiBaseExtension
import com.epages.restdocs.apispec.gradle.OpenApiBaseTask
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project

class OpenApiPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.afterEvaluate {
//            val generateOpenApi3Task = tasks.findByName("openapi3") as OpenApiBaseExtension


        }
    }
}