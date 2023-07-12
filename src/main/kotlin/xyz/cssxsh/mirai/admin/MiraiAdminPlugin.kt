package xyz.cssxsh.mirai.admin

import kotlinx.coroutines.*
import net.mamoe.mirai.console.*
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.unregister
import net.mamoe.mirai.console.data.*
import net.mamoe.mirai.console.plugin.*
import net.mamoe.mirai.console.plugin.jvm.*
import net.mamoe.mirai.console.util.*
import net.mamoe.mirai.event.*
import net.mamoe.mirai.utils.*
import xyz.cssxsh.mirai.admin.command.*
import xyz.cssxsh.mirai.admin.data.*
import xyz.cssxsh.mirai.spi.*
import java.time.*

@PublishedApi
internal object MiraiAdminPlugin : KotlinPlugin(
    JvmPluginDescription(
        id = "xyz.cssxsh.mirai.plugin.mirai-administrator",
        name = "mirai-administrator",
        version = "1.4.1",
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
        AdminMailConfig.reload()

        if (AdminSetting.owner != AdminSetting.OWNER_DEFAULT) {
            logger.info { "机器人所有者 ${AdminSetting.owner}" }
        } else {
            val owner = runBlocking { ConsoleInput.requestInput(hint = "请输入机器人所有者") }.toLong()
            @OptIn(ConsoleExperimentalApi::class)
            @Suppress("UNCHECKED_CAST")
            val value = AdminSetting.findBackingFieldValue<Long>("owner") as Value<Long>
            value.value = owner
            AdminSetting.save()
        }

        logger.info { "发送上线通知请使用 /perm add g群号 ${AdminOnlineMessageConfig.permission.id} 赋予权限" }
        logger.info { "发送留言评论请使用 /perm add u1234 ${AdminCommentConfig.permission.id} 赋予权限" }
        logger.info { "定时消息部分功能更新了，请查看最新版文档" }
        logger.info { "censor_regex 配置项废除, 改为加载 censor 文件夹中的 txt 文件（不需要重启，会监听文件修改）" }
        if (ZoneId.systemDefault() != ZoneId.of("Asia/Shanghai")) {
            logger.warning { "当前系统时区不是 Asia/Shanghai" }
        }

        ComparableService.reload()
        logger.info { ComparableService.render() }

        MiraiAdministrator.registerTo(globalEventChannel())

        if (ComparableService<MessageSourceHandler>().isEmpty()) {
            ComparableService.instances.add(MiraiMessageRecorder)
            MiraiMessageRecorder.registerTo(globalEventChannel())
        }

        AdminBotCommand.register()
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

        MiraiAdministrator.cancel()
        MiraiMessageRecorder.cancel()

        AdminCommentConfig.save()
    }
}