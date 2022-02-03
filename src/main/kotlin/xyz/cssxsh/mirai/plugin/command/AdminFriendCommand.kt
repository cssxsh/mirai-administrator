package xyz.cssxsh.mirai.plugin.command

import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.command.*
import net.mamoe.mirai.console.util.ContactUtils.render
import net.mamoe.mirai.utils.*
import xyz.cssxsh.mirai.plugin.*

public object AdminFriendCommand : SimpleCommand(
    owner = MiraiAdminPlugin,
    primaryName = "friend",
    description = "查看当前的好友"
) {
    @Handler
    public suspend fun CommandSender.handle() {
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
}