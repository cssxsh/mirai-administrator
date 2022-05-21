package xyz.cssxsh.mirai.admin

import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.message.data.MessageSource.Key.recall
import xyz.cssxsh.mirai.admin.data.*
import xyz.cssxsh.mirai.spi.*

public object MiraiContentCensor : ContentCensor, MiraiContentCensorConfig by AdminSetting {
    override val level: Int = 0
    override val id: String = "default-censor"

    override suspend fun handle(event: GroupMessageEvent): Boolean {
        if (censorTypes.any { event.message.contains(it.key) }) {
            event.group.sendMessage("触发消息类型审查")
            event.message.recall()

            if (censorMute > 0) event.sender.mute(censorMute)
        }
        if (censorRegex.isEmpty()) return false

        if (censorRegex.toRegex() in event.message.contentToString()) {
            event.group.sendMessage("触发消息正则审查")
            event.message.recall()

            if (censorMute > 0) event.sender.mute(censorMute)
        }

        return false
    }

    override suspend fun handle(event: NudgeEvent): Boolean = false
}