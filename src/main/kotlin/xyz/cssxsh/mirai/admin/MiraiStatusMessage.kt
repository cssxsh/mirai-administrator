package xyz.cssxsh.mirai.admin

import kotlinx.coroutines.flow.*
import net.mamoe.mirai.*
import net.mamoe.mirai.console.command.*
import net.mamoe.mirai.console.command.CommandSender.Companion.asCommandSender
import net.mamoe.mirai.message.*
import net.mamoe.mirai.utils.*
import xyz.cssxsh.mirai.admin.data.*
import xyz.cssxsh.mirai.spi.*
import java.time.*

public object MiraiStatusMessage : BotTimingMessage, MiraiStatusMessageConfig by AdminSetting {
    override val level: Int = 0
    override val id: String = "status"
    private val records: MutableMap<Long, LocalTime> = HashMap()

    override fun moment(contact: Bot): LocalTime? {
        if (sendStatusInterval <= 0) return null
        return records[contact.id]?.plusMinutes(sendStatusInterval) ?: LocalTime.now().plusSeconds(3)
    }

    override suspend fun run(contact: Bot): Flow<MessageReceipt<*>> {
        return flow {
            // XXX: StatusCommand no return
            BuiltInCommands.StatusCommand.runCatching {
                contact.owner().asCommandSender().handle()
            }.onFailure { cause ->
                logger.error({ "send status info failure." }, cause)
            }.onSuccess {
                records[contact.id] = LocalTime.now()
            }
        }
    }
}