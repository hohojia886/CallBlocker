/**
 * Enum indicating the reason an incoming call was intercepted or allowed.
 */
package io.github.hohojia886.callblocker.data.db

enum class BlockReason {
    /** Intercepted due to missing or private Caller ID. */
    NO_NUMBER,

    /** Intercepted because the caller was not in the contacts list. */
    NON_CONTACT,

    /** Intercepted because the call originated outside active SIM card countries. */
    INTERNATIONAL,

    /** Intercepted because the number matched a rule in the custom block list. */
    BLACK_LIST,

    /** Call was allowed without triggering any blocking rules. */
    NONE
}
