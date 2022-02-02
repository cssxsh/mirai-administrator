package xyz.cssxsh.mirai.spi

import net.mamoe.mirai.event.events.*

/**
 * 群成员审批
 */
public interface MemberApprover : ComparableService {

    /**
     * 群成员请求事件审批
     */
    public suspend fun approve(event: MemberJoinRequestEvent): ApproveStatus

    /**
     * 群成员事件审批
     */
    public suspend fun approve(event: MemberJoinEvent): ApproveStatus

}