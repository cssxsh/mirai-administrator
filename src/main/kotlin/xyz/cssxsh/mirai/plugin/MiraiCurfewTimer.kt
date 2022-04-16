package xyz.cssxsh.mirai.plugin

import net.mamoe.mirai.contact.*
import xyz.cssxsh.mirai.plugin.data.*
import xyz.cssxsh.mirai.spi.*
import java.time.*

public object MiraiCurfewTimer : GroupCurfewTimer, MiraiCurfewTimerConfig by AdminTimerData {
    override val level: Int = 0
    override val id: String = "curfew-timer"
    private val sleep: MutableMap<Long, Boolean> = HashMap()

    override fun moment(contact: Group): LocalTime? {
        val now: LocalTime = LocalTime.now()
        val next: LocalTime = now.plusMinutes(check)
        val range: LocalTimeRange = muted[contact.id] ?: return next
        return listOf(next, range.start, range.endInclusive).minByOrNull { wait(now, it) }
    }

    override suspend fun run(contact: Group): Boolean? {
        if (sleep[contact.id] ?: false) return null
        val range: LocalTimeRange = muted[contact.id] ?: return null
        return LocalTime.now() in range
    }

    override fun sleep(contact: Group, state: Boolean?) {
        sleep[contact.id] = state ?: !(sleep[contact.id] ?: false)
    }
}