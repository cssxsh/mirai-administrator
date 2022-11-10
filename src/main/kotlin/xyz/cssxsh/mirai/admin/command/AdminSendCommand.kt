package xyz.cssxsh.mirai.admin.command

import kotlinx.coroutines.*
import net.mamoe.mirai.*
import net.mamoe.mirai.console.command.*
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.*
import xyz.cssxsh.mirai.admin.*

/**
 * 发送相关指令
 */
public object AdminSendCommand : CompositeCommand(
    owner = MiraiAdminPlugin,
    primaryName = "send",
    description = "发送消息或者戳一戳"
) {

    @SubCommand
    @Description("发送给所有群")
    public suspend fun CommandSender.groups(bot: Bot? = this.bot, at: Boolean = false, second: Long = 3) {
        if (bot == null) {
            sendMessage("未指定机器人")
            return
        }
        try {
            val message = request(hint = "请输入要发送的消息")
            for (group in bot.groups) {
                delay(second * 1000)
                group.sendMessage(message = if (at) message + AtAll else message)
            }
        } catch (cause: Exception) {
            logger.warning({ "发送给所有好友 处理失败" }, cause)
        }
    }

    @SubCommand
    @Description("发送给所有好友")
    public suspend fun CommandSender.friends(bot: Bot? = this.bot, second: Long = 3) {
        if (bot == null) {
            sendMessage("未指定机器人")
            return
        }
        try {
            val message = request(hint = "请输入要发送的消息")
            for (friend in bot.friends) {
                delay(second * 1000)
                friend.sendMessage(message = message)
            }
        } catch (cause: Exception) {
            logger.warning({ "发送给所有好友 处理失败" }, cause)
        }
    }

    @SubCommand
    @Description("发送给指定联系人")
    public suspend fun CommandSender.to(contact: Contact, at: Boolean = false) {
        contact.sendMessage(
            message = request(hint = "请输入要发送的消息") + when {
                !at -> emptyMessageChain()
                contact is User -> At(contact)
                contact is Group -> AtAll
                else -> emptyMessageChain()
            }
        )
    }

    @SubCommand
    @Description("戳一戳指定联系人")
    public suspend fun CommandSender.nudge(user: User) {
        val message = try {
            when (user) {
                is NormalMember -> user.nudge().sendTo(user.group)
                is Friend -> user.nudge().sendTo(user)
                is Stranger -> user.nudge().sendTo(user)
                else -> throw UnsupportedOperationException("nudge $user")
            }
            "发送成功"
        } catch (cause: Exception) {
            logger.warning({ "发送失败" }, cause)
            "发送失败"
        }

        sendMessage(message)
    }
}