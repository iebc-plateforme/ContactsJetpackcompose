package com.contacts.android.contacts.domain.model

data class Website(
    val url: String,
    val type: WebsiteType = WebsiteType.OTHER
)

enum class WebsiteType(val displayName: String) {
    HOME("Home"),
    WORK("Work"),
    BLOG("Blog"),
    PORTFOLIO("Portfolio"),
    OTHER("Other"),
    CUSTOM("Custom")
}
