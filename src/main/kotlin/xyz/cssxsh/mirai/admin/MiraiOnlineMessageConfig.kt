package xyz.cssxsh.mirai.admin

import kotlinx.serialization.*
import net.mamoe.mirai.console.permission.*

@PublishedApi
internal interface MiraiOnlineMessageConfig {

    val duration: Long

    val type: Type

    val custom: String

    val permission: Permission

    @Serializable
    enum class Type { XML, PLAIN, CUSTOM }
}