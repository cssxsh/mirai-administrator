package xyz.cssxsh.mirai.plugin

public interface MiraiOnlineMessageConfig {

    public val duration: Long

    public val onlineMessageType: Type

    public enum class Type { XML, PLAIN }
}