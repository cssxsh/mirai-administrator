package xyz.cssxsh.mirai.plugin.command

import net.mamoe.mirai.console.command.*
import net.mamoe.mirai.console.permission.PermissionService.Companion.hasPermission
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.*
import net.mamoe.mirai.internal.message.*
import xyz.cssxsh.mirai.plugin.*
import xyz.cssxsh.mirai.spi.*

public object AdminRegisteredCommand : SimpleCommand(
    owner = MiraiAdminPlugin,
    primaryName = "registered",
    description = "查看已注册指令及服务"
) {
    @Handler
    public suspend fun UserCommandSender.handle() {
        try {
            val commands = CommandManager.allRegisteredCommands
            val services = ComparableService::class.sealedSubclasses
            val forward = buildForwardMessage(subject) {
                for (command in commands) {
                    bot named command.owner.parentPermission.id.namespace says {
                        appendLine("Id: ${command.permission.id}")
                        appendLine("HasPermission: ${hasPermission(command.permission)}")
                        appendLine("Description: ${command.description}")
                        appendLine(command.usage)
                    }
                }

                for (subclass in services) {
                    bot named subclass.simpleName!! says {
                        for (service in ComparableService.registered(subclass.java)) {
                            appendLine("Id: ${service.id}")
                            appendLine("Name: ${service::class.simpleName}")
                            appendLine("Level: ${service.level}")
                            appendLine(service.description)
                        }
                    }
                }

                displayStrategy = object : ForwardMessage.DisplayStrategy {
                    override fun generateTitle(forward: RawForwardMessage): String = "已注册指令及服务"
                    override fun generateSummary(forward: RawForwardMessage): String = "已注册${commands.size}条指令"
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