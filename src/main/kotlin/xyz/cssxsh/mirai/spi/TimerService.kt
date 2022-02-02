package xyz.cssxsh.mirai.spi

import net.mamoe.mirai.contact.*
import java.time.*

/**
 * 定时运行的服务
 */
public sealed interface TimerService<C : ContactOrBot>  {

    public fun moment(contact: C): LocalTime?

    public suspend fun run(contact: C)
}