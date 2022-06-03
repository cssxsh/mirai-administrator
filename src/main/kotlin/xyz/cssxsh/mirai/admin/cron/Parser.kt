package xyz.cssxsh.mirai.admin.cron

import com.cronutils.model.*
import com.cronutils.model.definition.*
import com.cronutils.parser.*
import net.mamoe.mirai.console.command.descriptor.*
import java.time.*

internal const val CRON_TYPE_KEY = "xyz.cssxsh.mirai.admin.cron"

public val DefaultCronParser: CronParser by lazy {
    val type = System.getProperty(CRON_TYPE_KEY, "QUARTZ")
    CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.valueOf(type)))
}

public fun Cron.asData(): DataCron = DataCron(delegate = this)

public val CronCommandArgumentContext: CommandArgumentContext = buildCommandArgumentContext {
    Cron::class with { text ->
        try {
            DefaultCronParser.parse(text)
        } catch (cause: Throwable) {
            throw CommandArgumentParserException(
                message = cause.message ?: "Cron 表达式读取错误，建议找在线表达式生成器生成",
                cause = cause
            )
        }
    }
    Duration::class with { text ->
        try {
            Duration.parse(text)
        } catch (cause: Throwable) {
            throw CommandArgumentParserException(
                message = cause.message ?: "Duration 表达式格式为 PnDTnHnMn.nS",
                cause = cause
            )
        }
    }
}