package xyz.cssxsh.mirai.spi

import net.mamoe.mirai.event.events.*

/**
 * 好友审批
 */
public interface FriendApprover : ComparableService {

    /**
     * 好友请求事件审批
     */
    public suspend fun approve(event: NewFriendRequestEvent): ApproveResult

    /**
     * 好友添加事件审批
     */
    public suspend fun approve(event: FriendAddEvent): ApproveResult
}