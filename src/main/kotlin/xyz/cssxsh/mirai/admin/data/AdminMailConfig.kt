package xyz.cssxsh.mirai.admin.data

import net.mamoe.mirai.console.command.*
import net.mamoe.mirai.console.data.*
import net.mamoe.mirai.console.plugin.*
import net.mamoe.mirai.console.plugin.jvm.*
import net.mamoe.mirai.console.util.*
import net.mamoe.mirai.utils.*
import java.io.*
import kotlin.io.path.*

@PublishedApi
internal object AdminMailConfig : ReadOnlyPluginConfig("AdminMailConfig") {

    @ValueName("offline_notify")
    @ValueDescription("机器人下线时，发送邮件")
    val notify: Boolean by value(true)

    @ValueName("close_notify")
    @ValueDescription("机器人正常关闭时，也发送邮件")
    val close: Boolean by value(false)

    @ValueName("bot_offline")
    @ValueDescription("机器人下线时，接收邮件的地址")
    val offline: String by value("")

    @ValueName("log_backup")
    @ValueDescription("备份日志时，接收邮件的地址")
    val log: String by value("")

    var properties = Path("admin.mail.properties")
        private set

    @OptIn(ConsoleExperimentalApi::class)
    override fun onInit(owner: PluginDataHolder, storage: PluginDataStorage) {
        if (owner is JvmPlugin) {
            properties = owner.resolveConfigPath("admin.mail.properties")
            if (properties.notExists()) {
                properties.writeText(
                    """
                    mail.host=smtp.example.com
                    mail.auth=true
                    mail.user=xxx
                    mail.password=****
                    mail.from=xxx@example.com
                    mail.store.protocol=smtp
                    mail.transport.protocol=smtp
                    # smtp
                    mail.smtp.starttls.enable=true
                    mail.smtp.auth=true
                    mail.smtp.timeout=15000
                """.trimIndent()
                )
                owner.logger.info { "邮件配置文件已生成，请修改内容以生效 $properties" }
            }
        }
    }
}