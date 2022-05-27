package xyz.cssxsh.mirai.admin

import net.mamoe.mirai.contact.*
import net.mamoe.mirai.event.*
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.message.data.*
import xyz.cssxsh.mirai.admin.data.AdminSetting.recordLimit
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

    @EventHandler(priority = EventPriority.HIGHEST)
    fun MessageRecallEvent.mark() {
        when (this) {
            is MessageRecallEvent.FriendRecall -> records[author.id]?.removeIf {
                it.ids.contentEquals(messageIds) && it.internalIds.contentEquals(messageInternalIds)
            }
            is MessageRecallEvent.GroupRecall -> records[group.id]?.removeIf {
                it.ids.contentEquals(messageIds) && it.internalIds.contentEquals(messageInternalIds)
            }
        }
    }

    override fun from(member: Member): MessageSource? {
        return records[member.group.id]?.findLast { it.fromId == member.id }
    }

    override fun target(contact: Contact): MessageSource? {
        return records[contact.id]?.findLast { it.fromId == contact.bot.id }
    }

    override fun quote(event: MessageEvent): MessageSource? {
        return event.message.findIsInstance<QuoteReply>()?.source
            ?: records[event.subject.id]?.findLast { it.fromId != event.source.fromId }
    }
}