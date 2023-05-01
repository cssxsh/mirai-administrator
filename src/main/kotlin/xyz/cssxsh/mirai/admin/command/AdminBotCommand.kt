package xyz.cssxsh.mirai.admin.command

import net.mamoe.mirai.*
import net.mamoe.mirai.console.command.*
import net.mamoe.mirai.console.util.ContactUtils.render
import net.mamoe.mirai.utils.*
import xyz.cssxsh.mirai.admin.*

/**
 * BOT相关指令
 */
public object AdminBotCommand : CompositeCommand(
    owner = MiraiAdminPlugin,
    primaryName = "bot",
    description = "BOT处理相关操作"
) {
    /**
     * 打印 BOT 列表
     */
    @SubCommand
    public suspend fun CommandSender.list() {
        val message = try {
            buildString {
                for (bot in Bot.instances) {
                    appendLine("--- ${bot.render()} ---")
                    appendLine("Protocol: ${bot.configuration.protocol}")
                    appendLine("Heartbeat Strategy: ${bot.configuration.heartbeatStrategy}")
                }
            }
        } catch (cause: IllegalStateException) {
            logger.warning({ "出现错误" }, cause)
            "出现错误"
        }

        sendMessage(message)
    }

    /**
     * 登出 BOT
     * @param bot 操作对象
     */
    @SubCommand
    public suspend fun CommandSender.logout(bot: Bot) {
        val message = try {
            val message = buildString {
                appendLine("将尝试关闭 ${bot.render()}")
            }
            bot.close()
            message
        } catch (cause: IllegalStateException) {
            logger.warning({ "出现错误" }, cause)
            "出现错误"
        }

        sendMessage(message)
    }
}