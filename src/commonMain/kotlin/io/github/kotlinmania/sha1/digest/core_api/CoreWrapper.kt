package io.github.kotlinmania.sha1.digest.core_api

/**
 * Lightweight local placeholder for the upstream `CoreWrapper` type.
 *
 * The Rust crate uses this as a composition wrapper around core traits. In this
 * port, it is represented as a simple marker class for source-shape parity.
 */
class CoreWrapper<T>(val core: T)
