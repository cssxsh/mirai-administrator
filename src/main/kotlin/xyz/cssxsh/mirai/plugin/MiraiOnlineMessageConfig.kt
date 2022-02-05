package xyz.cssxsh.mirai.plugin

public interface MiraiOnlineMessageConfig {

    public val duration: Long

    public val type: Type

    public val custom: String

    public enum class Type { XML, PLAIN, CUSTOM }
}