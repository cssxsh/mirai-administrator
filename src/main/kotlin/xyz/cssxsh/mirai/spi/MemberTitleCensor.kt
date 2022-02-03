package xyz.cssxsh.mirai.spi

import net.mamoe.mirai.contact.*

/**
 * 群成员头衔检查, [run] 返回检查方法
 */
public interface MemberTitleCensor : GroupTimerService<(NormalMember) -> String?>