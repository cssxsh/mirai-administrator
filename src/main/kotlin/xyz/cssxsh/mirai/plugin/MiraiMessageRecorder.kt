package xyz.cssxsh.mirai.plugin

import net.mamoe.mirai.contact.*
import net.mamoe.mirai.event.*
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.message.data.*
import xyz.cssxsh.mirai.plugin.data.AdminSetting.recordLimit
import xyz.cssxsh.mirai.spi.*

/**
 * 简陋的内置消息记录器
 */
internal object MiraiMessageRecorder : SimpleListenerHost(), MessageSourceHandler {
    override val level: Int = 0
    override val id: String = "default-recorder"

    private val records: MutableMap<Long, MutableList<MessageSource>> = HashMap()

    @EventHandler(priority = EventPriority.HIGHEST)
    fun MessageEvent.mark() {
        val record = records.getOrPut(subject.id, ::mutableListOf)
        if (record.size == recordLimit) {
            record.removeFirst()
        }
        record.add(source)
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    fun MessagePostSendEvent<*>.mark() {
        val record = records.getOrPut(target.id, ::mutableListOf)
        if (record.size == recordLimit) {
            record.removeFirst()
        }
        record.add(source ?: return)
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
                record = records[event.subject.id] ?: ArrayList()
                event.message.findIsInstance<QuoteReply>()?.source
                    ?: record.findLast { it.fromId != event.source.fromId }
            }
            else -> throw IllegalArgumentException("无法指定要撤回消息")
        } ?: return null

        record.remove(source)

        return source
    }
}