package xyz.cssxsh.mirai.admin.command

import net.mamoe.mirai.console.command.*
import net.mamoe.mirai.console.permission.PermissionService.Companion.hasPermission
import net.mamoe.mirai.console.plugin.*
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.*
import net.mamoe.mirai.internal.message.flags.*
import xyz.cssxsh.mirai.admin.*
import xyz.cssxsh.mirai.spi.*

/**
 * 已注册服务查看，代替 原生 help 指令
 */
public object AdminRegisteredCommand : SimpleCommand(
    owner = MiraiAdminPlugin,
    primaryName = "registered", "reg",
    description = "查看已注册指令及服务"
) {
    /**
     * 打印注册指令及服务 到转发合并消息
     */
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
                            override fun generatePreview(forward: RawForwardMessage): List<String> {
                                return if (owner is Plugin) {
                                    listOf(
                                        "Version: ${owner.version}",
                                        "Author: ${owner.author}",
                                        owner.info
                                    )
                                } else {
                                    commands.map { "${it.primaryName}: ${it.description}" }
                                }
                            }

                            override fun generateTitle(forward: RawForwardMessage): String {
                                return if (owner is Plugin) {
                                    owner.name
                                } else {
                                    owner.parentPermission.id.namespace
                                }
                            }

                            override fun generateSummary(forward: RawForwardMessage): String {
                                return "共${commands.size}条指令"
                            }
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
                        override fun generatePreview(forward: RawForwardMessage): List<String> = services.map { clazz ->
                            "${clazz.simpleName!!}: ${
                                ComparableService.registered(clazz.java).joinToString(", ") { it.id }
                            }"
                        }

                        override fun generateTitle(forward: RawForwardMessage): String = "MiraiAdminService"
                        override fun generateSummary(forward: RawForwardMessage): String = "共${services.size}个服务"
                    }
                }

                displayStrategy = object : ForwardMessage.DisplayStrategy {
                    override fun generateTitle(forward: RawForwardMessage): String = "已注册指令及服务"
                    override fun generateSummary(forward: RawForwardMessage): String = "共${group.size}个分组"
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