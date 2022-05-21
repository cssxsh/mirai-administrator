package xyz.cssxsh.mirai.admin.command

import net.mamoe.mirai.*
import net.mamoe.mirai.console.command.*
import net.mamoe.mirai.console.util.ContactUtils.render
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.utils.*
import xyz.cssxsh.mirai.admin.*

public object AdminGroupCommand : CompositeCommand(
    owner = MiraiAdminPlugin,
    primaryName = "group",
    description = "群处理相关操作"
) {
    @SubCommand
    @Description("群列表")
    public suspend fun CommandSender.list() {
        val message = try {
            buildString {
                for (bot in Bot.instances) {
                    appendLine("--- ${bot.render()} ---")
                    for (group in bot.groups) {
                        appendLine("${group.render()}[${group.botPermission}]<${group.members.size}>")
                    }
                }
            }
        } catch (cause: Throwable) {
            logger.warning({ "出现错误" }, cause)
            "出现错误"
        }

        sendMessage(message)
    }

    @SubCommand
    @Description("群成员")
    public suspend fun CommandSender.member(group: Group) {
        val message = try {
            buildString {
                appendLine("--- ${group.render()} ---")
                for (member in group.members) {
                    appendLine("${member.render()}[${member.permission}](${member.joinAt}~${member.lastSpeakAt})")
                }
            }
        } catch (cause: Throwable) {
            logger.warning({ "出现错误" }, cause)
            "出现错误"
        }

        sendMessage(message)
    }

    @SubCommand
    @Description("退出群聊")
    public suspend fun CommandSender.quit(group: Group) {
        val message = try {
            group.quit()
            "退出成功"
        } catch (cause: Throwable) {
            logger.warning({ "退出错误" }, cause)
            "退出错误"
        }

        sendMessage(message)
    }

    @SubCommand
    @Description("踢出群员")
    public suspend fun CommandSender.kick(member: NormalMember, reason: String = "", black: Boolean = false) {
        val message = try {
            member.kick(message = reason, block = black)
            "踢出成功"
        } catch (cause: Throwable) {
            logger.warning({ "踢出错误" }, cause)
            "踢出错误"
        }

        sendMessage(message)
    }

    @SubCommand
    @Description("群昵称")
    public suspend fun CommandSender.nick(member: NormalMember, nick: String) {
        val message = try {
            member.nameCard = nick
            "设置成功"
        } catch (cause: Throwable) {
            logger.warning({ "设置错误" }, cause)
            "设置错误"
        }

        sendMessage(message)
    }

    @SubCommand
    @Description("群头衔")
    public suspend fun CommandSender.title(member: NormalMember, title: String) {
        val message = try {
            member.specialTitle = title
            "设置成功"
        } catch (cause: Throwable) {
            logger.warning({ "设置错误" }, cause)
            "设置错误"
        }

        sendMessage(message)
    }

    @SubCommand
    @Description("禁言")
    public suspend fun CommandSender.mute(member: NormalMember, second: Int) {
        val message = try {
            if (second > 0) {
                member.mute(second)
                "禁言成功"
            } else {
                member.unmute()
                "解除成功"
            }
        } catch (cause: Throwable) {
            logger.warning({ "设置错误" }, cause)
            "设置错误"
        }

        sendMessage(message)
    }
}