package xyz.cssxsh.mirai.spi

import net.mamoe.mirai.contact.*
import java.time.*

/**
 * 定时的服务, [run] 返回消息
 */
public sealed interface TimerService<C : ContactOrBot, R> : ComparableService {

    /**
     * 运行的时间，为 null 时停止定时器
     */
    public fun moment(contact: C): LocalTime?

    /**
     * 运行定时器
     */
    public suspend fun run(contact: C): R
}