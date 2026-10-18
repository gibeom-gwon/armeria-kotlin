package dev.stackframe.armeria.kotlin.context

import io.netty.util.AttributeKey

object ServerAttributeKeys {
    val REQUEST_ID: AttributeKey<Long> = AttributeKey.valueOf(ServerAttributeKeys.javaClass, "REQUEST_ID")
}
