package com.codeforces.app.ui.components

import com.codeforces.app.data.api.UserDto

private const val DIRECT_USERPIC_PREFIX = "https://userpic.codeforces.org/"
private const val INSECURE_DIRECT_USERPIC_PREFIX = "http://userpic.codeforces.org/"
private const val CODEFORCES_USERPIC_PROXY_PREFIX =
    "https://codeforces.com/userpic.codeforces.org/"

/**
 * Returns a Codeforces profile image URL that is served through codeforces.com.
 *
 * The API's direct userpic.codeforces.org URLs intermittently return 503 for
 * otherwise valid images. Codeforces' own profile pages use this proxy route.
 */
fun UserDto.profileImageUrl(): String? {
    val source = titlePhoto?.trim()?.takeIf { it.isNotEmpty() }
        ?: avatar?.trim()?.takeIf { it.isNotEmpty() }
        ?: return null
    return when {
        source.startsWith(DIRECT_USERPIC_PREFIX) ->
            CODEFORCES_USERPIC_PROXY_PREFIX + source.removePrefix(DIRECT_USERPIC_PREFIX)
        source.startsWith(INSECURE_DIRECT_USERPIC_PREFIX) ->
            CODEFORCES_USERPIC_PROXY_PREFIX + source.removePrefix(INSECURE_DIRECT_USERPIC_PREFIX)
        else -> source
    }
}
