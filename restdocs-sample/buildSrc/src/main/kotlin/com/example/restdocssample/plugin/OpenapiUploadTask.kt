package com.example.restdocssample.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.core.buildPacket
import io.ktor.utils.io.core.writeFully
import kotlinx.coroutines.runBlocking
import java.io.File
import java.io.InputStream

class OpenapiUploadTask : Plugin<Project> {
    override fun apply(project: Project) {
        project.task("yun") {
            doLast {
                // 여기에 doLast 로직을 넣습니다.
                runBlocking {
                    val client = HttpClient {
                        install(Logging) {
                            logger = Logger.DEFAULT
                            level = LogLevel.ALL
                        }
                        install(ContentNegotiation) {
                            json()
                        }
                    }

                    val openApiFileName = "openapi3-test-api.yaml"
                    val openApiFile = File("build/api-spec/$openApiFileName")

                    val uploadResponse = client.post("http://localhost:3000/upload") {
                        this.setBody(
                            MultiPartFormDataContent(
                                parts = formData {
                                    appendInput(
                                        key = "file",
                                        headers = Headers.build { append(HttpHeaders.ContentDisposition, "filename=${openApiFile.name}") },
                                        block = { buildPacket { writeFully(openApiFile.readBytes()) } }
                                    )
                                }
                            )
                        )
                    }
                    println(uploadResponse.status)
                    val restartResponse = client.get("http://localhost:3000/restart")
                    println(restartResponse.status)
                    client.close()
                }
            }
        }
    }
}
