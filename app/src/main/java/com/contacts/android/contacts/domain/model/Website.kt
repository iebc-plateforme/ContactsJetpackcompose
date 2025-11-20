package com.contacts.android.contacts.domain.model

import androidx.annotation.StringRes
import com.contacts.android.contacts.R

data class Website(
    val url: String,
    val type: WebsiteType = WebsiteType.OTHER
)

enum class WebsiteType(@StringRes val displayNameRes: Int) {
    HOME(R.string.type_home),
    WORK(R.string.type_work),
    BLOG(R.string.website_type_blog),
    PORTFOLIO(R.string.website_type_portfolio),
    OTHER(R.string.type_other),
    CUSTOM(R.string.type_custom)
}
