package xyz.cssxsh.mirai.admin

import kotlinx.serialization.*

public interface MiraiOnlineMessageConfig {

    public val duration: Long

    public val type: Type

    public val custom: String

    @Serializable
    public enum class Type { XML, PLAIN, CUSTOM }
}