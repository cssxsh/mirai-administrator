package xyz.cssxsh.mirai.admin

import net.mamoe.mirai.event.events.*
import xyz.cssxsh.mirai.admin.data.*
import xyz.cssxsh.mirai.spi.*

@PublishedApi
internal object MiraiAutoApprover : FriendApprover, GroupApprover, MemberApprover,
    MiraiAutoApproverConfig by AdminAutoApproverConfig {
    override val level: Int = 0
    override val id: String = "default-approver"

    override suspend fun approve(event: NewFriendRequestEvent): ApproveResult {
        if (event.fromId == AdminSetting.owner) return ApproveResult.Accept
        AdminRequestEventData += event
        if (remindFriendAccept) event.bot.owner().sendMessage(message = event.render(accept = autoFriendAccept))
        return if (autoFriendAccept) ApproveResult.Accept else ApproveResult.Ignore
    }

    override suspend fun approve(event: BotInvitedJoinGroupRequestEvent): ApproveResult {
        if (event.invitorId == AdminSetting.owner) return ApproveResult.Accept
        AdminRequestEventData += event
        if (remindGroupAccept) event.bot.owner().sendMessage(message = event.render(accept = autoGroupAccept))
        return if (autoGroupAccept) ApproveResult.Accept else ApproveResult.Ignore
    }

    override suspend fun approve(event: MemberJoinRequestEvent): ApproveResult {
        if (event.fromId == AdminSetting.owner) return ApproveResult.Accept
        AdminRequestEventData += event
        if (remindMemberAccept) event.bot.owner().sendMessage(message = event.render(accept = autoMemberAccept))
        return if (autoMemberAccept) ApproveResult.Accept else ApproveResult.Ignore
    }

    override suspend fun approve(event: FriendAddEvent): ApproveResult {
        AdminRequestEventData -= event
        return ApproveResult.Ignore
    }

    override suspend fun approve(event: BotJoinGroupEvent): ApproveResult {
        AdminRequestEventData -= event
        return ApproveResult.Ignore
    }

    override suspend fun approve(event: MemberJoinEvent): ApproveResult {
        AdminRequestEventData -= event
        return ApproveResult.Ignore
    }
}