package xyz.cssxsh.mirai.spi

import net.mamoe.mirai.contact.*


/**
 * 宵禁, [run] 返回指定群是否禁言
 */
public interface GroupCurfewTimer : GroupTimerService<Boolean?> {

    /**
     * 设置睡眠，定期器不工作一段时间
     */
    public fun ignore(contact: Group)
}