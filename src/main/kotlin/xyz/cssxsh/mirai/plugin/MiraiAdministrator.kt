package xyz.cssxsh.mirai.plugin

import kotlinx.coroutines.*
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.event.*
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.utils.*
import xyz.cssxsh.mirai.spi.*
import java.time.*
import kotlin.coroutines.*

/**
 * 事件监听及定时器
 */
public object MiraiAdministrator : SimpleListenerHost() {

    private val logger = MiraiAdminPlugin.logger

    override fun handleException(context: CoroutineContext, exception: Throwable) {
        // TODO
    }

    // region Approver

    @EventHandler
    public suspend fun MemberJoinRequestEvent.handle() {
        for (approver in ComparableService<MemberApprover>()) {
            try {
                when (val status = approver.approve(this)) {
                    ApproveStatus.Accept -> accept()
                    is ApproveStatus.Reject -> reject(blackList = status.black, message = status.message)
                    ApproveStatus.Ignore -> continue
                }
                break
            } catch (cause: Throwable) {
                logger.warning({ "$approver 审核 $this 失败" }, cause)
                continue
            }
        }
    }

    @EventHandler
    public suspend fun MemberJoinEvent.handle() {
        for (approver in ComparableService<MemberApprover>()) {
            try {
                when (val status = approver.approve(this)) {
                    ApproveStatus.Accept -> Unit
                    is ApproveStatus.Reject -> member.kick(message = status.message, block = status.black)
                    ApproveStatus.Ignore -> continue
                }
                break
            } catch (cause: Throwable) {
                logger.warning({ "$approver 审核 $this 失败" }, cause)
                continue
            }
        }
    }

    @EventHandler
    public suspend fun NewFriendRequestEvent.handle() {
        for (approver in ComparableService<FriendApprover>()) {
            try {
                when (val status = approver.approve(this)) {
                    ApproveStatus.Accept -> accept()
                    is ApproveStatus.Reject -> reject(blackList = status.black)
                    ApproveStatus.Ignore -> continue
                }
                break
            } catch (cause: Throwable) {
                logger.warning({ "$approver 审核 $this 失败" }, cause)
                continue
            }
        }
    }

    @EventHandler
    public suspend fun FriendAddEvent.handle() {
        for (approver in ComparableService<FriendApprover>()) {
            try {
                when (val status = approver.approve(this)) {
                    ApproveStatus.Accept -> Unit
                    is ApproveStatus.Reject -> {
                        friend.sendMessage(status.message)
                        friend.delete()
                    }
                    ApproveStatus.Ignore -> continue
                }
                break
            } catch (cause: Throwable) {
                logger.warning({ "$approver 审核 $this 失败" }, cause)
                continue
            }
        }
    }

    @EventHandler
    public suspend fun BotInvitedJoinGroupRequestEvent.handle() {
        for (approver in ComparableService<GroupApprover>()) {
            try {
                when (val status = approver.approve(this)) {
                    ApproveStatus.Accept -> accept()
                    is ApproveStatus.Reject -> {
                        invitor?.sendMessage(status.message)
                        ignore()
                    }
                    ApproveStatus.Ignore -> continue
                }
                break
            } catch (cause: Throwable) {
                logger.warning({ "$approver 审核 $this 失败" }, cause)
                continue
            }
        }
    }

    // endregion

    // region Timer

    private fun <C : ContactOrBot> TimerService<C>.start(contact: C) {
        launch(SupervisorJob()) {
            while (isActive && contact.isActive) {
                val moment = moment(contact) ?: break
                val now = LocalTime.now()
                val wait: Int = if (moment > now) {
                    moment.toSecondOfDay() - now.toSecondOfDay()
                } else {
                    24 * 60 * 60 + moment.toSecondOfDay() - now.toSecondOfDay()
                }
                delay(wait * 1000L)
                if (contact.isActive.not()) break
                launch(SupervisorJob()) {
                    try {
                        run(contact)
                    } catch (cause: Throwable) {
                        TODO()
                    }
                }
            }
        }
    }

    @EventHandler
    public fun BotOnlineEvent.mark() {
        for (timer in ComparableService<BotTimer>()) {
            timer.start(bot)
        }
        for (timer in ComparableService<GroupTimer>()) {
            for (group in bot.groups) {
                timer.start(group)
            }
        }
        for (timer in ComparableService<FriendTimer>()) {
            for (friend in bot.friends) {
                timer.start(friend)
            }
        }
    }

    @EventHandler
    public fun FriendAddEvent.mark() {
        for (timer in ComparableService<FriendTimer>()) {
            for (friend in bot.friends) {
                timer.start(friend)
            }
        }
    }

    @EventHandler
    public fun BotInvitedJoinGroupRequestEvent.mark() {
        for (timer in ComparableService<GroupTimer>()) {
            for (group in bot.groups) {
                timer.start(group)
            }
        }
    }

    // endregion
}