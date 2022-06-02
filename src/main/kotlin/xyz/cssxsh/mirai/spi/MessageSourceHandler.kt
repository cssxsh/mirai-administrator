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
     * @see target
     * @see from
     * @see quote
     */
    @Deprecated("接口定义不明确", ReplaceWith("null"))
    public fun find(contact: Contact?, event: MessageEvent?): MessageSource? = null

    /**
     * 根据 曾发送消息的目标 获取 最后的 [MessageSource]
     * @param contact 机器人曾发送消息的目标
     */
    @Suppress("DEPRECATION")
    public fun target(contact: Contact): MessageSource? = find(contact = contact, event = null)

    /**
     * 根据 曾发送消息的群员 获取 最后的 [MessageSource]
     * @param member 曾发送消息的群员
     */
    @Suppress("DEPRECATION")
    public fun from(member: Member): MessageSource? = find(contact = member, event = null)

    /**
     * 根据 消息事件 获取 最后的 [MessageSource]
     * @param event 曾收到的的消息事件
     */
    @Suppress("DEPRECATION")
    public fun quote(event: MessageEvent): MessageSource? = find(contact = null, event = event)
}