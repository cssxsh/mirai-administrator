package xyz.cssxsh.mirai.admin.data

import kotlinx.serialization.modules.*
import net.mamoe.mirai.console.data.*
import xyz.cssxsh.mirai.admin.cron.*
import java.time.*

public object AdminTimerData : AutoSavePluginData("AdminTimerData") {

    override val serializersModule: SerializersModule = SerializersModule {
        contextual(DataCron)
        contextual(DurationSerializer)
    }

    public val last: MutableMap<Long, Long> by value()

    public val clear: MutableMap<Long, DataCron> by value()

    public val moments: MutableMap<Long, Duration> by value()

    public val mute: MutableMap<Long, DataCron> by value()

    public val status: MutableMap<Long, DataCron> by value()
}
