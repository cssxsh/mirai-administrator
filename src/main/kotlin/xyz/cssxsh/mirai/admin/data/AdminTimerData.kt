package xyz.cssxsh.mirai.admin.data

import kotlinx.serialization.modules.*
import net.mamoe.mirai.console.data.*
import net.mamoe.mirai.console.plugin.jvm.*
import net.mamoe.mirai.console.util.*
import xyz.cssxsh.mirai.admin.cron.*
import java.io.*
import java.time.*

@PublishedApi
internal object AdminTimerData : AutoSavePluginData("AdminTimerData") {

    override val serializersModule: SerializersModule = SerializersModule {
        contextual(DataCron)
        contextual(DurationSerializer)
    }

    var folder: File = File("./message")
        private set

    val last: MutableMap<Long, Long> by value()

    val clear: MutableMap<Long, DataCron> by value()

    val moments: MutableMap<Long, Duration> by value()

    val mute: MutableMap<Long, DataCron> by value()

    val status: MutableMap<Long, DataCron> by value()

    val message: MutableMap<Long, List<DataCron>> by value { this[12345] = emptyList() }

    @ConsoleExperimentalApi
    override fun onInit(owner: PluginDataHolder, storage: PluginDataStorage) {
        if (owner is AbstractJvmPlugin) {
            folder = owner.dataFolder
        }
        super.onInit(owner, storage)
    }
}
