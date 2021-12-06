package com.bloidonia

import io.micronaut.core.async.publisher.Publishers
import io.micronaut.http.HttpRequest
import io.micronaut.http.MediaType
import io.micronaut.http.MutableHttpResponse
import io.micronaut.http.annotation.Filter
import io.micronaut.http.client.ProxyHttpClient
import io.micronaut.http.filter.HttpServerFilter
import io.micronaut.http.filter.ServerFilterChain
import io.micronaut.runtime.Micronaut.build
import org.reactivestreams.Publisher

@Filter("/**")
class Proxy(private val client: ProxyHttpClient) : HttpServerFilter {

    // This uses a mock server created by visiting https://beeceptor.com/
    override fun doFilter(request: HttpRequest<*>, chain: ServerFilterChain?): Publisher<MutableHttpResponse<*>> {
        return Publishers.map(
            client.proxy(
                request
                    .mutate()
                    .uri { uri ->
                        uri.scheme("https").host("woowoo.free.beeceptor.com").replacePath(request.path)
                    }
                    .headers { h -> h.set("Forwarded", "by=localhost").set("Host", "woowoo.free.beeceptor.com") }
                    .accept(MediaType.APPLICATION_JSON_TYPE)

            )
        ) { response ->
            response
        }
    }
}

fun main(args: Array<String>) {
    build()
        .args(*args)
        .packages("com.bloidonia")
        .start()
}

