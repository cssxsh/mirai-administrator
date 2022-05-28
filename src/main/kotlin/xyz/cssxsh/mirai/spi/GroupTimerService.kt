package xyz.cssxsh.mirai.spi

import net.mamoe.mirai.contact.*

/**
 * 群定时运行的服务
 * @see GroupAllowTimer
 * @see GroupCurfewTimer
 * @see MemberCleaner
 * @see MemberNickCensor
 * @see MemberTitleCensor
 */
public sealed interface GroupTimerService<R> : TimerService<Group, R>