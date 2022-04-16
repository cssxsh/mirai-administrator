package xyz.cssxsh.mirai.spi

import net.mamoe.mirai.contact.*


/**
 * 宵禁, [run] 返回指定群是否禁言
 */
public interface GroupCurfewTimer : GroupTimerService<Boolean?> {

    /**
     * 睡眠状态，为真时，定期器不工作
     */
    public fun sleep(contact: Group, state: Boolean? = null)
}