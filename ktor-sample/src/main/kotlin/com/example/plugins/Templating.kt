package com.example.plugins

import io.ktor.server.html.*
import kotlinx.html.*
import kotlinx.css.*
import io.ktor.http.*
import io.ktor.server.response.*
import freemarker.cache.*
import io.ktor.server.freemarker.*
import com.github.mustachejava.DefaultMustacheFactory
import io.ktor.server.mustache.Mustache
import io.ktor.server.mustache.MustacheContent
import io.pebbletemplates.pebble.loader.ClasspathLoader
import io.ktor.server.pebble.*
import io.ktor.server.thymeleaf.Thymeleaf
import io.ktor.server.thymeleaf.ThymeleafContent
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver
import io.ktor.server.velocity.*
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader
import org.apache.velocity.runtime.RuntimeConstants
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureTemplating() {


    install(FreeMarker) {
        templateLoader = ClassTemplateLoader(this::class.java.classLoader, "templates")
    }
    install(Mustache) {
        mustacheFactory = DefaultMustacheFactory("templates/mustache")
    }
    install(Pebble) {
        loader(ClasspathLoader().apply {
            prefix = "templates"
        })
    }
    install(Thymeleaf) {
        setTemplateResolver(ClassLoaderTemplateResolver().apply {
            prefix = "templates/thymeleaf/"
            suffix = ".html"
            characterEncoding = "utf-8"
        })
    }
    install(Velocity) {
        setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath")
        setProperty("classpath.resource.loader.class", ClasspathResourceLoader::class.java.name)
    }
    routing {
        get("/html-dsl") {
            call.respondHtml {
                body {
                    h1 { +"HTML" }
                    ul {
                        for (n in 1..10) {
                            li { +"$n" }
                        }
                    }
                }
            }
        }
        get("/styles.css") {
            call.respondCss {
                body {
                    backgroundColor = Color.darkBlue
                    margin(0.px)
                }
                rule("h1.page-title") {
                    color = Color.white
                }
            }
        }

        get("/html-css-dsl") {
            call.respondHtml {
                head {
                    link(rel = "stylesheet", href = "/styles.css", type = "text/css")
                }
                body {
                    h1(classes = "page-title") {
                        +"Hello from Ktor!"
                    }
                }
            }
        }
        get("/html-freemarker") {
            call.respond(FreeMarkerContent("index.ftl", mapOf("data" to IndexData(listOf(1, 2, 3))), ""))
        }
        get("/html-mustache") {
            call.respond(MustacheContent("index.hbs", mapOf("user" to MustacheUser(1, "user1"))))
        }
        get("/pebble-index") {
            val sampleUser = PebbleUser(1, "John")
            call.respond(PebbleContent("pebble-index.html", mapOf("user" to sampleUser)))
        }
        get("/html-thymeleaf") {
            call.respond(ThymeleafContent("index", mapOf("user" to ThymeleafUser(1, "user1"))))
        }
        get("/index") {
            val sampleUser = VelocityUser(1, "John")
            call.respond(VelocityContent("templates/index.vl", mapOf("user" to sampleUser)))
        }
    }
}

suspend inline fun ApplicationCall.respondCss(builder: CSSBuilder.() -> Unit) {
    this.respondText(CSSBuilder().apply(builder).toString(), ContentType.Text.CSS)
}

data class IndexData(val items: List<Int>)
data class MustacheUser(val id: Int, val name: String)
data class PebbleUser(val id: Int, val name: String)
data class ThymeleafUser(val id: Int, val name: String)
data class VelocityUser(val id: Int, val name: String)
