package xyz.cssxsh.mirai.plugin

import net.mamoe.mirai.console.command.CommandSender.Companion.toCommandSender
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.*
import xyz.cssxsh.mirai.plugin.command.*
import xyz.cssxsh.mirai.plugin.data.*
import xyz.cssxsh.mirai.spi.*

public object MiraiAutoApprover : FriendApprover, GroupApprover, MemberApprover,
    MiraiAutoApproverConfig by AdminSetting {

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

    public suspend fun handle(event: MessageEvent) {
        AdminContactCommand.runCatching {
            val original = (event.message.findIsInstance<QuoteReply>() ?: return)
                .source.originalMessage
                .contentToString()
            val id = ("""(?<=<)\d+""".toRegex().find(original)?.value ?: return).toLong()
            val accept = replyAccept.toRegex() in event.message.contentToString()
            val black = replyBlack.toRegex() in event.message.contentToString()

            event.toCommandSender().handle(id = id, accept = accept, black = black, message = original)
        }.onFailure { cause ->
            logger.error({ "handle contact request failure." }, cause)
        }
    }

    override val id: String = "default"
}