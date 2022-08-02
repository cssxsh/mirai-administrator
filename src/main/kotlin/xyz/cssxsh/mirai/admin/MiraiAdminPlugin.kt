package xyz.cssxsh.mirai.admin

import net.mamoe.mirai.console.*
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.unregister
import net.mamoe.mirai.console.plugin.*
import net.mamoe.mirai.console.plugin.jvm.*
import net.mamoe.mirai.console.util.*
import net.mamoe.mirai.event.*
import net.mamoe.mirai.utils.*
import xyz.cssxsh.mirai.admin.command.*
import xyz.cssxsh.mirai.admin.data.*
import xyz.cssxsh.mirai.spi.*

public object MiraiAdminPlugin : KotlinPlugin(
    JvmPluginDescription(
        id = "xyz.cssxsh.mirai.plugin.mirai-administrator",
        name = "mirai-administrator",
        version = "1.2.6",
    ) {
        author("cssxsh")
    }
) {

    override fun onEnable() {
        // XXX: mirai console version check
        check(SemVersion.parseRangeRequirement(">= 2.12.0-RC").test(MiraiConsole.version)) {
            "$name $version 需要 Mirai-Console 版本 >= 2.12.0，目前版本是 ${MiraiConsole.version}"
        }

        AdminAutoApproverConfig.reload()
        AdminOnlineMessageConfig.reload()
        AdminCommentConfig.reload()
        AdminRequestEventData.reload()
        AdminTimerData.reload()
        AdminSetting.reload()
        AdminBlackListData.reload()
        AdminAutoQuitConfig.reload()

        if (AdminSetting.owner != AdminSetting.OWNER_DEFAULT) {
            logger.info { "机器人所有者 ${AdminSetting.owner}" }
        } else {
            throw IllegalArgumentException("机器人所有者 未设置")
        }

        logger.info { "发送上线通知请使用 /perm add g群号 ${AdminOnlineMessageConfig.permission.id} 赋予权限" }
        logger.info { "发送留言评论请使用 /perm add u1234 ${AdminCommentConfig.permission.id} 赋予权限" }
        logger.info { "定时消息部分功能更新了，请查看最新版文档" }

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

        AdminCommentConfig.save()
    }
}