package xyz.cssxsh.mirai.admin

public interface MiraiContentCensorConfig {

    public val censorRegex: Sequence<Regex>

    public val censorTypes: Set<MiraiContentType>

    public val censorMute: Int
}