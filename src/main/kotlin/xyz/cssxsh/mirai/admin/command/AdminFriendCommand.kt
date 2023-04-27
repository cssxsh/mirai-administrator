package xyz.cssxsh.mirai.admin.command

import net.mamoe.mirai.*
import net.mamoe.mirai.console.command.*
import net.mamoe.mirai.console.util.ContactUtils.render
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.utils.*
import xyz.cssxsh.mirai.admin.*

/**
 * 好友相关指令
 */
public object AdminFriendCommand : CompositeCommand(
    owner = MiraiAdminPlugin,
    primaryName = "friend",
    description = "查看当前的好友"
) {
    /**
     * 打印好友列表
     */
    @SubCommand
    @Description("好友列表")
    public suspend fun CommandSender.list(vararg bots: Bot) {
        val message = try {
            buildString {
                for (bot in bots.asList().ifEmpty { Bot.instances }) {
                    appendLine("### ${bot.render()} ###")
                    try {
                        for (friendGroup in bot.friendGroups.asCollection()) {
                            if (friendGroup.count == 0) continue
                            appendLine("--- ${bot.render()}/${friendGroup.name} ---")
                            for (friend in friendGroup.friends) {
                                appendLine(friend.render())
                            }
                        }
                    } catch (_: NoSuchMethodError) {
                        for (friend in bot.friends) {
                            appendLine(friend.render())
                        }
                    }
                }
            }
        } catch (cause: IllegalStateException) {
            logger.warning({ "出现错误" }, cause)
            "出现错误"
        }

        sendMessage(message)
    }

    /**
     * 删除好友
     * @param friend 操作对象
     */
    @SubCommand
    @Description("删除好友")
    public suspend fun CommandSender.delete(friend: Friend) {
        val message = try {
            friend.delete()
            "删除成功"
        } catch (cause: IllegalStateException) {
            logger.warning({ "删除错误" }, cause)
            "删除错误"
        }

        sendMessage(message)
    }

    /**
     * 更改备注信息
     * @param friend 操作对象
     * @param name 备注名字
     */
    @SubCommand
    @Description("备注好友")
    public suspend fun CommandSender.remark(friend: Friend, name: String) {
        val message = try {
            friend.remark = name
            "更改成功"
        } catch (cause: IllegalStateException) {
            logger.warning({ "更改错误" }, cause)
            "更改错误"
        }

        sendMessage(message)
    }

    /**
     * 移动分组
     * @param friend 操作对象
     * @param name 分组名字
     */
    @SubCommand
    @Description("分组好友")
    public suspend fun CommandSender.group(friend: Friend, name: String) {
        val message = try {
            val target = with(friend.bot.friendGroups) {
                asCollection().find { it.name == name } ?: create(name)
            }
            target.moveIn(friend)
            "分组成功"
        } catch (cause: IllegalStateException) {
            logger.warning({ "分组错误" }, cause)
            "分组错误"
        }

        sendMessage(message)
    }
}