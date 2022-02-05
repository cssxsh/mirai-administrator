package xyz.cssxsh.mirai.plugin.data

import net.mamoe.mirai.console.data.*
import xyz.cssxsh.mirai.plugin.*

public object AdminTimerData : AutoSavePluginData("AdminTimerData"),
    MiraiCurfewTimerConfig, MiraiMemberCleanerConfig {

    override var check: Long by value(3L)

    override val last: MutableMap<Long, Long> by value()

    override val muted: MutableMap<Long, LocalTimeRange> by value()
}
