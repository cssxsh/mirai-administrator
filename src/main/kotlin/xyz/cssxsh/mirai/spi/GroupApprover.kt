package xyz.cssxsh.mirai.spi

import net.mamoe.mirai.event.events.*

/**
 * 加群审批
 */
public interface GroupApprover : ComparableService {

    /**
     * 加群请求事件审批
     */
    public suspend fun approve(event: BotInvitedJoinGroupRequestEvent): ApproveResult

    /**
     * 加群事件审批
     */
    public suspend fun approve(event: BotJoinGroupEvent): ApproveResult
}