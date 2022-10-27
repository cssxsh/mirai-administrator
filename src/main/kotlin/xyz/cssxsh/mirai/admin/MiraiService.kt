package xyz.cssxsh.mirai.admin

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.compression.*
import net.mamoe.mirai.*
import net.mamoe.mirai.console.*
import net.mamoe.mirai.console.command.*
import net.mamoe.mirai.console.permission.*
import net.mamoe.mirai.console.plugin.*
import net.mamoe.mirai.console.plugin.jvm.*
import net.mamoe.mirai.console.util.ContactUtils.render
import net.mamoe.mirai.console.util.*
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.message.code.*
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.message.*
import net.mamoe.mirai.utils.*
import xyz.cssxsh.mirai.admin.data.*
import xyz.cssxsh.mirai.spi.*
import java.time.*
import java.util.*

internal val logger by lazy {
    try {
        MiraiAdminPlugin.logger
    } catch (_: ExceptionInInitializerError) {
        MiraiLogger.Factory.create(MiraiAdministrator::class)
    }
}

internal fun Bot.owner() = getFriendOrFail(AdminSetting.owner)

internal val http = HttpClient(OkHttp) {
    install(UserAgent) {
        agent = "Mozilla/5.0 (Linux; Android 11; Redmi Note 8 Pro Build/RP1A.200720.011; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/89.0.4389.72 MQQBrowser/6.2 TBS/045913 Mobile Safari/537.36 V1_AND_SQ_8.8.68_2538_YYB_D A_8086800 QQ/8.8.68.7265 NetType/WIFI WebP/0.3.0 Pixel/1080 StatusBarHeight/76 SimpleUISwitch/1 QQTheme/2971 InMagicWin/0 StudyMode/0 CurrentMode/1 CurrentFontScale/1.0 GlobalDensityScale/0.9818182 AppId/537112567 Edg/98.0.4758.102"
    }
    ContentEncoding()
}

internal val NormalMember.lastSpeakAt: LocalDateTime
    get() = LocalDateTime.ofInstant(Instant.ofEpochSecond(lastSpeakTimestamp.toLong()), ZoneId.systemDefault())

internal val NormalMember.joinAt: LocalDateTime
    get() = LocalDateTime.ofInstant(Instant.ofEpochSecond(joinTimestamp.toLong()), ZoneId.systemDefault())

internal suspend fun CommandSender.request(hint: String, contact: Contact? = null): MessageChain = when (this) {
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

internal fun CommandOwner.registerPermission(name: String, description: String): Permission {
    return PermissionService.INSTANCE.register(permissionId(name), description, parentPermission)
}

internal fun NewFriendRequestEvent.render(accept: Boolean): Message = buildMessageChain {
    appendLine("with <${eventId}>")
    appendLine("@${fromNick}#${fromId}")
    appendLine("申请添加好友")
    appendLine("from $fromGroup")
    appendLine(message)
    if (accept) appendLine("已自动同意")
}

internal fun MemberJoinRequestEvent.render(accept: Boolean): Message = buildMessageChain {
    appendLine("with <${eventId}>")
    appendLine("@${fromNick}#${fromId}")
    appendLine("申请加入群")
    appendLine("to [$groupName](${groupId}) by $invitorId")
    appendLine(message)
    if (accept) appendLine("已自动同意")
}

internal fun BotInvitedJoinGroupRequestEvent.render(accept: Boolean): Message = buildMessageChain {
    appendLine("with <${eventId}>")
    appendLine("@${invitorNick}#${invitorId}")
    appendLine("邀请机器人加入群")
    appendLine("to [${groupName}](${groupId})")
    if (accept) appendLine("已自动同意")
}

internal fun AdminRequestEventData.render(): String = buildString {
    for ((qq, list) in this@render) {
        if (list.isEmpty()) continue
        val bot = try {
            Bot.getInstance(qq).render()
        } catch (_: Exception) {
            "Bot($qq)"
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
    val oc = Thread.currentThread().contextClassLoader
    try {
        for (plugin in PluginManager.plugins) {
            if (plugin !is JvmPlugin) continue
            if (plugin.description.dependencies.none { it.id == "xyz.cssxsh.mirai.plugin.mirai-administrator" }) continue
            val classLoader = plugin::class.java.classLoader
            Thread.currentThread().contextClassLoader = classLoader
            for (provider in ServiceLoader.load(ComparableService::class.java, classLoader).stream()) {
                try {
                    val service = provider.get()
                    instances.add(service)
                } catch (cause: Exception) {
                    logger.warning({ "${provider.type().name} load fail." }, cause)
                }
            }
        }
    } finally {
        Thread.currentThread().contextClassLoader = oc
    }
    instances.add(MiraiAutoApprover)
    instances.add(MiraiOnlineMessage)
    instances.add(MiraiStatusMessage)
    instances.add(MiraiMemberCleaner)
    instances.add(MiraiCurfewTimer)
    instances.add(MiraiContentCensor)
    instances.add(MiraiBlackList)
    instances.add(MiraiMessageTimer)
    instances.add(MiraiBackupService)
}

internal fun ComparableService.Loader.render(): String = buildString {
    appendLine("ComparableService Registered [${instances.size}]:")
    for (subclass in ComparableService::class.sealedSubclasses) {
        appendLine("${subclass.simpleName}: ${registered(subclass.java).joinToString { it.id }}")
    }
}

internal fun target(contact: Contact): MessageSource? {
    for (handler in ComparableService<MessageSourceHandler>()) {
        return try {
            handler.target(contact = contact) ?: continue
        } catch (cause: Exception) {
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
        } catch (cause: Exception) {
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
        } catch (cause: Exception) {
            logger.warning({ "message source find failure." }, cause)
            continue
        }
    }
    return null
}

internal fun backup() {
    if (ComparableService<BackupService>().isEmpty()) throw UnsupportedOperationException("没有任何实例")
    for (backup in ComparableService<BackupService>()) {
        try {
            backup.bot()
        } catch (cause: UnsupportedOperationException) {
            continue
        } catch (cause: Exception) {
            logger.warning({ "backup bot failure." }, cause)
            continue
        }
        logger.info { "backup bot ok by ${backup.id}" }
        break
    }

    for (backup in ComparableService<BackupService>()) {
        try {
            backup.friend()
        } catch (cause: UnsupportedOperationException) {
            continue
        } catch (cause: Exception) {
            logger.warning({ "backup friend failure." }, cause)
            continue
        }
        logger.info { "backup friend ok by ${backup.id}" }
        break
    }

    for (backup in ComparableService<BackupService>()) {
        try {
            backup.group()
        } catch (cause: UnsupportedOperationException) {
            continue
        } catch (cause: Exception) {
            logger.warning({ "backup group failure." }, cause)
            continue
        }
        logger.info { "backup group ok by ${backup.id}" }
        break
    }
}