package xyz.cssxsh.mirai.admin.data

import net.mamoe.mirai.console.data.*
import xyz.cssxsh.mirai.admin.cron.*

public object AdminTimerData : AutoSavePluginData("AdminTimerData") {

    public val last: MutableMap<Long, Long> by value()

    public val clear: MutableMap<Long, DataCron> by value()

    public val moment: MutableMap<Long, Int> by value()

    public val mute: MutableMap<Long, DataCron> by value()

    public val status: MutableMap<Long, DataCron> by value()
}
