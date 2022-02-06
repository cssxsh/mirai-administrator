package xyz.cssxsh.mirai.plugin

import io.ktor.client.request.*
import kotlinx.coroutines.flow.*
import net.mamoe.mirai.*
import net.mamoe.mirai.console.permission.PermissionService.Companion.testPermission
import net.mamoe.mirai.console.permission.PermitteeId.Companion.permitteeId
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.message.*
import net.mamoe.mirai.message.code.*
import net.mamoe.mirai.message.data.Image.Key.queryUrl
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.*
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import xyz.cssxsh.mirai.plugin.data.*
import xyz.cssxsh.mirai.spi.*
import java.time.*
import java.util.*

public object MiraiOnlineMessage : BotTimingMessage, MiraiOnlineMessageConfig by AdminOnlineMessageConfig {
    override val level: Int = 0
    override val id: String = "online"

    internal val permission by lazy {
        MiraiAdminPlugin.registerPermission("online.include", "发送上线通知")
    }

    private val cache: MutableMap<Bot, LocalTime> = WeakHashMap()

    override fun moment(contact: Bot): LocalTime? {
        return if (contact !in cache) {
            cache[contact] = LocalTime.now()
            LocalTime.now().plusSeconds(3)
        } else {
            null
        }
    }

    @OptIn(MiraiExperimentalApi::class)
    private suspend fun xml(group: Group) = buildXmlMessage(1) {
        templateId = -1
        action = "web"
        brief = "QQ Bot 已启动"
        flag = 0

        val avatar = avatars.getOrPut(group.bot.id) {
            http.get<ByteArray>(group.bot.avatarUrl).toExternalResource()
        }
        val image = group.uploadImage(resource = avatar)

        item {
            layout = 2
            picture(coverUrl = image.queryUrl())
            title(text = "[${group.botAsMember.nick}]已加入该会话")
            summary(text = "[${group.botAsMember.nick}]开始接受指令执行")
        }

        source(name = "QQ Bot 已启动，可以开始执行指令")
    }

    private fun plain(group: Group) = buildMessageChain {
        appendLine("[${group.botAsMember.nick}]已加入该会话")
        appendLine("[${group.botAsMember.nick}]开始接受指令执行")
    }

    private val avatars: MutableMap<Long, ExternalResource> = WeakHashMap()

    override suspend fun run(contact: Bot): Flow<MessageReceipt<*>> {
        return contact.groups.asFlow().transform { group ->
            if (!permission.testPermission(group.permitteeId)) return@transform

            val message = when (type) {
                MiraiOnlineMessageConfig.Type.XML -> xml(group = group)
                MiraiOnlineMessageConfig.Type.PLAIN -> plain(group = group)
                MiraiOnlineMessageConfig.Type.CUSTOM -> MiraiCode.deserializeMiraiCode(code = custom, contact = group)
            }
            emit(group.sendMessage(message = message))
        }
    }
}