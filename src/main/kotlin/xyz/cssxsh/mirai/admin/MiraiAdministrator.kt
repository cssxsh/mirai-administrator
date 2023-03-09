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
import xyz.cssxsh.mirai.admin.mail.*
import xyz.cssxsh.mirai.spi.*
import kotlin.collections.*
import kotlin.coroutines.*
import kotlin.io.path.*

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

    @EventHandler(priority = EventPriority.HIGH)
    internal suspend fun MemberJoinRequestEvent.handle() {
        if ((group?.botPermission ?: MemberPermission.MEMBER) < MemberPermission.ADMINISTRATOR) return
        for (approver in ComparableService<MemberApprover>()) {
            try {
                when (val status = approver.approve(event = this)) {
                    ApproveResult.Accept -> accept()
                    is ApproveResult.Reject -> reject(blackList = status.black, message = status.message)
                    ApproveResult.Ignore -> continue
                }
                intercept()
                break
            } catch (cause: IllegalStateException) {
                logger.warning({ "${approver.id} 审核 $this 失败" }, cause)
                continue
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    internal suspend fun MemberJoinEvent.handle() {
        if (group.botPermission < MemberPermission.ADMINISTRATOR) return
        for (approver in ComparableService<MemberApprover>()) {
            try {
                when (val status = approver.approve(event = this)) {
                    ApproveResult.Accept -> Unit
                    is ApproveResult.Reject -> member.kick(message = status.message, block = status.black)
                    ApproveResult.Ignore -> continue
                }
                intercept()
                break
            } catch (cause: IllegalStateException) {
                logger.warning({ "${approver.id} 审核 $this 失败" }, cause)
                continue
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    internal suspend fun NewFriendRequestEvent.handle() {
        for (approver in ComparableService<FriendApprover>()) {
            try {
                when (val status = approver.approve(event = this)) {
                    ApproveResult.Accept -> accept()
                    is ApproveResult.Reject -> reject(blackList = status.black)
                    ApproveResult.Ignore -> continue
                }
                intercept()
                break
            } catch (cause: IllegalStateException) {
                logger.warning({ "${approver.id} 审核 $this 失败" }, cause)
                continue
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
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
                intercept()
                break
            } catch (cause: IllegalStateException) {
                logger.warning({ "${approver.id} 审核 $this 失败" }, cause)
                continue
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
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
                intercept()
                break
            } catch (cause: IllegalStateException) {
                logger.warning({ "${approver.id} 审核 $this 失败" }, cause)
                continue
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
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
                intercept()
                break
            } catch (cause: IllegalStateException) {
                logger.warning({ "${approver.id} 审核 $this 失败" }, cause)
                continue
            }
        }
    }

    // XXX: AdminContactCommand ...
    @EventHandler(priority = EventPriority.HIGH)
    internal suspend fun FriendMessageEvent.approve() {
        if (sender.id != AdminSetting.owner) return
        if (message.anyIsInstance<PlainText>().not()) return
        val original = (quote(event = this) ?: return).originalMessage.contentToString()
        val id = ("""(?<=with <)\d+""".toRegex().find(original)?.value ?: return).toLong()

        val content = message.contentToString()
        val accept = MiraiAutoApprover.replyAccept.toRegex() in content
        val reject = MiraiAutoApprover.replyReject.toRegex() in content
        val black = MiraiAutoApprover.replyBlack.toRegex() in content
        val reply = content.substringAfter('\n')

        AdminContactCommand.runCatching {
            when {
                accept -> toCommandSender().handle(id = id, accept = true, black = false, message = "")
                reject -> toCommandSender().handle(id = id, accept = false, black = false, message = reply)
                black -> toCommandSender().handle(id = id, accept = false, black = true, message = reply)
            }
        }.onFailure { cause ->
            logger.error({ "handle contact request failure." }, cause)
        }
        intercept()
    }

    // endregion

    // region Timer

    /**
     * 启动一个群定时服务
     */
    @PublishedApi
    internal fun GroupTimerService<*>.start(target: Group) {
        records.remove(target.id)?.cancel()
        val job = launch(target.coroutineContext) service@{
            while (isActive) {
                when (val millis = wait(contact = target)) {
                    null -> break
                    else -> {
                        logger.debug { "${target.render()} timer after ${millis}ms" }
                        delay(millis)
                    }
                }

                when (this@start) {
                    is GroupAllowTimer -> launch {
                        for ((id, permit) in run(target)) {
                            try {
                                if (permit) {
                                    AbstractPermitteeId.AnyMember(target.id).permit(permissionId = id)
                                } else {
                                    AbstractPermitteeId.AnyMember(target.id).cancel(permissionId = id, false)
                                }
                            } catch (cause: NoSuchElementException) {
                                logger.error({ "$id set failure with $id" }, cause)
                            }
                        }
                    }
                    is GroupCurfewTimer -> launch curfew@{
                        try {
                            val moment = run(target) ?: return@curfew
                            if (!target.settings.isMuteAll) {
                                target.settings.isMuteAll = true
                            }
                            delay(moment)
                            if (target.settings.isMuteAll) {
                                target.settings.isMuteAll = false
                            }
                        } catch (cause: PermissionDeniedException) {
                            logger.error({ "${target.render()} mute set failure with $id" }, cause)
                        }
                    }
                    is MemberCleaner -> launch {
                        for ((member, reason) in run(target)) {
                            try {
                                member.kick(message = reason)
                            } catch (cause: PermissionDeniedException) {
                                logger.error({ "${member.render()} clean failure with $id" }, cause)
                            }
                            // XXX: Operation too fast
                            delay(60_000L)
                        }
                    }
                    is MemberNickCensor -> launch {
                        val censor = run(target)
                        for (member in target.members) {
                            try {
                                member.nameCard = censor(member) ?: continue
                            } catch (cause: PermissionDeniedException) {
                                logger.error({ "${target.render()} nick set failure with $id" }, cause)
                            }
                        }
                    }
                    is MemberTitleCensor -> launch {
                        val censor = run(target)
                        for (member in target.members) {
                            try {
                                member.specialTitle = censor(member) ?: continue
                            } catch (cause: PermissionDeniedException) {
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
        }
        job.invokeOnCompletion {
            records.remove(target.id, job)
        }
        records[target.id] = job
    }

    /**
     * 启动一个定时消息服务
     */
    @PublishedApi
    internal fun BotTimingMessage.start(from: Bot) {
        records.remove(from.id)?.cancel()
        val job = launch(from.coroutineContext) {
            while (isActive) {
                when (val millis = wait(contact = from)) {
                    null -> break
                    else -> {
                        logger.debug { "$${from.render()} timing message after ${millis}ms" }
                        delay(millis)
                    }
                }

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
        }
        job.invokeOnCompletion {
            records.remove(from.id, job)
        }
        records[from.id] = job
    }

    /**
     * 启动一个初始化服务
     */
    @PublishedApi
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

    @EventHandler(priority = EventPriority.HIGH)
    internal suspend fun GroupMessageEvent.mark() {
        if (group.botPermission <= sender.permission) return
        for (censor in ComparableService<ContentCensor>()) {
            if (censor.handle(event = this)) {
                intercept()
                break
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    internal suspend fun NudgeEvent.mark() {
        if (from !is Member) return
        if ((subject as Group).botPermission <= (from as Member).permission) return
        for (censor in ComparableService<ContentCensor>()) {
            if (censor.handle(event = this)) {
                intercept()
                break
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    internal suspend fun MessageEvent.check() {
        for (blacklist in ComparableService<BlackListService>()) {
            if (blacklist.check(sender)) {
                intercept()
                break
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    internal suspend fun NudgeEvent.check() {
        val sender = from as? User ?: return
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
            this is UserMessageEvent && AdminCommentConfig.user &&
                message.findIsInstance<PlainText>()?.content?.getOrNull(0) != '/' -> Unit
            message.findIsInstance<QuoteReply>()?.source?.fromId == bot.id && AdminCommentConfig.quote -> Unit
            message.findIsInstance<At>()?.target == bot.id && AdminCommentConfig.at -> Unit
            else -> return
        }

        if (toCommandSender().hasPermission(AdminCommentConfig.permission).not()) return
        if ((comments[sender.id] ?: 0) + AdminCommentConfig.interval > System.currentTimeMillis()) return

        comments[sender.id] = System.currentTimeMillis()

        launch {
            val forward = buildForwardMessage(subject) {
                displayStrategy = object : ForwardMessage.DisplayStrategy {
                    override fun generateTitle(forward: RawForwardMessage): String {
                        return "来自 ${sender.render()} 的留言"
                    }
                }

                try {
                    quote(this@comment)?.let {
                        it.fromId at it.time says it.originalMessage
                    }
                } catch (cause: Exception) {
                    logger.warning({ "回溯评论上文失败" }, cause)
                }

                add(this@comment)
            }

            forward.sendTo(contact = bot.owner())

            if (AdminCommentConfig.reply.isEmpty()) return@launch
            MiraiCode.deserializeMiraiCode(code = AdminCommentConfig.reply, contact = sender)
                .plus(message.quote())
                .sendTo(contact = subject)
        }
    }

    // endregion

    // region Auto

    @EventHandler
    internal suspend fun BotMuteEvent.handle() {
        with(AdminAutoQuitConfig) {
            if (!admin && group.botPermission.isAdministrator()) return
            if (!owner && operator.id == AdminSetting.owner) return
            if (durationSeconds < limit) return

            delay(duration)

            // 检查是否还是禁言状态，如果是就退群
            if (group.isBotMuted) {
                launch {
                    bot.owner().sendMessage("因为禁言，将自动退群 ${group.render()}")
                }
                group.quit()
            }
        }
    }

    @EventHandler
    internal fun BotOfflineEvent.handle() {
        if (AdminMailConfig.notify.not()) return
        if (AdminMailConfig.close.not() && this is BotOfflineEvent.Active) return
        val session = buildMailSession {
            AdminMailConfig.properties.inputStream().use {
                load(it)
            }
        }
        val offline = this

        launch {
            val mail = buildMailContent(session) {
                to = AdminMailConfig.offline.ifEmpty { "${AdminSetting.owner}@qq.com" }
                title = "机器人下线通知 $bot"
                text {
                    @OptIn(MiraiInternalApi::class)
                    when (offline) {
                        is BotOfflineEvent.Active -> {
                            append("主动离线")
                        }
                        is BotOfflineEvent.Dropped -> {
                            append("因网络问题而掉线")
                            if (offline.cause != null) {
                                append('\n')
                                append("cause:\n")
                                append(offline.cause!!.stackTraceToString())
                            }
                        }
                        is BotOfflineEvent.Force -> {
                            append("被挤下线.")
                        }
                        is BotOfflineEvent.MsfOffline -> {
                            append("被服务器断开.")
                            if (offline.cause != null) {
                                append('\n')
                                append("cause:\n")
                                append(offline.cause!!.stackTraceToString())
                            }
                        }
                        is BotOfflineEvent.RequireReconnect -> {
                            append("服务器主动要求更换另一个服务器.")
                            if (offline.cause != null) {
                                append('\n')
                                append("cause:\n")
                                append(offline.cause!!.stackTraceToString())
                            }
                        }
                    }

                    var start = 0
                    while (isActive) {
                        val index = indexOf("\t", start)
                        if (index == -1) break
                        replace(index, index + 1, "    ")
                        start = index + 4
                    }
                }
                file("console.log") {
                    val logs = java.io.File("logs")
                    logs.listFiles()?.maxByOrNull { it.lastModified() }

                }
                file("network.log") {
                    val logs = java.io.File("bots/${bot.id}/logs")
                    logs.listFiles()?.maxByOrNull { it.lastModified() }
                }
            }

            val current = Thread.currentThread()
            val oc = current.contextClassLoader
            try {
                current.contextClassLoader = AdminMailConfig::class.java.classLoader
                jakarta.mail.Transport.send(mail)
            } catch (cause: jakarta.mail.MessagingException) {
                logger.error({ "邮件发送失败" }, cause)
            } finally {
                current.contextClassLoader = oc
            }
        }
    }

    // endregion
}
