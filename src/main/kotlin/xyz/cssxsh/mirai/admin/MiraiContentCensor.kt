package xyz.cssxsh.mirai.admin

import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.message.data.MessageSource.Key.recall
import xyz.cssxsh.mirai.admin.data.*
import xyz.cssxsh.mirai.spi.*

public object MiraiContentCensor : ContentCensor, MiraiContentCensorConfig by AdminSetting {
    override val level: Int = 0
    override val id: String = "default-censor"

    override suspend fun handle(event: GroupMessageEvent): Boolean {
        for (type in censorTypes) {
            if (type.key !in event.message) continue
            event.group.sendMessage("触发消息类型审查")
            event.message.recall()

            if (censorMute > 0) event.sender.mute(censorMute)
            return true
        }

        val content = event.message.contentToString()
        for (regex in censorRegex) {
            if (regex !in content) continue
            event.group.sendMessage("触发消息正则审查")
            event.message.recall()

            if (censorMute > 0) event.sender.mute(censorMute)
            return true
        }

        return false
    }

    override suspend fun handle(event: NudgeEvent): Boolean = false
}