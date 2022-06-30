package xyz.cssxsh.mirai.admin

import com.cronutils.model.*
import net.mamoe.mirai.contact.*
import xyz.cssxsh.mirai.admin.cron.*
import xyz.cssxsh.mirai.admin.data.*
import xyz.cssxsh.mirai.spi.*
import java.time.*

public object MiraiCurfewTimer : GroupCurfewTimer {
    override val level: Int = 0
    override val id: String = "curfew-timer"
    private val moments: Map<Long, Duration> get() = AdminTimerData.moments
    private val settings: Map<Long, Cron> get() = AdminTimerData.mute

    override fun wait(contact: Group): Long? {
        val cron = settings[contact.id] ?: return null
        return cron.toExecutionTime()
            .timeToNextExecution(ZonedDateTime.now())
            .orElse(Duration.ZERO)
            .toMillis()
            .coerceAtLeast(1_000)
    }

    override suspend fun run(contact: Group): Long? {
        val moment = moments[contact.id] ?: return null

        contact.sendMessage("宵禁将开始，${moment} 后解禁")

        return moment.toMillis()
    }
}