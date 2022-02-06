package xyz.cssxsh.mirai.plugin

import net.mamoe.mirai.event.events.*
import xyz.cssxsh.mirai.plugin.data.*
import xyz.cssxsh.mirai.spi.*

public object MiraiAutoApprover : FriendApprover, GroupApprover, MemberApprover,
    MiraiAutoApproverConfig by AdminAutoApproverConfig {
    override val level: Int = 0
    override val id: String = "default-approver"

    override suspend fun approve(event: NewFriendRequestEvent): ApproveResult {
        AdminRequestEventData += event
        event.bot.owner().sendMessage(message = event.render(accept = autoFriendAccept))
        return if (autoFriendAccept) ApproveResult.Accept else ApproveResult.Ignore
    }

    override suspend fun approve(event: BotInvitedJoinGroupRequestEvent): ApproveResult {
        AdminRequestEventData += event
        event.bot.owner().sendMessage(message = event.render(accept = autoGroupAccept))
        return if (autoGroupAccept) ApproveResult.Accept else ApproveResult.Ignore
    }

    override suspend fun approve(event: MemberJoinRequestEvent): ApproveResult {
        AdminRequestEventData += event
        event.bot.owner().sendMessage(message = event.render(accept = autoGroupAccept))
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