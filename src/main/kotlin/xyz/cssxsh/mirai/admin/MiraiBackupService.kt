package xyz.cssxsh.mirai.admin

import io.ktor.utils.io.core.*
import net.mamoe.mirai.*
import net.mamoe.mirai.console.util.ContactUtils.render
import net.mamoe.mirai.contact.*
import org.apache.poi.ss.usermodel.*
import xyz.cssxsh.mirai.admin.poi.*
import xyz.cssxsh.mirai.spi.*
import java.io.*
import java.util.zip.*

@PublishedApi
internal object MiraiBackupService : BackupService {
    override val level: Int = 0
    override val id: String = "default-backup"

    override fun group() {
        val backup = File("backup/group.${System.currentTimeMillis()}.zip")
        backup.parentFile.mkdirs()
        backup.outputStream().buffered().use { buffered ->
            val output = ZipOutputStream(buffered)

            for (bot in Bot.instances) {
                val workbook = workbook {
                    for (group in bot.groups) {
                        val iso = creationHelper.createDataFormat().getFormat("yyyy/MM/ddThh:mm:dd")
                        sheet(group.render()) {
                            createFreezePane(0, 1, 0, 1)
                            defaultColumnWidth = 20

                            val header =
                                listOf("QQ", "NAME", "PERMISSION", "SPECIAL_TITLE", "TEMPERATURE", "JOIN", "LAST_SPEAK")
                            row(0) {
                                header.forEachIndexed { index, name ->
                                    cell(index) {
                                        setCellValue(name)
                                        style {
                                            font {
                                                alignment = HorizontalAlignment.CENTER
                                            }
                                        }
                                    }
                                }
                            }
                            var line = 1
                            for (member in group.members) {
                                row(line++) {
                                    var col = 0
                                    // QQ
                                    cell(col++) {
                                        setCellValue("${member.id}")
                                    }
                                    // NAME
                                    cell(col++) {
                                        setCellValue(member.nameCardOrNick)
                                    }
                                    // PERMISSION
                                    cell(col++) {
                                        setCellValue(member.permission.name)
                                    }
                                    // SPECIAL_TITLE
                                    cell(col++) {
                                        setCellValue(member.specialTitle)
                                    }
                                    // TEMPERATURE
                                    cell(col++) {
                                        setCellValue(member.active.temperature.toDouble())
                                    }
                                    // JOIN_AT
                                    cell(col++) {
                                        style {
                                            dataFormat = iso
                                        }
                                        setCellValue(member.joinAt)
                                    }
                                    // LAST_SPEAK_AT
                                    cell(col) {
                                        style {
                                            dataFormat = iso
                                        }
                                        setCellValue(member.lastSpeakAt)
                                    }
                                }
                            }
                        }
                    }
                }
                output.putNextEntry(ZipEntry("${bot.id}.group.xlsx"))
                try {
                    workbook.write(output)
                } finally {
                    output.closeEntry()
                    workbook.close()
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

            for (bot in Bot.instances) {
                val workbook = workbook {
                    val header = listOf("QQ", "NAME", "REMARK")
                    for (friendGroup in bot.friendGroups.asCollection()) {
                        sheet(friendGroup.name) {
                            createFreezePane(0, 1, 0, 1)
                            defaultColumnWidth = 20

                            row(0) {
                                header.forEachIndexed { index, name ->
                                    cell(index) {
                                        setCellValue(name)
                                        style {
                                            font {
                                                alignment = HorizontalAlignment.CENTER
                                            }
                                        }
                                    }
                                }
                            }
                            var line = 1
                            for (friend in friendGroup.friends) {
                                row(line++) {
                                    var col = 0
                                    // QQ
                                    cell(col++) {
                                        setCellValue("${friend.id}")
                                    }
                                    // NAME
                                    cell(col++) {
                                        setCellValue(friend.remarkOrNick)
                                    }
                                    // REMARK
                                    cell(col) {
                                        setCellValue(friend.remark)
                                    }
                                }
                            }
                        }
                    }
                }
                output.putNextEntry(ZipEntry("${bot.id}.friend.xlsx"))
                try {
                    workbook.write(output)
                } finally {
                    output.closeEntry()
                    workbook.close()
                }
            }
            output.close()
        }
    }

    override fun bot() {
        val backup = File("backup/bot.${System.currentTimeMillis()}.zip")
        backup.parentFile.mkdirs()
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