package xyz.cssxsh.mirai.admin.command

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.http.*
import net.mamoe.mirai.*
import net.mamoe.mirai.console.command.*
import net.mamoe.mirai.console.util.ContactUtils.render
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.contact.announcement.*
import net.mamoe.mirai.internal.*
import net.mamoe.mirai.internal.network.*
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.message.data.Image.Key.queryUrl
import net.mamoe.mirai.utils.*
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import xyz.cssxsh.mirai.admin.*
import java.util.*

/**
 * 群组相关指令
 */
public object AdminGroupCommand : CompositeCommand(
    owner = MiraiAdminPlugin,
    primaryName = "group",
    description = "群处理相关操作"
) {
    @SubCommand
    @Description("群列表")
    public suspend fun CommandSender.list() {
        val message = try {
            buildString {
                for (bot in Bot.instances) {
                    appendLine("--- ${bot.render()} ---")
                    for (group in bot.groups) {
                        appendLine("${group.render()}[${group.botPermission}]<${group.members.size}>")
                    }
                }
            }
        } catch (cause: Exception) {
            logger.warning({ "出现错误" }, cause)
            "出现错误"
        }

        sendMessage(message)
    }

    @SubCommand
    @Description("群成员")
    public suspend fun CommandSender.member(group: Group) {
        val message = try {
            buildString {
                appendLine("--- ${group.render()} ---")
                for (member in group.members) {
                    appendLine("${member.render()}[${member.permission}](${member.joinAt}~${member.lastSpeakAt})")
                }
            }
        } catch (cause: Exception) {
            logger.warning({ "出现错误" }, cause)
            "出现错误"
        }

        sendMessage(message)
    }

    @SubCommand
    @Description("退出群聊")
    public suspend fun CommandSender.quit(group: Group) {
        val message = try {
            group.quit()
            "退出成功"
        } catch (cause: Exception) {
            logger.warning({ "退出错误" }, cause)
            "退出错误"
        }

        sendMessage(message)
    }

    @SubCommand
    @Description("踢出群员")
    public suspend fun CommandSender.kick(member: NormalMember, reason: String = "", black: Boolean = false) {
        val message = try {
            member.kick(message = reason, block = black)
            "踢出成功"
        } catch (exception: PermissionDeniedException) {
            logger.warning({ "权限不足" }, exception)
            "权限不足"
        } catch (cause: Exception) {
            logger.warning({ "踢出错误" }, cause)
            "踢出错误"
        }

        sendMessage(message)
    }

    @SubCommand
    @Description("群昵称")
    public suspend fun CommandSender.nick(member: NormalMember, nick: String) {
        val message = try {
            member.nameCard = nick
            "设置成功"
        } catch (exception: PermissionDeniedException) {
            logger.warning({ "权限不足" }, exception)
            "权限不足"
        } catch (cause: Exception) {
            logger.warning({ "设置错误" }, cause)
            "设置错误"
        }

        sendMessage(message)
    }

    @SubCommand
    @Description("群头衔")
    public suspend fun CommandSender.title(member: NormalMember, title: String) {
        val message = try {
            member.specialTitle = title
            "设置成功"
        } catch (exception: PermissionDeniedException) {
            logger.warning({ "权限不足" }, exception)
            "权限不足"
        } catch (cause: Exception) {
            logger.warning({ "设置错误" }, cause)
            "设置错误"
        }

        sendMessage(message)
    }

    @SubCommand
    @Description("禁言")
    public suspend fun CommandSender.mute(member: Member, second: Int) {
        val message = try {
            if (second > 0) {
                member.mute(durationSeconds = second)
                "禁言成功"
            } else {
                if (member is NormalMember) {
                    member.unmute()
                } else {
                    member.mute(durationSeconds = 0)
                }
                "解除成功"
            }
        } catch (exception: PermissionDeniedException) {
            logger.warning({ "权限不足" }, exception)
            "权限不足"
        } catch (cause: Exception) {
            logger.warning({ "设置错误" }, cause)
            "设置错误"
        }

        sendMessage(message)
    }

    @SubCommand("quiet")
    @Description("全体禁言")
    public suspend fun CommandSender.quiet(group: Group, open: Boolean = true) {
        val message = try {
            group.settings.isMuteAll = open
            "设置全体禁言禁言成功"
        } catch (exception: PermissionDeniedException) {
            logger.warning({ "权限不足" }, exception)
            "权限不足"
        } catch (cause: Exception) {
            logger.warning({ "设置错误" }, cause)
            "设置错误"
        }

        sendMessage(message)
    }

    @SubCommand
    @Description("设置管理员")
    public suspend fun CommandSender.admin(member: NormalMember, operation: Boolean = true) {
        val message = try {
            member.modifyAdmin(operation = operation)
            "设置成功"
        } catch (exception: PermissionDeniedException) {
            logger.warning({ "权限不足" }, exception)
            "权限不足"
        } catch (cause: Exception) {
            logger.warning({ "设置错误" }, cause)
            "设置错误"
        }

        sendMessage(message)
    }

    @SubCommand
    @Description("设置公告")
    public suspend fun CommandSender.announce(group: Group) {
        val message = try {
            val message = request(hint = "请输入公告内容")
            val text = message.filterIsInstance<PlainText>()
            val content = text[0].content
            val properties = Properties().apply {
                load(text.getOrNull(1)?.content.orEmpty().reader())
            }
            val image = message.findIsInstance<Image>()?.let {
                http.prepareGet(urlString = it.queryUrl()).execute { response ->
                    group.announcements.uploadImage(resource = response.body<ByteArray>().toExternalResource())
                }
            }
            group.announcements.publish(OfflineAnnouncement(content = content) {
                this.image = image
                this.sendToNewMember = (properties["send"] ?: properties["sendToNewMember"])
                    .toString().toBooleanStrictOrNull() ?: false
                this.isPinned = (properties["pinned"] ?: properties["isPinned"])
                    .toString().toBooleanStrictOrNull() ?: false
                this.showEditCard = (properties["edit"] ?: properties["showEditCard"])
                    .toString().toBooleanStrictOrNull() ?: false
                this.showPopup = (properties["popup"] ?: properties["showPopup"])
                    .toString().toBooleanStrictOrNull() ?: false
                this.requireConfirmation = (properties["confirmation"] ?: properties["requireConfirmation"])
                    .toString().toBooleanStrictOrNull() ?: false
            })
            "设置成功"
        } catch (exception: PermissionDeniedException) {
            logger.warning({ "权限不足" }, exception)
            "权限不足"
        } catch (cause: Exception) {
            logger.warning({ "设置错误" }, cause)
            "设置错误"
        }

        sendMessage(message)
    }

    @SubCommand
    @Description("设置等级头衔")
    public suspend fun CommandSender.rank(group: Group, vararg levels: String) {
        if (levels.size !in 1..6) throw IllegalCommandArgumentException("等级词条数量不对")
        val message = try {
            group.active.setRankTitles(levels.withIndex().associate { it.index to it.value })
            "设置成功"
        } catch (exception: PermissionDeniedException) {
            logger.warning({ "权限不足" }, exception)
            "权限不足"
        } catch (cause: Exception) {
            logger.warning({ "设置错误" }, cause)
            "设置错误"
        }

        sendMessage(message)
    }
}