package xyz.cssxsh.mirai.admin

import kotlinx.serialization.*
import net.mamoe.mirai.console.permission.*

public interface MiraiOnlineMessageConfig {

    public val duration: Long

    public val type: Type

    public val custom: String

    public val permission: Permission

    @Serializable
    public enum class Type { XML, PLAIN, CUSTOM }
}