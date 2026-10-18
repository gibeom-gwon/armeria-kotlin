package dev.stackframe.armeria.kotlin.grpc.handler

import com.linecorp.armeria.common.RequestContext
import com.linecorp.armeria.common.kotlin.asCoroutineContext
import com.linecorp.armeria.server.ServiceRequestContext
import dev.stackframe.armeria.kotlin.grpc.SecondGrpcServiceGrpc
import dev.stackframe.armeria.kotlin.grpc.Service
import io.grpc.stub.StreamObserver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.future.asCompletableFuture
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class SecondGrpcServiceHandler : SecondGrpcServiceGrpc.SecondGrpcServiceImplBase() {
    override fun dummy(
        request: Service.DummyRequest,
        responseObserver: StreamObserver<Service.DummyResponse>
    ) {
        val ctx = RequestContext.current<ServiceRequestContext>()

        log.info("out request: ${ctx.rpcRequest()}")

        CoroutineScope(Dispatchers.Default + ctx.asCoroutineContext()).launch {
            val coroutineCtx = RequestContext.current<ServiceRequestContext>()
            log.info("default in request: ${coroutineCtx.rpcRequest()}")
        }.asCompletableFuture().thenRun {
            CoroutineScope(Dispatchers.IO + ctx.asCoroutineContext()).launch {
                val coroutineCtx = RequestContext.current<ServiceRequestContext>()
                log.info("IO in request: ${coroutineCtx.rpcRequest()}")
            }
        }

        CoroutineScope(ctx.eventLoop().asCoroutineDispatcher()).launch {
            val coroutineCtx = RequestContext.current<ServiceRequestContext>()
            log.info("coroutineDispatcher in request: ${coroutineCtx.rpcRequest()}")
        }
        responseObserver.onNext(
            Service
                .DummyResponse
                .newBuilder()
                .apply {
                    text = request.text
                }
                .build()
        )

        responseObserver.onNext(
            Service
                .DummyResponse
                .newBuilder()
                .apply {
                    text = request.text
                }
                .build()
        )

        responseObserver.onCompleted()
    }

    companion object {
        private val log = LoggerFactory.getLogger(this.javaClass)
    }
}
