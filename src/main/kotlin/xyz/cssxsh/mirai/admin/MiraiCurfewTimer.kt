package xyz.cssxsh.mirai.admin

import net.mamoe.mirai.contact.*
import xyz.cssxsh.mirai.admin.data.*
import xyz.cssxsh.mirai.spi.*
import java.time.*

public object MiraiCurfewTimer : GroupCurfewTimer, MiraiCurfewTimerConfig by AdminTimerData {
    override val level: Int = 0
    override val id: String = "curfew-timer"

    override fun moment(contact: Group): LocalTime? {
        val now: LocalTime = LocalTime.now()
        val next: LocalTime = now.plusMinutes(check)
        val range: LocalTimeRange = muted[contact.id] ?: return next
        return listOf(next, range.start, range.endInclusive).minByOrNull { wait(now, it) }
    }

    override suspend fun run(contact: Group): Boolean? {
        if (System.currentTimeMillis() < (sleep[contact.id] ?: 0)) return null
        val range: LocalTimeRange = muted[contact.id] ?: return null
        return LocalTime.now() in range
    }

    override fun ignore(contact: Group) {
        val now: LocalTime = LocalTime.now()
        val range: LocalTimeRange = muted[contact.id] ?: return
        val millis = listOf(range.start, range.endInclusive).minOf { wait(now, it) }
        sleep[contact.id] = System.currentTimeMillis() + millis
    }
}