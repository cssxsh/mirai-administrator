package xyz.cssxsh.mirai.spi

import net.mamoe.mirai.contact.*

/**
 * 宵禁
 */
public interface GroupCurfewTimer : GroupTimerService<Long?> {

    /**
     * @return 指定群禁言时间（毫秒），null 不禁言
     */
    override suspend fun run(contact: Group): Long?
}