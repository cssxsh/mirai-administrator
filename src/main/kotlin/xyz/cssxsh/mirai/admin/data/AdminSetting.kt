package xyz.cssxsh.mirai.admin.data

import kotlinx.coroutines.*
import net.mamoe.mirai.console.data.*
import net.mamoe.mirai.console.plugin.jvm.*
import net.mamoe.mirai.console.util.*
import xyz.cssxsh.mirai.admin.*
import java.nio.file.*
import java.util.WeakHashMap
import kotlin.io.path.*

public object AdminSetting : ReadOnlyPluginConfig("AdminSetting"), MiraiContentCensorConfig {

    internal const val OWNER_DEFAULT = 12345L

    private val list: MutableMap<String, List<String>> = HashMap()

    private val cache: MutableMap<String, Regex> = WeakHashMap()

    override val censorRegex: Sequence<Regex> = sequence {
        for ((_, patterns) in list) {
            for (pattern in patterns) {
                if (pattern.isEmpty()) continue
                val regex = try {
                    cache.getOrPut(pattern) { pattern.toRegex() }
                } catch (_: Exception) {
                    continue
                }

                yield(regex)
            }
        }
    }

    @ConsoleExperimentalApi
    override fun onInit(owner: PluginDataHolder, storage: PluginDataStorage) {
        val plugin = owner as? JvmPlugin ?: return
        val folder = plugin.resolveConfigPath("censor")
        folder.toFile().mkdirs()

        for (path in folder.listDirectoryEntries()) {
            if (!path.name.endsWith(".txt")) continue
            plugin.logger.info("读取审核库 ${path.fileName}")

            try {
                list[path.name] = path.readLines()
            } catch (cause: Exception) {
                plugin.logger.warning("读取失败 $path", cause)
            }
        }

        val watcher = folder.fileSystem.newWatchService()
        val kinds = arrayOf(
            StandardWatchEventKinds.ENTRY_CREATE,
            StandardWatchEventKinds.ENTRY_DELETE,
            StandardWatchEventKinds.ENTRY_MODIFY
        )
        folder.register(watcher, kinds, com.sun.nio.file.SensitivityWatchEventModifier.LOW)

        plugin.launch {
            while (isActive) {
                val key = runInterruptible(Dispatchers.IO, watcher::take)
                for (event in key.pollEvents()) {
                    val path = event.context() as? Path ?: continue
                    if (!path.toString().endsWith(".txt")) continue

                    when (event.kind()) {
                        StandardWatchEventKinds.ENTRY_CREATE,
                        StandardWatchEventKinds.ENTRY_MODIFY -> {
                            val file = folder.resolve(path)
                            if (!file.exists()) {
                                plugin.logger.info("移除审核库 $path , ${event.kind().name()}")
                                list.remove(file.name)
                                continue
                            }
                            plugin.logger.info("更新审核库 $path , ${event.kind().name()}")
                            try {
                                list[file.name] = file.readLines()
                            } catch (cause: Exception) {
                                plugin.logger.warning("更新审核库 $file", cause)
                            }
                        }
                        StandardWatchEventKinds.ENTRY_CREATE -> {
                            list.remove(path.name)
                        }
                        else -> Unit
                    }
                }
                key.reset()
            }
        }
    }

    @ValueName("owner")
    @ValueDescription("机器人所有者")
    public val owner: Long by value(OWNER_DEFAULT)

    @ValueName("censor_regex")
    @ValueDescription("配置项废除, 改为加载 censor 文件夹中的 txt 文件")
    @Suppress("unused")
    private val censorRegexOld: String by value("")

    @ValueName("censor_types")
    @ValueDescription("消息审查，类型, IMAGE, FLASH, SERVICE, APP, AUDIO, FORWARD, VIP, MARKET, MUSIC, POKE")
    override val censorTypes: Set<MiraiContentType> by value()

    @ValueName("censor_mute")
    @ValueDescription("消息审查，禁言时间 单位秒")
    override val censorMute: Int by value(0)

    @ValueName("record_limit")
    @ValueDescription("联系人（群/好友/...）聊天记录上限，（用于消息撤销等操作）")
    public val recordLimit: Int by value(100)
}