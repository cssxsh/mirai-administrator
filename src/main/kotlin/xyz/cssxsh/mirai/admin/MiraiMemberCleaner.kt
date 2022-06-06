package xyz.cssxsh.mirai.admin

import com.cronutils.model.*
import net.mamoe.mirai.contact.*
import xyz.cssxsh.mirai.admin.cron.*
import xyz.cssxsh.mirai.admin.data.*
import xyz.cssxsh.mirai.spi.*
import java.time.*

public object MiraiMemberCleaner : MemberCleaner {
    override val level: Int = 0
    override val id: String = "cleaner"
    private val last: Map<Long, Long> get() = AdminTimerData.last
    private val settings: Map<Long, Cron> get() = AdminTimerData.clear

    override fun wait(contact: Group): Long? {
        val cron = settings[contact.id] ?: return null
        return cron.toExecutionTime()
            .timeToNextExecution(ZonedDateTime.now())
            .orElse(Duration.ZERO)
            .toMillis()
    }

    override suspend fun run(contact: Group): List<Pair<NormalMember, String>> {
        val day = last[contact.id] ?: return emptyList()
        val limit = LocalDateTime.now().minusDays(day)
        return contact.members.mapNotNull { member ->
            if (member.permission == MemberPermission.MEMBER && member.lastSpeakAt > limit) {
                null
            } else {
                member to "自 ${member.lastSpeakAt} 起，${day}天未发言"
            }
        }
    }
}