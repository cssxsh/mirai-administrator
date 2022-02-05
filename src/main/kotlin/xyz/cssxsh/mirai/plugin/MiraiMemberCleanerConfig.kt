package xyz.cssxsh.mirai.plugin


public interface MiraiMemberCleanerConfig {
    public val check: Long

    public val last: Map<Long, Long>
}