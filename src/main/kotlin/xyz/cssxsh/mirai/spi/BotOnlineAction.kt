package xyz.cssxsh.mirai.spi

import net.mamoe.mirai.*

/**
 * 机器人上线事件会触发的服务
 */
public interface BotOnlineAction : ComparableService {
    /**
     * 触发函数
     */
    public suspend fun run(bot: Bot)
}