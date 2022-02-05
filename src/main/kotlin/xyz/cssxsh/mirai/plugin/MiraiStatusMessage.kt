package xyz.cssxsh.mirai.plugin

import kotlinx.coroutines.flow.*
import net.mamoe.mirai.*
import net.mamoe.mirai.console.command.*
import net.mamoe.mirai.console.command.CommandSender.Companion.asCommandSender
import net.mamoe.mirai.message.*
import net.mamoe.mirai.utils.*
import xyz.cssxsh.mirai.plugin.data.*
import xyz.cssxsh.mirai.spi.*
import java.time.*

public object MiraiStatusMessage : BotTimingMessage, MiraiStatusMessageConfig by AdminSetting {
    override val level: Int = 0
    override val id: String = "status"

    override fun moment(contact: Bot): LocalTime? {
        return if (sendStatusInterval > 0) LocalTime.now().plusMinutes(sendStatusInterval) else null
    }

    override suspend fun run(contact: Bot): Flow<MessageReceipt<*>> {
        BuiltInCommands.StatusCommand.runCatching {
            contact.owner().asCommandSender().handle()
        }.onFailure { cause ->
            logger.error({ "send status info failure." }, cause)
        }
        // XXX: StatusCommand no return
        return emptyFlow()
    }
}