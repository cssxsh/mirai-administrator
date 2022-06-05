package xyz.cssxsh.mirai.admin

import com.cronutils.model.*
import com.cronutils.model.time.*
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import net.mamoe.mirai.*
import net.mamoe.mirai.console.*
import net.mamoe.mirai.console.command.*
import net.mamoe.mirai.console.permission.*
import net.mamoe.mirai.console.plugin.jvm.*
import net.mamoe.mirai.console.util.ContactUtils.render
import net.mamoe.mirai.console.util.*
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.message.code.*
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.message.*
import net.mamoe.mirai.utils.*
import xyz.cssxsh.mirai.admin.cron.*
import xyz.cssxsh.mirai.admin.data.*
import xyz.cssxsh.mirai.spi.*
import java.time.*
import java.util.*

internal inline val logger get() = MiraiAdminPlugin.logger

internal fun Bot.owner() = getFriendOrFail(AdminSetting.owner)

internal val http by lazy { HttpClient(OkHttp) }

internal fun Cron.toExecutionTime(): ExecutionTime = when (this) {
    is DataCron -> ExecutionTime.forCron(delegate)
    else -> ExecutionTime.forCron(this)
}

internal val NormalMember.lastSpeakAt: LocalDateTime
    get() = LocalDateTime.ofInstant(Instant.ofEpochSecond(lastSpeakTimestamp.toLong()), ZoneId.systemDefault())

internal val NormalMember.joinAt: LocalDateTime
    get() = LocalDateTime.ofInstant(Instant.ofEpochSecond(joinTimestamp.toLong()), ZoneId.systemDefault())

internal suspend fun CommandSender.request(hint: String,  contact: Contact? = null): MessageChain = when (this) {
    is ConsoleCommandSender -> {
        val code = MiraiConsole.requestInput(hint)
        MiraiCode.deserializeMiraiCode(code, contact)
    }
    is CommandSenderOnMessage<*> -> {
        sendMessage(hint)
        fromEvent.nextMessage()
    }
    else -> throw IllegalStateException("未知环境 $this")
}

internal fun AbstractJvmPlugin.registerPermission(name: String, description: String): Permission {
    return PermissionService.INSTANCE.register(permissionId(name), description, parentPermission)
}

internal fun NewFriendRequestEvent.render(accept: Boolean): Message = buildMessageChain {
    appendLine("@${fromNick}#${fromId} with <${eventId}>")
    appendLine("申请添加好友")
    appendLine("from $fromGroup")
    appendLine(message)
    if (accept) appendLine("已自动同意")
}

internal fun MemberJoinRequestEvent.render(accept: Boolean): Message = buildMessageChain {
    appendLine("@${fromNick}#${fromId} with <${eventId}>")
    appendLine("申请加入群")
    appendLine("to [$groupName](${groupId}) by $invitorId")
    appendLine(message)
    if (accept) appendLine("已自动同意")
}

internal fun BotInvitedJoinGroupRequestEvent.render(accept: Boolean): Message = buildMessageChain {
    appendLine("@${invitorNick}#${invitorId} with <${eventId}>")
    appendLine("邀请机器人加入群")
    appendLine("to [${groupName}](${groupId})")
    if (accept) appendLine("已自动同意")
}

internal fun AdminRequestEventData.render(): String = buildString {
    for ((qq, list) in this@render) {
        if (list.isEmpty()) continue
        val bot = try {
            Bot.getInstance(qq).render()
        } catch (_: Throwable) {
            "$qq"
        }
        appendLine("--- $bot ---")
        for (request in list) {
            appendLine(request)
        }
    }
    if (isEmpty()) {
        appendLine("没有记录")
    }
}

internal fun ComparableService.Loader.reload() {
    instances.clear()
    for (classLoader in JvmPluginLoader.classLoaders) {
        instances.addAll(ServiceLoader.load(ComparableService::class.java, classLoader))
        for (subclass in ComparableService::class.sealedSubclasses) {
            instances.addAll(ServiceLoader.load(subclass.java, classLoader))
        }
    }
    instances.add(MiraiAutoApprover)
    instances.add(MiraiOnlineMessage)
    instances.add(MiraiStatusMessage)
    instances.add(MiraiMemberCleaner)
    instances.add(MiraiCurfewTimer)
    instances.add(MiraiContentCensor)
    instances.add(MiraiBlackList)
}

internal fun ComparableService.Loader.render(): String = buildString {
    appendLine("ComparableService Registered [${instances.size}]:")
    for (subclass in ComparableService::class.sealedSubclasses) {
        appendLine("${subclass.simpleName}: ${registered(subclass.java).joinToString { it.id }}")
    }
}

@Deprecated("接口定义不明确", ReplaceWith("null"))
internal fun source(contact: Contact?, event: MessageEvent?): MessageSource? {
    logger.error { "xyz.cssxsh.mirai.admin.source 方法已废弃" }
    return null
}

internal fun target(contact: Contact): MessageSource? {
    for (handler in ComparableService<MessageSourceHandler>()) {
        return try {
            handler.target(contact = contact) ?: continue
        } catch (cause: Throwable) {
            logger.warning({ "message source find failure." }, cause)
            continue
        }
    }
    return null
}

internal fun from(member: Member): MessageSource? {
    for (handler in ComparableService<MessageSourceHandler>()) {
        return try {
            handler.from(member) ?: continue
        } catch (cause: Throwable) {
            logger.warning({ "message source find failure." }, cause)
            continue
        }
    }
    return null
}

internal fun quote(event: MessageEvent): MessageSource? {
    for (handler in ComparableService<MessageSourceHandler>()) {
        return try {
            handler.quote(event = event) ?: continue
        } catch (cause: Throwable) {
            logger.warning({ "message source find failure." }, cause)
            continue
        }
    }
    return null
}