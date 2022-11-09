package xyz.cssxsh.mirai.admin

import com.cronutils.model.*
import kotlinx.coroutines.*
import net.mamoe.mirai.contact.*
import xyz.cssxsh.mirai.admin.cron.*
import xyz.cssxsh.mirai.admin.data.*
import xyz.cssxsh.mirai.spi.*
import java.time.*

@PublishedApi
internal object MiraiMemberCleaner : MemberCleaner {
    override val level: Int = 0
    override val id: String = "cleaner"
    override val records: MutableMap<Long, Job> = java.util.concurrent.ConcurrentHashMap()
    private val last: Map<Long, Long> get() = AdminTimerData.last
    private val settings: Map<Long, Cron> get() = AdminTimerData.clear

    override fun wait(contact: Group): Long? {
        val cron = settings[contact.id] ?: return null
        return cron.toExecutionTime()
            .timeToNextExecution(ZonedDateTime.now())
            .orElse(Duration.ZERO)
            .toMillis()
            .coerceAtLeast(1_000)
    }

    override suspend fun run(contact: Group): List<Pair<NormalMember, String>> {
        val day = last[contact.id] ?: return emptyList()
        if (day <= 0) return emptyList()
        val limit = LocalDateTime.now().minusDays(day)
        return contact.members.mapNotNull { member ->
            if (member.permission == MemberPermission.MEMBER && member.lastSpeakAt > limit) {
                null
            } else {
                member to "自 ${member.lastSpeakAt} 起，${day} 天未发言"
            }
        }
    }
}