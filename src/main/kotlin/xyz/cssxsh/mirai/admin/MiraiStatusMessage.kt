package xyz.cssxsh.mirai.admin

import com.cronutils.model.*
import kotlinx.coroutines.flow.*
import net.mamoe.mirai.*
import net.mamoe.mirai.console.command.*
import net.mamoe.mirai.console.command.CommandSender.Companion.asCommandSender
import net.mamoe.mirai.event.*
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.message.*
import net.mamoe.mirai.utils.*
import xyz.cssxsh.mirai.admin.cron.*
import xyz.cssxsh.mirai.admin.data.*
import xyz.cssxsh.mirai.spi.*
import java.time.*

public object MiraiStatusMessage : BotTimingMessage {
    override val level: Int = 0
    override val id: String = "status"
    private val settings: Map<Long, Cron> get() = AdminTimerData.status

    override fun wait(contact: Bot): Long? {
        val cron = settings[contact.id] ?: return null
        return cron.toExecutionTime()
            .timeToNextExecution(ZonedDateTime.now())
            .orElse(Duration.ZERO)
            .toMillis()
            .coerceAtLeast(1_000)
    }

    override suspend fun run(contact: Bot): Flow<MessageReceipt<*>> {
        val owner = contact.owner()
        return channelFlow {
            owner.globalEventChannel().subscribe<MessagePostSendEvent<*>> {
                if (owner != target) return@subscribe ListeningStatus.LISTENING

                receipt?.let { trySend(element = it) }
                close(cause = exception)

                ListeningStatus.STOPPED
            }
            BuiltInCommands.StatusCommand.runCatching {
                owner.asCommandSender().handle()
            }.onFailure { cause ->
                logger.error({ "send status info failure." }, cause)
            }
        }
    }
}