package xyz.cssxsh.mirai.admin

import com.cronutils.model.*
import net.mamoe.mirai.contact.*
import xyz.cssxsh.mirai.admin.data.*
import xyz.cssxsh.mirai.spi.*
import java.time.*

public object MiraiCurfewTimer : GroupCurfewTimer {
    override val level: Int = 0
    override val id: String = "curfew-timer"
    private val moment: Map<Long, Int> get() = AdminTimerData.moment
    private val settings: Map<Long, Cron> get() = AdminTimerData.mute

    override fun wait(contact: Group): Long? {
        val cron = settings[contact.id] ?: return null
        return cron.toExecutionTime()
            .timeToNextExecution(ZonedDateTime.now())
            .orElse(Duration.ZERO)
            .toMillis()
    }

    override suspend fun run(contact: Group): Int? = moment[contact.id]
}