package xyz.cssxsh.mirai.spi

import net.mamoe.mirai.contact.*
import java.time.*

/**
 * 定时的服务, [run] 返回消息
 * @see BotTimingMessage
 * @see GroupTimerService
 */
public sealed interface TimerService<C : ContactOrBot, R> : ComparableService {

    /**
     * 运行的时间，为 null 时停止定时器
     * @see wait
     */
    @Deprecated(message = "设计不够良好", level = DeprecationLevel.ERROR, replaceWith = ReplaceWith("null"))
    public fun moment(contact: C): LocalTime? = null

    /**
     * 运行的时间，为 null 时停止定时器
     * @return 等待执行的时间，为 null 时停止定时器
     */
    public fun wait(contact: C): Long?

    /**
     * 运行定时器
     */
    public suspend fun run(contact: C): R
}