package dev.stackframe.armeria.kotlin.rest.controller

import com.linecorp.armeria.server.annotation.Get
import com.linecorp.armeria.server.annotation.ProducesJson
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Controller

@Controller
class FirstAnnotatedService : ArmeriaAnnotationServiceInterface {
    @Get("/annotated1")
    @ProducesJson
    suspend fun dummy(): Response {
        log.info("Hello 1?")
        return Response("Hello 1")
    }

    data class Response(val result: String)

    companion object {
        private val log = LoggerFactory.getLogger(this.javaClass)
    }
}
