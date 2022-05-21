package xyz.cssxsh.mirai.admin

public interface MiraiContentCensorConfig {

    public val censorRegex: String

    public val censorTypes: Set<MiraiContentType>

    public val censorMute: Int
}