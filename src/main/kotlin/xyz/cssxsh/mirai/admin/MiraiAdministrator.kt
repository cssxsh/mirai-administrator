package xyz.cssxsh.mirai.admin

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
import net.mamoe.mirai.message.code.*
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.message.data.MessageSource.Key.quote
import net.mamoe.mirai.utils.*
import xyz.cssxsh.mirai.admin.command.*
import xyz.cssxsh.mirai.admin.data.*
import xyz.cssxsh.mirai.spi.*
import kotlin.collections.*
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

    // XXX: AdminContactCommand ...
    @EventHandler
    internal suspend fun FriendMessageEvent.approve() {
        if (sender.id != AdminSetting.owner) return
        if (message.anyIsInstance<PlainText>().not()) return
        val original = (quote(event = this) ?: return).originalMessage.contentToString()
        val id = ("""(?<=with <)\d+""".toRegex().find(original)?.value ?: return).toLong()
        val accept = MiraiAutoApprover.replyAccept.toRegex() in message.contentToString()
        val black = MiraiAutoApprover.replyBlack.toRegex() in message.contentToString()
        if (!accept && !black) return
        AdminContactCommand.runCatching {
            toCommandSender().handle(id = id, accept = accept, black = black, message = original)
        }.onFailure { cause ->
            logger.error({ "handle contact request failure." }, cause)
        }
    }

    // endregion

    // region Timer

    private val cache: MutableMap<String, MutableSet<Long>> = HashMap()

    private val TimerService<*, *>.records get() = cache.getOrPut(id, ::HashSet)

    /**
     * 启动一个群定时服务
     */
    internal fun GroupTimerService<*>.start(target: Group) {
        if (records.add(target.id).not()) return
        launch(target.coroutineContext) service@{
            while (isActive) {
                delay(wait(contact = target) ?: break)

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
                    is GroupCurfewTimer -> launch(SupervisorJob()) curfew@{
                        try {
                            val moment = run(target) ?: return@curfew
                            if (!target.settings.isMuteAll) {
                                target.settings.isMuteAll = true
                            }
                            delay(moment)
                            if (target.settings.isMuteAll) {
                                target.settings.isMuteAll = false
                            }
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
                        logger.verbose { "${target.render()} timer run success with $id" }
                    }
                }
                // XXX: 保险, 避免 corn 输入错误，导致疯狂执行
                delay(60_000L)
            }
        }.invokeOnCompletion {
            records.remove(target.id)
        }
    }

    /**
     * 启动一个定时消息服务
     */
    internal fun BotTimingMessage.start(from: Bot) {
        if (records.add(from.id).not()) return
        launch(from.coroutineContext) {
            while (isActive) {
                delay(wait(contact = from) ?: break)

                run(contact = from).onCompletion { cause ->
                    if (cause != null) {
                        logger.error({ "${from.render()} timer run failure with $id" }, cause)
                    }
                }.collect { receipt ->
                    logger.info { "${from.render()} timer ${receipt.target.render()} success with $id" }
                }
                // XXX: 保险, 避免 corn 输入错误，导致疯狂执行
                delay(60_000L)
            }
        }.invokeOnCompletion {
            records.remove(from.id)
        }
    }

    /**
     * 启动一个初始化服务
     */
    internal fun BotOnlineAction.start(from: Bot) {
        launch(from.coroutineContext) {
            run(bot = from)
        }.invokeOnCompletion { cause ->
            if (cause != null) {
                logger.error({ "${from.render()} online action run failure with $id" }, cause)
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
        for (action in ComparableService<BotOnlineAction>()) {
            action.start(from = bot)
        }
    }

    @EventHandler
    internal fun BotGroupPermissionChangeEvent.mark() {
        if (new < MemberPermission.ADMINISTRATOR) return
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
        if (from !is Member) return
        if ((subject as Group).botPermission <= (from as Member).permission) return
        for (censor in ComparableService<ContentCensor>()) {
            if (censor.handle(event = this)) break else continue
        }
    }

    @EventHandler(priority = EventPriority.HIGH, concurrency = ConcurrencyKind.LOCKED)
    internal suspend fun MessageEvent.check() {
        for (blacklist in ComparableService<BlackListService>()) {
            if (blacklist.check(sender)) {
                intercept()
                break
            }
        }
    }

    // endregion

    // region Comment

    private val comments: MutableMap<Long, Long> = HashMap()

    @EventHandler(concurrency = ConcurrencyKind.LOCKED)
    internal suspend fun MessageEvent.comment() {
        when {
            sender.id == AdminSetting.owner -> return
            this is UserMessageEvent && AdminCommentConfig.user -> {}
            message.findIsInstance<At>()?.target == bot.id && AdminCommentConfig.at -> {}
            else -> return
        }

        if (toCommandSender().hasPermission(AdminCommentConfig.permission).not()) return
        if ((comments[sender.id] ?: 0) + AdminCommentConfig.interval > System.currentTimeMillis()) return

        comments[sender.id] = System.currentTimeMillis()

        val forward = buildForwardMessage {
            displayStrategy = object : ForwardMessage.DisplayStrategy {
                override fun generateTitle(forward: RawForwardMessage): String {
                    return "来自 ${sender.render()} 的留言"
                }
            }

            add(this@comment)
        }

        forward.sendTo(contact = bot.owner())

        MiraiCode.deserializeMiraiCode(code = AdminCommentConfig.reply.ifEmpty { return }, contact = sender)
            .plus(message.quote())
            .sendTo(contact = subject)
    }

    // endregion
}
