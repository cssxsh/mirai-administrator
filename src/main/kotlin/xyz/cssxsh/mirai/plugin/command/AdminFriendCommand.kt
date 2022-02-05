package xyz.cssxsh.mirai.plugin.command

import net.mamoe.mirai.*
import net.mamoe.mirai.console.command.*
import net.mamoe.mirai.console.util.ContactUtils.render
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.utils.*
import xyz.cssxsh.mirai.plugin.*

public object AdminFriendCommand : CompositeCommand(
    owner = MiraiAdminPlugin,
    primaryName = "friend",
    description = "查看当前的好友"
) {
    @SubCommand
    @Description("好友列表")
    public suspend fun CommandSender.list() {
        val message = try {
            buildString {
                for (bot in Bot.instances) {
                    appendLine("--- ${bot.render()} ---")
                    for (friend in bot.friends) {
                        appendLine(friend.render())
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
    @Description("删除好友")
    public suspend fun CommandSender.delete(friend: Friend) {
        val message = try {
            friend.delete()
            "删除成功"
        } catch (cause: Throwable) {
            logger.warning({ "删除错误" }, cause)
            "删除错误"
        }

        sendMessage(message)
    }
}