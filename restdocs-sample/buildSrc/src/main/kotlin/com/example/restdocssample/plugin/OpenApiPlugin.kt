package com.example.restdocssample.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.jetbrains.kotlin.konan.properties.Properties
import org.yaml.snakeyaml.Yaml
import java.io.File
import java.io.InputStream

class OpenApiPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        // Extension을 만들어서 더 많은 설정을 할 수 있습니다.
        project.extensions.create(OpenApiExtension.taskName, OpenApiExtension::class.java, project)

    }
}

open class OpenApiExtension(project: Project) {
    companion object {
        const val taskName = "OpenApiPlugin"
    }

    val serverUrl = "http://localhost:2222"
    val serverDescription = "Sandbox server"

    val format = "yml"

    val version: String by lazy {
        "${getCurrentGitBranch()}-${getProperty(project)}"
    }

    val title: String by lazy {
        getSpringAppName()
    }

    val outputFileNamePrefix: String by lazy {
        "openapi3-${getSpringAppName()}"
    }

    private fun getCurrentGitBranch(): String {
        val process = Runtime.getRuntime().exec("git rev-parse --abbrev-ref HEAD")
        return process.inputStream.reader().readText().trim()
    }

    private fun getProperty(project: Project): String {
        val properties = Properties()
        val inputStream = project.rootProject.file("gradle.properties").inputStream()
        properties.load(inputStream)
        return properties.getProperty("spring-boot")
    }

    private fun getSpringAppName(): String {
        val inputStream: InputStream = File("src/main/resources/application.yml").inputStream()
        val yaml = Yaml().load<Map<String, Any>>(inputStream)

        val springMap = yaml["spring"] as Map<*, *>
        val applicationMap = springMap["application"] as Map<*, *>
        return applicationMap["name"] as String
    }


}
