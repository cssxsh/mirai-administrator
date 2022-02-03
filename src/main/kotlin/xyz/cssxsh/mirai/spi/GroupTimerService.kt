package xyz.cssxsh.mirai.spi

import net.mamoe.mirai.contact.*

/**
 * 群定时运行的服务
 */
public sealed interface GroupTimerService<R> : TimerService<Group, R>