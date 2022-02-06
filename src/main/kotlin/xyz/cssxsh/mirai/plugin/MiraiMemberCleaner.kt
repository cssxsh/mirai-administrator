package xyz.cssxsh.mirai.plugin

import net.mamoe.mirai.contact.*
import xyz.cssxsh.mirai.plugin.data.*
import xyz.cssxsh.mirai.spi.*
import java.time.*

public object MiraiMemberCleaner : MemberCleaner, MiraiMemberCleanerConfig by AdminTimerData {
    override val level: Int = 0
    override val id: String = "cleaner"

    override fun moment(contact: Group): LocalTime {
        return LocalTime.now().plusMinutes(check)
    }

    override suspend fun run(contact: Group): List<Pair<NormalMember, String>> {
        val day = last[contact.id] ?: return emptyList()
        return contact.members.mapNotNull { member ->
            val second = (System.currentTimeMillis() / 1000 - member.lastSpeakTimestamp)
            val start = LocalDateTime.now().minusSeconds(second)
            val last = LocalDateTime.now().minusDays(day)
            if (last > start) {
                null
            } else {
                member to "自 $start 起，长时间未发言"
            }
        }
    }
}