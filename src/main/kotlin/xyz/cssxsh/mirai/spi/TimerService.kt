package xyz.cssxsh.mirai.spi

import net.mamoe.mirai.contact.*
import java.time.*

/**
 * 定时运行的服务
 */
public sealed interface TimerService<C : ContactOrBot> {

    /**
     * 运行的时间，为 null 时停止定时器
     */
    public fun moment(contact: C): LocalTime?

    /**
     * 运行定时器
     */
    public suspend fun run(contact: C)
}