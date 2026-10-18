package dev.stackframe.armeria.kotlin.configuration

import com.linecorp.armeria.server.grpc.GrpcService
import com.linecorp.armeria.spring.ArmeriaServerConfigurator
import dev.stackframe.armeria.kotlin.decorator.RequestIdDecoratingService
import dev.stackframe.armeria.kotlin.rest.controller.ArmeriaAnnotationServiceInterface
import io.grpc.BindableService
import io.grpc.protobuf.services.ProtoReflectionServiceV1
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class ArmeriaServiceConfiguration {
    @Bean
    fun armeriaServerConfigurator(
        annotatedServices: List<ArmeriaAnnotationServiceInterface>,
        grpcServices: List<BindableService>,
    ): ArmeriaServerConfigurator {
        return ArmeriaServerConfigurator { serverBuilder ->
            // Restrict eventloop worker thread count to 1 to see requests processed async and non-blocking
            serverBuilder.workerGroup(1)

            for (annotatedService in annotatedServices) {
                serverBuilder
                    .annotatedService("/api/rest", annotatedService)
            }

            if (grpcServices.isNotEmpty()) {
                serverBuilder
                    .service(
                        GrpcService
                            .builder()
                            .let {
                                for (grpcService in grpcServices) {
                                    it.addService(grpcService)
                                }
                                it.addService(ProtoReflectionServiceV1.newInstance())
                            }
                            .enableUnframedRequests(true)
                            .build()
                )
            }
            serverBuilder.decorator {
                RequestIdDecoratingService(it)
            }
        }
    }
}
