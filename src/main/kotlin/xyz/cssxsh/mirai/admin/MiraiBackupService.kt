package xyz.cssxsh.mirai.admin

import io.ktor.utils.io.core.*
import io.ktor.utils.io.streams.*
import net.mamoe.mirai.*
import net.mamoe.mirai.contact.*
import xyz.cssxsh.mirai.spi.*
import java.io.File
import java.util.zip.*
import kotlin.io.use

public object MiraiBackupService : BackupService {
    override val level: Int = 0
    override val id: String = "default-backup"

    private fun ZipOutputStream.writeEntry(name: String, block: BytePacketBuilder.() -> Unit) {
        putNextEntry(ZipEntry(name))
        writePacket(block)
        closeEntry()
    }

    override fun group() {
        val backup = File("backup/group.${System.currentTimeMillis()}.zip")
        backup.parentFile.mkdirs()
        backup.outputStream().buffered().use { buffered ->
            val output = ZipOutputStream(buffered)

            output.writeEntry("readme.txt") {
                append("csv 文件可以用 excel 或 wps 打开，或者你可以直接当成普通的文本文件编辑")
            }

            for (bot in Bot.instances) {
                for (group in bot.groups) {
                    output.writeEntry("${bot.id}.${group.id}.group.csv") {
                        append("group, uid, name").append('\n')

                        for (member in group.members) {
                            append(group.id.toString())
                                .append(", ")
                                .append(member.id.toString())
                                .append(", ")
                                .append(member.nameCardOrNick)
                                .append('\n')
                        }
                    }
                }
            }
            output.close()
        }
    }

    override fun friend() {
        val backup = File("backup/friend.${System.currentTimeMillis()}.zip")
        backup.parentFile.mkdirs()
        backup.outputStream().buffered().use { buffered ->
            val output = ZipOutputStream(buffered)

            output.writeEntry("readme.txt") {
                append("csv 文件可以用 excel 或 wps 打开，或者你可以直接当成普通的文本文件编辑")
            }

            for (bot in Bot.instances) {

                output.writeEntry("${bot.id}.friend.csv") {
                    append("bot, uid, name, group").append('\n')

                    for (friend in bot.friends) {
                        append(bot.id.toString())
                            .append(", ")
                            .append(friend.id.toString())
                            .append(", ")
                            .append(friend.remarkOrNick)
                            .append(", ")
                            .append(friend.friendGroup.name)
                            .append('\n')
                    }
                }
            }
            output.close()
        }
    }

    override fun bot() {
        val backup = File("backup/bot.${System.currentTimeMillis()}.zip")
        backup.outputStream().buffered(1 shl 23).use { buffered ->
            val output = ZipOutputStream(buffered)
            val bots = File("bots")
            for (bot in bots.listFiles().orEmpty()) {
                val device = bot.resolve("device.json")
                if (device.exists()) {
                    val entry = ZipEntry("bots/${bot.name}/device.json")
                    entry.time = device.lastModified()
                    output.putNextEntry(entry)
                    device.inputStream().use { input -> input.transferTo(output) }
                    output.closeEntry()
                }

                val cache = bot.resolve("cache")
                for (file in cache.listFiles() ?: continue) {
                    if (file.isDirectory) continue
                    val entry = ZipEntry("bots/${bot.name}/cache/${file.name}")
                    entry.time = file.lastModified()
                    output.putNextEntry(entry)
                    file.inputStream().use { input -> input.transferTo(output) }
                    output.closeEntry()
                }
                output.flush()
            }

            val yml = File("config/Console/AutoLogin.yml")
            if (yml.exists()) {
                val entry = ZipEntry("config/Console/AutoLogin.yml")
                entry.time = yml.lastModified()
                output.putNextEntry(entry)
                yml.inputStream().use { input -> input.transferTo(output) }
                output.closeEntry()
            }
            output.close()
        }
    }
}