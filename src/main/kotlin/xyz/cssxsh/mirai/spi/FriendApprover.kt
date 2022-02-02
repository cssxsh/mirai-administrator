package xyz.cssxsh.mirai.spi

import net.mamoe.mirai.event.events.*

/**
 * 好友审批
 */
public interface FriendApprover : ComparableService {

    /**
     * 好友请求事件审批
     */
    public suspend fun approve(event: NewFriendRequestEvent): ApproveStatus

    /**
     * 好友事件件审批
     */
    public suspend fun approve(event: FriendAddEvent): ApproveStatus
}