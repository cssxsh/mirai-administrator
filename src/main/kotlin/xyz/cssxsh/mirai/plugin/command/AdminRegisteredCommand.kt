package xyz.cssxsh.mirai.plugin.command

import net.mamoe.mirai.console.command.*
import net.mamoe.mirai.console.permission.PermissionService.Companion.hasPermission
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.*
import net.mamoe.mirai.internal.message.*
import xyz.cssxsh.mirai.plugin.*

public object AdminRegisteredCommand : SimpleCommand(
    owner = MiraiAdminPlugin,
    primaryName = "registered",
    description = "查看已注册指令"
) {
    @Handler
    public suspend fun UserCommandSender.handle() {
        try {
            val registered = CommandManager.allRegisteredCommands
            val forward = buildForwardMessage(subject) {
                for (command in registered) {
                    bot named command.owner.parentPermission.id.namespace says {
                        appendLine("Id: ${command.permission.id}")
                        appendLine("HasPermission: ${hasPermission(command.permission)}")
                        appendLine("Description: ${command.description}")
                        appendLine(command.usage)
                    }
                }

                displayStrategy = object : ForwardMessage.DisplayStrategy {
                    override fun generateTitle(forward: RawForwardMessage): String = "已注册指令"
                    override fun generateSummary(forward: RawForwardMessage): String = "已注册${registered.size}条指令"
                }
            }
            @Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
            sendMessage(forward + IgnoreLengthCheck)
        } catch (cause: Throwable) {
            logger.warning({ "出现错误" }, cause)
            sendMessage("出现错误")
        }
    }
}