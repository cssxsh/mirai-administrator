package xyz.cssxsh.mirai.admin

@PublishedApi
internal interface MiraiContentCensorConfig {

    val censorRegex: Sequence<Regex>

    val censorTypes: Set<MiraiContentType>

    val censorMute: Int
}