package xyz.cssxsh.mirai.admin

import com.cronutils.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.*
import net.mamoe.mirai.*
import net.mamoe.mirai.message.*
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.*
import xyz.cssxsh.mirai.admin.cron.*
import xyz.cssxsh.mirai.admin.data.*
import xyz.cssxsh.mirai.spi.*
import java.io.*
import java.time.*

@PublishedApi
internal object MiraiMessageTimer : BotTimingMessage {
    override val level: Int = 0
    override val id: String = "message-timer"
    override val records: MutableMap<Long, Job> = java.util.concurrent.ConcurrentHashMap()
    private val settings: Map<Long, List<Cron>> get() = AdminTimerData.message
    private val folder: File get() = AdminTimerData.folder
    private val cache: HashSet<String> = HashSet()
    private val mutex: Mutex = Mutex()

    override fun wait(contact: Bot): Long {
        val now = ZonedDateTime.now()
        var duration = Duration.ofMillis(60_000)
        for (group in contact.groups) {
            val list = settings[group.id] ?: continue
            if (list.isEmpty()) continue
            for (cron in list) {
                val next = cron.toExecutionTime().timeToNextExecution(now)
                    .orElse(null) ?: continue
                duration = minOf(duration, next)
            }
        }
        return duration.toMillis()
    }

    override suspend fun run(contact: Bot): Flow<MessageReceipt<*>> {
        val now = ZonedDateTime.now()
        return channelFlow {
            for (group in contact.groups) {
                val list = settings[group.id] ?: continue
                if (list.isEmpty()) continue
                for (cron in list) {
                    val execution = cron.toExecutionTime()
                    if (execution.isMatch(now).not()) continue
                    val uuid = "${group.id}/${cron.asString().toByteArray().toUHexString("")}.json"
                    val file = folder.resolve(uuid)
                    if (file.exists().not()) {
                        contact.logger.warning { "$uuid Not Found" }
                        continue
                    }
                    val message = MessageChain.deserializeFromJsonString(file.readText())
                    mutex.withLock {
                        if (cache.add(uuid).not()) {
                            contact.logger.debug { "$uuid Cached" }
                            return@withLock
                        }
                        try {
                            send(group.sendMessage(message))
                            contact.launch {
                                delay(60_000)
                                cache.remove(uuid)
                            }
                        } catch (cause: Exception) {
                            contact.logger.debug({ "$uuid Exception" }, cause)
                            cache.remove(uuid)
                        }
                    }
                }
            }
        }
    }
}