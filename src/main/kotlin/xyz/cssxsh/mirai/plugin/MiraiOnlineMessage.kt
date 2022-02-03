package xyz.cssxsh.mirai.plugin

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.request.*
import kotlinx.coroutines.flow.*
import net.mamoe.mirai.*
import net.mamoe.mirai.console.permission.PermissionService.Companion.testPermission
import net.mamoe.mirai.console.permission.PermitteeId.Companion.permitteeId
import net.mamoe.mirai.message.*
import net.mamoe.mirai.message.data.Image.Key.queryUrl
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.*
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import xyz.cssxsh.mirai.plugin.data.*
import xyz.cssxsh.mirai.spi.*
import java.time.*
import java.util.*

public object MiraiOnlineMessage : BotTimingMessage, MiraiOnlineMessageConfig by AdminSetting {

    internal val permission by lazy {
        MiraiAdminPlugin.registerPermission("online.include", "发送上线通知")
    }

    private val http = HttpClient(OkHttp)

    private val cache: MutableMap<Bot, LocalTime> = WeakHashMap()

    override fun moment(contact: Bot): LocalTime? {
        return if (cache[contact] == null) {
            cache[contact] = LocalTime.now()
            LocalTime.now()
        } else {
            null
        }
    }

    @OptIn(MiraiExperimentalApi::class)
    private fun xml(bot: Bot, picture: String = bot.avatarUrl) = buildXmlMessage(1) {
        templateId = -1
        action = "web"
        brief = "QQ Bot 已启动"
        flag = 0

        item {
            layout = 2
            picture(coverUrl = picture)
            title(text = "[${bot.nick}]已加入该会话")
            summary(text = "[${bot.nick}]开始接受指令执行")
        }

        source(name = "QQ Bot 已启动，可以开始执行指令")
    }

    private fun plain(bot: Bot) = buildMessageChain {
        appendLine( "[${bot.nick}]已加入该会话")
        appendLine("[${bot.nick}]开始接受指令执行")
    }

    private val avatars: MutableMap<Long, ExternalResource> = WeakHashMap()

    override suspend fun run(contact: Bot): Flow<MessageReceipt<*>> {
        return contact.groups.asFlow().transform { group ->
            if (!permission.testPermission(group.permitteeId)) return@transform

            val message = when(onlineMessageType) {
                MiraiOnlineMessageConfig.Type.XML -> {
                    val avatar = avatars.getOrPut(group.bot.id) {
                        http.get<ByteArray>(group.bot.avatarUrl).toExternalResource()
                    }
                    val image = group.uploadImage(resource = avatar)
                    xml(bot = group.bot, picture = image.queryUrl())
                }
                MiraiOnlineMessageConfig.Type.PLAIN -> plain(bot = group.bot)
            }
            emit(group.sendMessage(message = message))
        }
    }

    override val id: String = "online"
}