package xyz.cssxsh.mirai.plugin.command

import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.command.*
import net.mamoe.mirai.console.util.ContactUtils.render
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.utils.*
import xyz.cssxsh.mirai.plugin.*

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
                        appendLine("${group.render()}[${group.botPermission}]<${group.members.size}>(${group.botMuteRemaining}s)")
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
    public suspend fun CommandSender.member(group: Group = subject as Group) {
        val message = try {
            buildString {
                for (bot in Bot.instances) {
                    appendLine("--- ${group.render()} ---")
                    for (member in group.members) {
                        appendLine("${member.render()}[${member.permission}](${member.muteTimeRemaining}s)")
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
    @Description("删除群员")
    public suspend fun CommandSender.quit(member: NormalMember, reason: String = "", block: Boolean = false) {
        val message = try {
            member.kick(message = reason, block = block)
            "删除成功"
        } catch (cause: Throwable) {
            logger.warning({ "删除错误" }, cause)
            "删除错误"
        }

        sendMessage(message)
    }

    @SubCommand
    @Description("群昵称")
    public suspend fun CommandSender.nick(member: NormalMember, nick: String = "") {
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
    public suspend fun CommandSender.special(member: NormalMember, title: String = "") {
        val message = try {
            member.specialTitle = title
            "设置成功"
        } catch (cause: Throwable) {
            logger.warning({ "设置错误" }, cause)
            "设置错误"
        }

        sendMessage(message)
    }
}