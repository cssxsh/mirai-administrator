package xyz.cssxsh.mirai.admin.data

import com.cronutils.model.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.modules.*
import net.mamoe.mirai.console.data.*
import xyz.cssxsh.mirai.admin.*

public object AdminTimerData : AutoSavePluginData("AdminTimerData") {

    override val serializersModule: SerializersModule = SerializersModule {
        contextual(Cron::class, CronSerializer)
        @OptIn(ExperimentalSerializationApi::class)
        polymorphicDefaultSerializer(Cron::class) { CronSerializer }
    }

    public val last: MutableMap<Long, Long> by value()

    public val clear: MutableMap<Long, Cron> by value()

    public val moment: MutableMap<Long, Int> by value()

    public val mute: MutableMap<Long, Cron> by value()

    public val status: MutableMap<Long, Cron> by value()
}
