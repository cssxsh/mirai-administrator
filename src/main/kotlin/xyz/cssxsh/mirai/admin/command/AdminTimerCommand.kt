package xyz.cssxsh.mirai.admin.command

import com.cronutils.descriptor.*
import com.cronutils.model.*
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.command.*
import net.mamoe.mirai.console.util.ContactUtils.render
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.message.data.*
import xyz.cssxsh.mirai.admin.*
import xyz.cssxsh.mirai.admin.cron.*
import xyz.cssxsh.mirai.admin.data.*
import java.time.*
import java.util.*

public object AdminTimerCommand : CompositeCommand(
    owner = MiraiAdminPlugin,
    primaryName = "timer",
    description = "定时器相关指令",
    overrideContext = CronCommandArgumentContext
) {
    private val descriptor = CronDescriptor.instance(Locale.getDefault())

    @SubCommand
    @Description("设置")
    public suspend fun CommandSender.config() {
        sendMessage(message = buildMessageChain {
            //
            appendLine("宵禁:")
            for ((group, cron) in AdminTimerData.mute) {
                appendLine("Group($group) - ${AdminTimerData.moments[group]} minute")
                appendLine(descriptor.describe(cron))
            }
            appendLine()
            //
            appendLine("清理:")
            for ((group, cron) in AdminTimerData.clear) {
                appendLine("Group($group) - ${AdminTimerData.last[group]} day")
                appendLine(descriptor.describe(cron))
            }
            appendLine()
            //
            appendLine("状态:")
            for ((bot, cron) in AdminTimerData.status) {
                appendLine("Bot($bot)")
                appendLine(descriptor.describe(cron))
            }
        })
    }

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
                append(descriptor.describe(cron))
            })
        } else {
            AdminTimerData.mute.remove(group.id)
            AdminTimerData.moments.remove(group.id)
            sendMessage("${group.render()} 宵禁 关闭")
        }
    }

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
                append(descriptor.describe(cron))
            })
        } else {
            AdminTimerData.last.remove(group.id)
            AdminTimerData.clear.remove(group.id)
            sendMessage("${group.render()} 清理不发言 关闭")
        }
    }

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
            append(descriptor.describe(cron))
        })
    }
}