package xyz.cssxsh.mirai.plugin

import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.unregister
import net.mamoe.mirai.console.plugin.jvm.*
import net.mamoe.mirai.event.*
import net.mamoe.mirai.utils.*
import xyz.cssxsh.mirai.plugin.command.*
import xyz.cssxsh.mirai.plugin.data.*
import xyz.cssxsh.mirai.spi.*

public object MiraiAdminPlugin : KotlinPlugin(
    JvmPluginDescription(
        id = "xyz.cssxsh.mirai.plugin.mirai-administrator",
        name = "mirai-administrator",
        version = "1.0.6",
    ) {
        author("cssxsh")
    }
) {

    override fun onEnable() {
        AdminAutoApproverConfig.reload()
        AdminOnlineMessageConfig.reload()
        AdminRequestEventData.reload()
        AdminTimerData.reload()
        AdminSetting.reload()
        AdminBlackListData.reload()

        if (AdminSetting.owner != AdminSetting.OWNER_DEFAULT) {
            logger.info { "机器人所有者 ${AdminSetting.owner}" }
        } else {
            throw IllegalArgumentException("机器人所有者 未设置")
        }

        logger.info { "发送上线通知请使用 /perm add g群号 ${MiraiOnlineMessage.permission.id} 赋予权限" }

        ComparableService.reload()
        logger.info { ComparableService.render() }

        MiraiAdministrator.registerTo(globalEventChannel())

        if (ComparableService<MessageSourceHandler>().isEmpty()) {
            ComparableService.instances.add(MiraiMessageRecorder)
            MiraiMessageRecorder.registerTo(globalEventChannel())
        }

        AdminContactCommand.register()
        AdminFriendCommand.register()
        AdminGroupCommand.register()
        AdminRecallCommand.register()
        AdminRegisteredCommand.register()
        AdminSendCommand.register()
        AdminTimerCommand.register()
    }

    override fun onDisable() {
        AdminContactCommand.unregister()
        AdminFriendCommand.unregister()
        AdminGroupCommand.unregister()
        AdminRecallCommand.unregister()
        AdminRegisteredCommand.unregister()
        AdminSendCommand.unregister()
        AdminTimerCommand.unregister()

        MiraiAdministrator.cancelAll()
        MiraiMessageRecorder.cancelAll()
    }
}