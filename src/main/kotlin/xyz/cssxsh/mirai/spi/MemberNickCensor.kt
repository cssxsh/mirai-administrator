package xyz.cssxsh.mirai.spi

import net.mamoe.mirai.contact.*

/**
 * 群成员昵称检查, [run] 返回检查方法
 */
public interface MemberNickCensor : GroupTimerService<(NormalMember) -> String?>