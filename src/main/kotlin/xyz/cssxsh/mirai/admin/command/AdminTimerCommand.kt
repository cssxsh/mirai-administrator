package xyz.cssxsh.mirai.admin.command

import com.cronutils.model.*
import net.mamoe.mirai.*
import net.mamoe.mirai.console.command.*
import net.mamoe.mirai.console.util.ContactUtils.render
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.*
import xyz.cssxsh.mirai.admin.*
import xyz.cssxsh.mirai.admin.cron.*
import xyz.cssxsh.mirai.admin.data.*
import java.time.*

/**
 * 定时器相关指令
 */
public object AdminTimerCommand : CompositeCommand(
    owner = MiraiAdminPlugin,
    primaryName = "timer",
    description = "定时器相关指令",
    overrideContext = CronCommandArgumentContext
) {
    /**
     * 打印定时设置
     */
    @SubCommand
    @Description("设置")
    public suspend fun CommandSender.config() {
        sendMessage(message = buildMessageChain {
            //
            appendLine("# 宵禁:")
            for ((group, cron) in AdminTimerData.mute) {
                appendLine("## Group($group) - ${AdminTimerData.moments[group]}")
                appendLine(cron.description())
            }
            appendLine()
            //
            appendLine("# 清理:")
            for ((group, cron) in AdminTimerData.clear) {
                appendLine("## Group($group) - ${AdminTimerData.last[group]} day")
                appendLine(cron.description())
            }
            appendLine()
            //
            appendLine("# 状态:")
            for ((bot, cron) in AdminTimerData.status) {
                appendLine("## Bot($bot)")
                appendLine(cron.description())
            }
            //
            appendLine("# 定时:")
            for ((group, list) in AdminTimerData.message) {
                if (list.isEmpty()) continue
                appendLine("## Group($group)")
                for (cron in list) {
                    appendLine(cron.description())
                }
            }
        })
    }

    /**
     * 设置宵禁
     * @param moment 时长
     * @param cron 启用的时间点
     * @param group 目标群
     */
    @SubCommand
    @Description("宵禁")
    public suspend fun CommandSender.mute(moment: Duration, cron: Cron, group: Group? = subject as? Group) {
        if (group == null) {
            sendMessage("未指定群")
            return
        }

        if (moment > Duration.ZERO) {
            AdminTimerData.mute[group.id] = cron.asData()
            AdminTimerData.moments[group.id] = moment
            with(MiraiAdministrator) {
                MiraiCurfewTimer.start(group)
            }

            sendMessage(message = buildString {
                appendLine("${group.render()} 宵禁 $moment 将生效于")
                append(cron.description())
            })
        } else {
            AdminTimerData.mute.remove(group.id)
            AdminTimerData.moments.remove(group.id)
            with(MiraiAdministrator) {
                MiraiCurfewTimer.start(group)
            }

            sendMessage("${group.render()} 宵禁 关闭")
        }
    }

    /**
     * 清理不发言
     * @param day 不发言时长
     * @param cron 启用的时间点
     * @param group 目标群
     */
    @SubCommand
    @Description("清理不发言")
    public suspend fun CommandSender.cleaner(day: Long, cron: Cron, group: Group? = subject as? Group) {
        if (group == null) {
            sendMessage("未指定群")
            return
        }

        if (day > 0) {
            AdminTimerData.last[group.id] = day
            AdminTimerData.clear[group.id] = cron.asData()
            with(MiraiAdministrator) {
                MiraiMemberCleaner.start(group)
            }

            sendMessage(message = buildString {
                appendLine("${group.render()} 清理不发言 开启，不发言期限 $day day")
                append(cron.description())
            })
        } else {
            AdminTimerData.last.remove(group.id)
            AdminTimerData.clear.remove(group.id)
            with(MiraiAdministrator) {
                MiraiMemberCleaner.start(group)
            }

            sendMessage("${group.render()} 清理不发言 关闭")
        }
    }

    /**
     * 定时发送机器人状态到所有者
     * @param cron 启用的时间点
     * @param from 目标机器人
     */
    @SubCommand
    @Description("定时发送机器人状态")
    public suspend fun CommandSender.status(cron: Cron, from: Bot? = bot) {
        if (from == null) {
            sendMessage("未指定机器人")
            return
        }
        AdminTimerData.status[from.id] = cron.asData()
        with(MiraiAdministrator) {
            MiraiStatusMessage.start(from)
        }

        sendMessage(message = buildString {
            appendLine("${from.render()} 状态消息 将生效于")
            append(cron.description())
        })
    }

    /**
     * 定时发送群消息
     * @param cron 启用的时间点
     * @param target 目标群
     * @param at 是否At
     */
    @SubCommand
    @Description("定时发送群消息")
    public suspend fun CommandSender.message(cron: Cron, target: Group, at: Boolean = false) {
        val message = request(hint = "请输入要发送的消息或者‘stop’终止定时") + if (at) AtAll else emptyMessageChain()
        if (message.findIsInstance<PlainText>()?.content == "stop") {
            AdminTimerData.message[target.id] = AdminTimerData.message[target.id].orEmpty()
                .filterNot { it.asString() == cron.asString() }

            sendMessage(message = "定时消息将取消")
            return
        }
        val json = with(MessageChain) {
            message.serializeToJsonString()
        }
        val uuid = "${target.id}/${cron.asString().toByteArray().toUHexString("")}.json"
        val file = AdminTimerData.folder.resolve(uuid)
        file.parentFile.mkdirs()
        file.writeText(json)
        AdminTimerData.message[target.id] = (AdminTimerData.message[target.id].orEmpty() + cron.asData())
            .distinctBy { it.asString() }

        sendMessage(message = buildString {
            appendLine("${target.render()} 定时消息 将生效于")
            append(cron.description())
        })
    }
}