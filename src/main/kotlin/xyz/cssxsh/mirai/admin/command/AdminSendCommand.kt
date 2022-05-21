package xyz.cssxsh.mirai.admin.command

import net.mamoe.mirai.*
import net.mamoe.mirai.console.*
import net.mamoe.mirai.console.command.*
import net.mamoe.mirai.console.util.*
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.message.code.*
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.message.*
import net.mamoe.mirai.utils.*
import xyz.cssxsh.mirai.admin.*

public object AdminSendCommand : CompositeCommand(
    owner = MiraiAdminPlugin,
    primaryName = "send",
    description = "联系人处理相关操作"
) {

    private suspend fun CommandSender.read(contact: Contact?): Message {
        return when (this) {
            is ConsoleCommandSender -> {
                val code = MiraiConsole.requestInput("请输入要发送的消息")
                MiraiCode.deserializeMiraiCode(code, contact)
            }
            is CommandSenderOnMessage<*> -> {
                sendMessage("请输入要发送的消息")
                fromEvent.nextMessage()
            }
            else -> throw IllegalStateException("未知环境 $this")
        }
    }

    @SubCommand
    @Description("发送给所有群")
    public suspend fun CommandSender.groups(bot: Bot? = this.bot, at: Boolean = false) {
        if (bot == null) {
            sendMessage("未指定机器人")
            return
        }
        try {
            val message = read(contact = null)
            for (group in bot.groups) {
                group.sendMessage(message = message + if (at) AtAll else EmptyMessageChain)
            }
        } catch (cause: Throwable) {
            logger.warning({ "发送给所有好友 处理失败" }, cause)
        }
    }

    @SubCommand
    @Description("发送给所有好友")
    public suspend fun CommandSender.friends(bot: Bot? = this.bot) {
        if (bot == null) {
            sendMessage("未指定机器人")
            return
        }
        try {
            val message = read(contact = null)
            for (friend in bot.friends) {
                friend.sendMessage(message = message)
            }
        } catch (cause: Throwable) {
            logger.warning({ "发送给所有好友 处理失败" }, cause)
        }
    }

    @SubCommand
    @Description("发送给指定联系人")
    public suspend fun CommandSender.to(contact: Contact, at: Boolean = false) {
        contact.sendMessage(
            message = read(contact = contact) + when {
                !at -> EmptyMessageChain
                contact is User -> At(contact)
                contact is Group -> AtAll
                else -> EmptyMessageChain
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
        } catch (cause: Throwable) {
            logger.warning({ "发送失败" }, cause)
            "发送失败"
        }

        sendMessage(message)
    }
}