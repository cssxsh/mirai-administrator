package xyz.cssxsh.mirai.plugin


public interface MiraiCurfewTimerConfig {
    public val check: Long

    public val muted: Map<Long, LocalTimeRange>

    public val sleep: MutableMap<Long, Long>
}