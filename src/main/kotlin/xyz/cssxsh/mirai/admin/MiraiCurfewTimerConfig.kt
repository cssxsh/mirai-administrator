package xyz.cssxsh.mirai.admin


public interface MiraiCurfewTimerConfig {
    public val check: Long

    public val muted: Map<Long, LocalTimeRange>

    public val sleep: MutableMap<Long, Long>
}