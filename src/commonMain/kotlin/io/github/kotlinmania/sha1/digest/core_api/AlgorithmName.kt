package io.github.kotlinmania.sha1.digest.core_api

/**
 * Mirrors the upstream `AlgorithmName` trait method shape.
 */
interface AlgorithmName {
    fun writeAlgName(formatter: StringBuilder)
}
