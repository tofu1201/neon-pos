package com.yourcompany.pos.data.remote

import io.ktor.server.routing.Routing
import io.ktor.server.http.content.staticResources

fun test(routing: Routing) {
    routing.staticResources("/", "web")
}
