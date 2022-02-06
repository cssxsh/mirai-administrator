package xyz.cssxsh.mirai.plugin

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import net.mamoe.mirai.*
import net.mamoe.mirai.console.command.CommandSender.Companion.toCommandSender
import net.mamoe.mirai.console.permission.*
import net.mamoe.mirai.console.permission.PermissionService.Companion.cancel
import net.mamoe.mirai.console.permission.PermissionService.Companion.hasPermission
import net.mamoe.mirai.console.permission.PermissionService.Companion.permit
import net.mamoe.mirai.console.util.ContactUtils.render
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.event.*
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.*
import xyz.cssxsh.mirai.plugin.command.*
import xyz.cssxsh.mirai.spi.*
import java.time.*
import kotlin.coroutines.*

/**
 * 事件监听及定时器
 */
public object MiraiAdministrator : SimpleListenerHost() {

    override fun handleException(context: CoroutineContext, exception: Throwable) {
        when (exception) {
            is CancellationException -> {
                // ...
            }
            is ExceptionInEventHandlerException -> {
                logger.warning({ "MiraiAdministrator with ${exception.event}" }, exception.cause)
            }
            else -> {
                logger.warning({ "MiraiAdministrator" }, exception)
            }
        }
    }

    // region Approver

    @EventHandler
    internal suspend fun MemberJoinRequestEvent.handle() {
        for (approver in ComparableService<MemberApprover>()) {
            try {
                when (val status = approver.approve(event = this)) {
                    ApproveResult.Accept -> accept()
                    is ApproveResult.Reject -> reject(blackList = status.black, message = status.message)
                    ApproveResult.Ignore -> continue
                }
                break
            } catch (cause: Throwable) {
                logger.warning({ "$approver 审核 $this 失败" }, cause)
                continue
            }
        }
    }

    @EventHandler
    internal suspend fun MemberJoinEvent.handle() {
        for (approver in ComparableService<MemberApprover>()) {
            try {
                when (val status = approver.approve(event = this)) {
                    ApproveResult.Accept -> Unit
                    is ApproveResult.Reject -> member.kick(message = status.message, block = status.black)
                    ApproveResult.Ignore -> continue
                }
                break
            } catch (cause: Throwable) {
                logger.warning({ "$approver 审核 $this 失败" }, cause)
                continue
            }
        }
    }

    @EventHandler
    internal suspend fun NewFriendRequestEvent.handle() {
        for (approver in ComparableService<FriendApprover>()) {
            try {
                when (val status = approver.approve(event = this)) {
                    ApproveResult.Accept -> accept()
                    is ApproveResult.Reject -> reject(blackList = status.black)
                    ApproveResult.Ignore -> continue
                }
                break
            } catch (cause: Throwable) {
                logger.warning({ "$approver 审核 $this 失败" }, cause)
                continue
            }
        }
    }

    @EventHandler
    internal suspend fun FriendAddEvent.handle() {
        for (approver in ComparableService<FriendApprover>()) {
            try {
                when (val status = approver.approve(event = this)) {
                    ApproveResult.Accept -> Unit
                    is ApproveResult.Reject -> {
                        friend.sendMessage(message = status.message)
                        friend.delete()
                    }
                    ApproveResult.Ignore -> continue
                }
                break
            } catch (cause: Throwable) {
                logger.warning({ "$approver 审核 $this 失败" }, cause)
                continue
            }
        }
    }

    @EventHandler
    internal suspend fun BotInvitedJoinGroupRequestEvent.handle() {
        for (approver in ComparableService<GroupApprover>()) {
            try {
                when (val status = approver.approve(event = this)) {
                    ApproveResult.Accept -> accept()
                    is ApproveResult.Reject -> {
                        invitor?.sendMessage(message = status.message)
                        ignore()
                    }
                    ApproveResult.Ignore -> continue
                }
                break
            } catch (cause: Throwable) {
                logger.warning({ "$approver 审核 $this 失败" }, cause)
                continue
            }
        }
    }

    @EventHandler
    internal suspend fun BotJoinGroupEvent.handle() {
        for (approver in ComparableService<GroupApprover>()) {
            try {
                when (val status = approver.approve(event = this)) {
                    ApproveResult.Accept -> Unit
                    is ApproveResult.Reject -> {
                        @OptIn(MiraiExperimentalApi::class)
                        if (this is BotJoinGroupEvent.Invite) {
                            invitor.sendMessage(message = status.message)
                        }
                        group.quit()
                    }
                    ApproveResult.Ignore -> continue
                }
                break
            } catch (cause: Throwable) {
                logger.warning({ "$approver 审核 $this 失败" }, cause)
                continue
            }
        }
    }

    @EventHandler// XXX: AdminContactCommand ...
    internal suspend fun MessageEvent.approve() {
        if (toCommandSender().hasPermission(AdminContactCommand.permission).not()) return
        val original = (message.findIsInstance<QuoteReply>() ?: return)
            .source.originalMessage
            .contentToString()
        val id = ("""(?<=with <)\d+""".toRegex().find(original)?.value ?: return).toLong()
        val accept = MiraiAutoApprover.replyAccept.toRegex() in message.contentToString()
        val black = MiraiAutoApprover.replyBlack.toRegex() in message.contentToString()

        AdminContactCommand.runCatching {
            toCommandSender().handle(id = id, accept = accept, black = black, message = original)
        }.onFailure { cause ->
            logger.error({ "handle contact request failure." }, cause)
        }
    }

    // endregion

    // region Timer

    /**
     * 启动一个群定时服务
     */
    private fun GroupTimerService<*>.start(target: Group): Job = launch(SupervisorJob()) {
        while (isActive && target.isActive) {
            // 延时到 [moment]
            delay(wait(end = moment(target) ?: break))
            if (target.isActive.not() || target.botPermission < MemberPermission.ADMINISTRATOR) break

            when (this@start) {
                is GroupAllowTimer -> launch(SupervisorJob()) {
                    for ((id, permit) in run(target)) {
                        try {
                            if (permit) {
                                AbstractPermitteeId.AnyMember(target.id).permit(permissionId = id)
                            } else {
                                AbstractPermitteeId.AnyMember(target.id).cancel(permissionId = id, false)
                            }
                        } catch (cause: Throwable) {
                            logger.error({ "$id set failure with $id" }, cause)
                        }
                    }
                }
                is GroupCurfewTimer -> launch(SupervisorJob()) {
                    try {
                        target.settings.isMuteAll = run(target)
                    } catch (cause: Throwable) {
                        logger.error({ "${target.render()} mute set failure with $id" }, cause)
                    }
                }
                is MemberCleaner -> launch(SupervisorJob()) {
                    for ((member, reason) in run(target)) {
                        try {
                            member.kick(message = reason)
                        } catch (cause: Throwable) {
                            logger.error({ "${member.render()} clean failure with $id" }, cause)
                        }
                    }
                }
                is MemberNickCensor -> launch(SupervisorJob()) {
                    val censor = run(target)
                    for (member in target.members) {
                        try {
                            member.nameCard = censor(member) ?: continue
                        } catch (cause: Throwable) {
                            logger.error({ "${target.render()} nick set failure with $id" }, cause)
                        }
                    }
                }
                is MemberTitleCensor -> launch(SupervisorJob()) {
                    val censor = run(target)
                    for (member in target.members) {
                        try {
                            member.specialTitle = censor(member) ?: continue
                        } catch (cause: Throwable) {
                            logger.error({ "${target.render()} title set failure with $id" }, cause)
                        }
                    }
                }
            }.invokeOnCompletion { cause ->
                if (cause != null) {
                    logger.error({ "${target.render()} timer run failure with $id" }, cause)
                } else {
                    logger.info { "${target.render()} timer run success with $id" }
                }
            }
        }
    }

    /**
     * 启动一个定时消息服务
     */
    private fun BotTimingMessage.start(from: Bot): Job = launch(SupervisorJob()) {
        while (isActive && from.isActive) {
            delay(wait(end = moment(from) ?: break))

            run(contact = from).onCompletion { cause ->
                if (cause != null) {
                    logger.error({ "${from.render()} timer run failure with $id" }, cause)
                }
            }.collect { receipt ->
                logger.info { "${from.render()} timer ${receipt.target.render()} success with $id" }
            }
        }
    }

    @EventHandler
    internal fun BotOnlineEvent.mark() {
        for (timer in ComparableService<GroupTimerService<*>>()) {
            for (group in bot.groups) {
                timer.start(target = group)
            }
        }
        for (timer in ComparableService<BotTimingMessage>()) {
            timer.start(from = bot)
        }
    }

    @EventHandler
    internal fun BotGroupPermissionChangeEvent.mark() {
        if (group.botPermission < MemberPermission.ADMINISTRATOR) return
        for (timer in ComparableService<GroupTimerService<*>>()) {
            timer.start(target = group)
        }
    }

    // endregion

    // region Censor

    @EventHandler
    internal suspend fun GroupMessageEvent.mark() {
        if (group.botPermission <= sender.permission) return
        for (censor in ComparableService<ContentCensor>()) {
            if (censor.handle(event = this)) break else continue
        }
    }

    @EventHandler
    internal suspend fun NudgeEvent.mark() {
        if (subject !is Group) return
        if ((subject as Group).botPermission <= (from as Member).permission) return
        for (censor in ComparableService<ContentCensor>()) {
            if (censor.handle(event = this)) break else continue
        }
    }

    // endregion
}