package xyz.cssxsh.mirai.plugin

import net.mamoe.mirai.contact.*
import net.mamoe.mirai.event.*
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.message.data.*
import xyz.cssxsh.mirai.spi.*

/**
 * 简陋的内置消息记录器
 */
internal object MiraiMessageRecorder : SimpleListenerHost(), MessageSourceHandler {

    private val records: MutableMap<Long, MutableList<MessageSource>> = HashMap()

    @EventHandler
    fun MessageEvent.mark() {
        records.getOrPut(subject.id, ::mutableListOf).add(source)
    }

    @EventHandler
    fun MessagePostSendEvent<*>.mark() {
        records.getOrPut(target.id, ::mutableListOf).add(source ?: return)
    }

    override fun find(contact: Contact?, event: MessageEvent?): MessageSource? {
        val record: MutableList<MessageSource>
        val source = when {
            contact is Member -> {
                record = records[contact.group.id] ?: return null
                record.findLast { it.fromId == contact.id }
            }
            contact != null -> {
                record = records[contact.id] ?: return null
                record.findLast { it.fromId == contact.bot.id }
            }
            event != null -> {
                record = records[event.subject.id] ?: return null
                (event.message.findIsInstance<QuoteReply>()?.source
                    ?: record.findLast { it.fromId != event.source.fromId })
            }
            else -> throw IllegalArgumentException("无法指定要撤回消息")
        } ?: return null

        record.remove(source)

        return source
    }

    override val id: String = "default"
}