package com.example.restdocssample.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project

class OpenApiPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.afterEvaluate {
            val generateOpenApi3Task = tasks.findByName("openapi3")
            generateOpenApi3Task?.doLast {
                // 여기에 추가적인 동작을 넣습니다.
                logger.info("This will run after openapi3 task!")
            }
        }
    }
}


open class OpenApiExtension {
    var title: String = "Member API"
    var description: String = "My API description"
    var tagDescriptionsPropertiesFile: String = "src/test/resources/tags-descriptions.yaml"
    var version: String = "0.1.0"
    var format: String = "yaml"
    // getSpringAppName() 함수의 구현은 별도로 처리되어야 합니다.
    val outputFileNamePrefix: String = "openapi3-"
}