package dev.stackframe.armeria.kotlin.grpc.handler

import com.linecorp.armeria.client.WebClient
import com.linecorp.armeria.client.kotlin.execute
import com.linecorp.armeria.client.logging.LoggingClient
import com.linecorp.armeria.common.HttpMethod
import com.linecorp.armeria.common.HttpRequest
import com.linecorp.armeria.common.RequestContext
import com.linecorp.armeria.common.kotlin.asCoroutineContext
import com.linecorp.armeria.common.logging.LogLevel
import com.linecorp.armeria.common.logging.LogWriter
import com.linecorp.armeria.server.ServiceRequestContext
import dev.stackframe.armeria.kotlin.grpc.FirstGrpcServiceGrpcKt
import dev.stackframe.armeria.kotlin.grpc.Service
import dev.stackframe.armeria.kotlin.grpc.dummyResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.future.await
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class FirstGrpcServiceHandler : FirstGrpcServiceGrpcKt.FirstGrpcServiceCoroutineImplBase() {
    override suspend fun dummy(request: Service.DummyRequest): Service.DummyResponse {
        val ctx = RequestContext.current<ServiceRequestContext>()

        log.info("out request: ${ctx.rpcRequest()}")

        CoroutineScope(Dispatchers.Default + ctx.asCoroutineContext()).launch {
            val coroutineCtx = RequestContext.current<ServiceRequestContext>()
            log.info("default in request: ${coroutineCtx.rpcRequest()}")
        }.join()

        withContext(Dispatchers.IO) {
            val coroutineCtx = RequestContext.current<ServiceRequestContext>()
            log.info("IO in request: ${coroutineCtx.rpcRequest()}")
        }

        val response = client.execute(
            HttpRequest.of(HttpMethod.GET, "/err")
        ).aggregate().await()

        log.info("response: $response")

        WebClient
            .builder("https://stackframe.dev")
            .decorator {
                LoggingClient
                    .builder()
                    .logWriter(
                        LogWriter
                            .builder()
                            .successfulResponseLogLevel(LogLevel.INFO)
                            .requestLogLevel(LogLevel.INFO)
                            .build()
                    )
                    .build(it)
            }
            .build()
            .asRestClient()
            .get("/")
            .execute<String>()

        return dummyResponse {
            text = request.text
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(this.javaClass)

        private val client = WebClient
            .builder("https://stackframe.dev")
            .contextCustomizer {

            }
            .decorator {
                LoggingClient
                    .builder()
                    .logWriter(
                        LogWriter
                            .builder()
                            .successfulResponseLogLevel(LogLevel.INFO)
                            .requestLogLevel(LogLevel.INFO)
                            .build()
                    )
                    .build(it)
            }
            .build()
    }
}
