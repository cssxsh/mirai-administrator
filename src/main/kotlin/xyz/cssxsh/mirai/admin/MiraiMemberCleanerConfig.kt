package xyz.cssxsh.mirai.admin


public interface MiraiMemberCleanerConfig {
    public val check: Long

    public val last: Map<Long, Long>
}