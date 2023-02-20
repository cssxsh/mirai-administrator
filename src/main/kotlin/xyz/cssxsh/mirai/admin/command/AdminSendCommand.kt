package xyz.cssxsh.mirai.admin.command

import kotlinx.coroutines.*
import net.mamoe.mirai.*
import net.mamoe.mirai.console.command.*
import net.mamoe.mirai.console.util.ContactUtils.render
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.*
import xyz.cssxsh.mirai.admin.*
import xyz.cssxsh.mirai.admin.data.*
import xyz.cssxsh.mirai.admin.mail.*
import kotlin.io.path.*

/**
 * 发送相关指令
 */
public object AdminSendCommand : CompositeCommand(
    owner = MiraiAdminPlugin,
    primaryName = "send",
    description = "发送消息或者戳一戳"
) {
    /**
     * 发送消息给所有群
     * @param bot 操作的机器人
     * @param at 是否At全体
     * @param second 发送的间隔
     */
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
                if (group.isBotMuted) {
                    logger.warning { "机器人在群 ${group.render()} 中是禁言状态" }
                    continue
                }
                delay(second * 1000)
                group.sendMessage(message = if (at) message + AtAll else message)
            }
        } catch (cause: SendMessageFailedException) {
            logger.warning({ "发送给所有群 发送失败" }, cause)
        } catch (cause: IllegalStateException) {
            logger.warning({ "发送给所有群 处理失败" }, cause)
        }
    }

    /**
     * 发送消息给所有好友
     * @param bot 操作的机器人
     * @param second 发送的间隔
     */
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
        } catch (cause: SendMessageFailedException) {
            logger.warning({ "发送给所有好友 发送失败" }, cause)
        } catch (cause: IllegalStateException) {
            logger.warning({ "发送给所有好友 处理失败" }, cause)
        }
    }

    /**
     * 发送给指定联系人
     * @param contact 目标联系人
     * @param at 是否At
     */
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

    /**
     * 戳一戳指定用户
     * @param user 要戳的用户
     */
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
        } catch (cause: UnsupportedOperationException) {
            logger.warning({ "发送失败" }, cause)
            "发送失败"
        } catch (cause: IllegalStateException) {
            logger.warning({ "发送失败" }, cause)
            "发送失败"
        }

        sendMessage(message)
    }

    /**
     * 备份日志到邮箱
     * @param addresses 接收的邮箱
     */
    @SubCommand
    @Description("备份日志到邮箱")
    public suspend fun CommandSender.log(vararg addresses: String) {
        val session = buildMailSession {
            AdminMailConfig.properties.inputStream().use {
                load(it)
            }
        }

        val mail = buildMailContent(session) {
            to = addresses.joinToString().ifEmpty { AdminMailConfig.log }
            title = "日志备份"
            text {
                val plugins = java.io.File("plugins")
                append("plugins: \n")
                for (file in plugins.listFiles().orEmpty()) {
                    append("    ").append(file.name)
                        .append(" ").append(file.length().div(1024)).append("KB").append('\n')
                }
                val libs = java.io.File("libs")
                append("libs: \n")
                for (file in libs.listFiles().orEmpty()) {
                    append("    ").append(file.name)
                        .append(" ").append(file.length().div(1024)).append("KB").append('\n')
                }
            }
            file("console.log") {
                val logs = java.io.File("logs")
                logs.listFiles()?.maxByOrNull { it.lastModified() }
            }
            file("network.log") {
                val logs = java.io.File("bots/${bot?.id}/logs")
                logs.listFiles()?.maxByOrNull { it.lastModified() }
            }
        }

        val current = Thread.currentThread()
        val oc = current.contextClassLoader
        try {
            current.contextClassLoader = AdminMailConfig::class.java.classLoader
            jakarta.mail.Transport.send(mail)
        } catch (cause: jakarta.mail.MessagingException) {
            sendMessage("邮件发送失败, cause: ${cause.message}")
        } finally {
            current.contextClassLoader = oc
        }
    }
}