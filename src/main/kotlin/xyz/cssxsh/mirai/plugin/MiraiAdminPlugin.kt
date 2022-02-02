package xyz.cssxsh.mirai.plugin

import net.mamoe.mirai.console.plugin.jvm.*
import net.mamoe.mirai.event.*
import net.mamoe.mirai.utils.*
import xyz.cssxsh.mirai.spi.*

public object MiraiAdminPlugin : KotlinPlugin(
    JvmPluginDescription(
        id = "xyz.cssxsh.mirai.mirai-administrator",
        name = "mirai-administrator",
        version = "1.0.0-RC1",
    ) {
        author("cssxsh")
    }
) {

    override fun onEnable() {
        logger.info { "MemberApprover Registered: ${ComparableService<MemberApprover>()}" }
        logger.info { "FriendApprover Registered: ${ComparableService<FriendApprover>()}" }
        logger.info { "GroupApprover Registered: ${ComparableService<GroupApprover>()}" }
        logger.info { "BotTimer Registered: ${ComparableService<BotTimer>()}" }
        logger.info { "GroupTimer Registered: ${ComparableService<GroupTimer>()}" }
        logger.info { "FriendTimer Registered: ${ComparableService<FriendTimer>()}" }

        MiraiAdministrator.registerTo(globalEventChannel())
    }

    override fun onDisable() {
        MiraiAdministrator.cancelAll()
    }
}