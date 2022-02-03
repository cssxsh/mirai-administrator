package xyz.cssxsh.mirai.spi

import net.mamoe.mirai.contact.*

/**
 * 群成员清理, [run] 返回需要清理的成员及理由
 */
public interface MemberCleaner : GroupTimerService<List<Pair<NormalMember, String>>>