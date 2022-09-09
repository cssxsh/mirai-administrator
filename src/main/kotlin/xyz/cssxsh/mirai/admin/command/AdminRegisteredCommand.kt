package xyz.cssxsh.mirai.admin.command

import net.mamoe.mirai.console.command.*
import net.mamoe.mirai.console.permission.PermissionService.Companion.hasPermission
import net.mamoe.mirai.console.plugin.*
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.*
import net.mamoe.mirai.internal.message.flags.*
import xyz.cssxsh.mirai.admin.*
import xyz.cssxsh.mirai.spi.*

public object AdminRegisteredCommand : SimpleCommand(
    owner = MiraiAdminPlugin,
    primaryName = "registered", "reg",
    description = "查看已注册指令及服务"
) {
    @Handler
    public suspend fun UserCommandSender.handle() {
        try {
            val registered = CommandManager.allRegisteredCommands
            val services = ComparableService::class.sealedSubclasses
            val group = registered.groupBy { it.owner }
            val forward = buildForwardMessage(subject) {
                for ((owner, commands) in group) {
                    bot named owner.parentPermission.id.namespace says buildForwardMessage(subject) {
                        for (command in commands) {
                            bot named command.primaryName says {
                                appendLine("Id: ${command.permission.id}")
                                appendLine("HasPermission: ${hasPermission(command.permission)}")
                                appendLine("Description: ${command.description}")
                                appendLine(command.usage)
                            }
                        }

                        displayStrategy = object : ForwardMessage.DisplayStrategy {
                            override fun generateTitle(forward: RawForwardMessage): String {
                                return (owner as? Plugin)?.name ?: owner.parentPermission.id.namespace
                            }
                            override fun generateSummary(forward: RawForwardMessage): String = "已注册${commands.size}条指令"
                        }
                    }
                }

                bot named "ComparableService" says buildForwardMessage(subject) {
                    for (subclass in services) {
                        bot named subclass.simpleName!! says {
                            for (service in ComparableService.registered(subclass.java)) {
                                appendLine("Id: ${service.id}")
                                appendLine("Class: ${service::class.qualifiedName}")
                                appendLine("Level: ${service.level}")
                                appendLine(service.description)
                            }
                        }
                    }
                    displayStrategy = object : ForwardMessage.DisplayStrategy {
                        override fun generateTitle(forward: RawForwardMessage): String = "ComparableService"
                        override fun generateSummary(forward: RawForwardMessage): String = "已注册${services.size}个服务"
                    }
                }

                displayStrategy = object : ForwardMessage.DisplayStrategy {
                    override fun generateTitle(forward: RawForwardMessage): String = "已注册指令及服务"
                    override fun generateSummary(forward: RawForwardMessage): String = "已注册${group.size}个插件"
                }
            }
            try {
                @Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")
                sendMessage(forward + IgnoreLengthCheck)
            } catch (_: NoClassDefFoundError) {
                sendMessage(forward)
            }
        } catch (cause: Exception) {
            logger.warning({ "出现错误" }, cause)
            sendMessage("出现错误")
        }
    }
}