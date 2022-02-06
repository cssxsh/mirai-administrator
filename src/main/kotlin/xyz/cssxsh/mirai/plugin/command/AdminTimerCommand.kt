package xyz.cssxsh.mirai.plugin.command

import net.mamoe.mirai.console.command.*
import net.mamoe.mirai.console.util.ContactUtils.render
import net.mamoe.mirai.contact.*
import xyz.cssxsh.mirai.plugin.*
import xyz.cssxsh.mirai.plugin.data.*
import java.time.*

public object AdminTimerCommand : CompositeCommand(
    owner = MiraiAdminPlugin,
    primaryName = "timer",
    description = "定时器相关指令"
) {
    @SubCommand
    @Description("检查周期")
    public suspend fun CommandSender.check(minute: Long) {
        AdminTimerData.check = minute.coerceAtLeast(1)

        sendMessage("定时器检查周期更改为 $minute minute")
    }

    @SubCommand
    @Description("宵禁")
    public suspend fun CommandSender.mute(start: LocalTime, end: LocalTime, group: Group? = subject as? Group) {
        if (group == null) {
            sendMessage("未指定群")
            return
        }

        if (start != end) {
            val range = LocalTimeRange(start, end)
            AdminTimerData.muted[group.id] = range
            sendMessage("${group.render()} 宵禁 $range 将生效")
        } else {
            val range = AdminTimerData.muted.remove(group.id)
            sendMessage("${group.render()} 宵禁 $range 被取消")
        }
    }

    @SubCommand
    @Description("清理不发言")
    public suspend fun CommandSender.cleaner(day: Long, group: Group? = subject as? Group) {
        if (group == null) {
            sendMessage("未指定群")
            return
        }

        if (day > 0) {
            AdminTimerData.last[group.id] = day
            sendMessage("${group.render()} 清理不发言 开启，不发言期限 $day day")
        } else {
            AdminTimerData.last.remove(group.id)
            sendMessage("${group.render()} 清理不发言 关闭")
        }
    }
}