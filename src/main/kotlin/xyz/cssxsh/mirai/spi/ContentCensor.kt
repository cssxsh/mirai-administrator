package xyz.cssxsh.mirai.spi

import net.mamoe.mirai.event.events.*

/**
 * 群消息审批
 */
public interface ContentCensor : ComparableService {

    /**
     * 审批群消息事件，
     * @return 返回 true 表示触发审计
     */
    public suspend fun handle(event: GroupMessageEvent): Boolean

    /**
     * 审批群戳一戳事件
     * @return 返回 true 表示触发审计
     */
    public suspend fun handle(event: NudgeEvent): Boolean
}