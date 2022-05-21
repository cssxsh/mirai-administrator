package xyz.cssxsh.mirai.admin

import net.mamoe.mirai.contact.*
import xyz.cssxsh.mirai.admin.data.*
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
            val last = member.lastSpeakAt
            val limit = LocalDateTime.now().minusDays(day)
            if (last > limit) {
                null
            } else {
                member to "自 ${member.lastSpeakAt} 起，${day}天未发言"
            }
        }
    }
}