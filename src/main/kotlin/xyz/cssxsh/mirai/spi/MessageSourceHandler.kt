package xyz.cssxsh.mirai.spi

import net.mamoe.mirai.contact.*
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.message.data.*

/**
 * [MessageSource] 解析获取
 */
public interface MessageSourceHandler : ComparableService {

    /**
     * 根据指定参数 获取 最后的 [MessageSource]
     * @param contact 指定的用户
     * @param event 触发操作的事件，用于获取相关信息
     */
    public fun find(contact: Contact?, event: MessageEvent?): MessageSource?
}