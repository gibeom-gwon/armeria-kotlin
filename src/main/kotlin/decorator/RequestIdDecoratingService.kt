package dev.stackframe.armeria.kotlin.decorator

import com.linecorp.armeria.common.HttpRequest
import com.linecorp.armeria.common.HttpResponse
import com.linecorp.armeria.common.HttpStatus
import com.linecorp.armeria.server.HttpService
import com.linecorp.armeria.server.ServiceRequestContext
import com.linecorp.armeria.server.SimpleDecoratingHttpService
import dev.stackframe.armeria.kotlin.context.ServerAttributeKeys.REQUEST_ID
import kotlin.concurrent.atomics.AtomicLong
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.incrementAndFetch
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.future.asCompletableFuture
import org.slf4j.LoggerFactory

@OptIn(ExperimentalAtomicApi::class)
class RequestIdDecoratingService(
    delegate: HttpService,
) : SimpleDecoratingHttpService(delegate) {
    override fun serve(
        ctx: ServiceRequestContext,
        req: HttpRequest
    ): HttpResponse {
        val currentRequestId = requestId.incrementAndFetch()

        ctx.setAttr(REQUEST_ID, currentRequestId)

        val check = CoroutineScope(ctx.eventLoop().asCoroutineDispatcher()).async {
            // External authentication
            delay(1000.milliseconds)

            if (true) {
                unwrap().serve(ctx, req)
            } else {
                HttpResponse.of(HttpStatus.UNAUTHORIZED)
            }
        }.asCompletableFuture()

        return HttpResponse.of(check)
    }

    companion object {
        private val log = LoggerFactory.getLogger(this.javaClass)
        private val requestId = AtomicLong(100000000)
    }
}
