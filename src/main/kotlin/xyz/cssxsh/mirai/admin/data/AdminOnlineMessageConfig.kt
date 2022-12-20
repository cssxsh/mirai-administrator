package xyz.cssxsh.mirai.admin.data

import net.mamoe.mirai.console.command.*
import net.mamoe.mirai.console.data.*
import net.mamoe.mirai.console.permission.*
import net.mamoe.mirai.console.util.*
import xyz.cssxsh.mirai.admin.*

@PublishedApi
internal object AdminOnlineMessageConfig : ReadOnlyPluginConfig("AdminOnlineMessageConfig"), MiraiOnlineMessageConfig {

    override var permission: Permission = Permission.getRootPermission()
        private set

    @OptIn(ConsoleExperimentalApi::class)
    override fun onInit(owner: PluginDataHolder, storage: PluginDataStorage) {
        if (owner is CommandOwner) {
            permission = owner.registerPermission(name = "online.include", description = "发送上线通知")
        }
    }

    @ValueName("type")
    @ValueDescription("发送上线消息的类型 XML, PLAIN, CUSTOM")
    override val type: MiraiOnlineMessageConfig.Type by value(MiraiOnlineMessageConfig.Type.XML)

    @ValueName("custom")
    @ValueDescription("CUSTOM 上线消息的内容")
    override val custom: String by value("哈哈哈，鸡汤来啦 [mirai:atall]")

    @ValueName("duration")
    @ValueDescription("逐个发送消息延时，单位秒")
    override val duration: Long by value(10L)
}