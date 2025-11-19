package com.contacts.android.contacts.domain.model

data class InstantMessage(
    val handle: String, // Username/phone number/ID
    val protocol: IMProtocol = IMProtocol.OTHER
)

enum class IMProtocol(val displayName: String) {
    WHATSAPP("WhatsApp"),
    TELEGRAM("Telegram"),
    SIGNAL("Signal"),
    SKYPE("Skype"),
    DISCORD("Discord"),
    SLACK("Slack"),
    MESSENGER("Messenger"),
    INSTAGRAM("Instagram"),
    SNAPCHAT("Snapchat"),
    LINE("LINE"),
    VIBER("Viber"),
    WECHAT("WeChat"),
    QQ("QQ"),
    ICQ("ICQ"),
    AIM("AIM"),
    JABBER("Jabber"),
    OTHER("Other"),
    CUSTOM("Custom")
}
