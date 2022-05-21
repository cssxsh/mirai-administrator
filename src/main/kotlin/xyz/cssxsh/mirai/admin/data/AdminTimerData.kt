package xyz.cssxsh.mirai.admin.data

import net.mamoe.mirai.console.data.*
import xyz.cssxsh.mirai.admin.*

public object AdminTimerData : AutoSavePluginData("AdminTimerData"),
    MiraiCurfewTimerConfig, MiraiMemberCleanerConfig {

    override var check: Long by value(3L)

    override val last: MutableMap<Long, Long> by value()

    override val muted: MutableMap<Long, LocalTimeRange> by value()

    override val sleep: MutableMap<Long, Long> by value()
}
