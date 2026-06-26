package io.github.kotlinmania.sha1.digest.const_oid

/**
 * Minimal value object for the OID constant used by optional feature wiring.
 */
class ObjectIdentifier private constructor(private val value: String) {
    companion object {
        fun newUnwrap(value: String): ObjectIdentifier = ObjectIdentifier(value)
    }

    override fun toString(): String = value
}
