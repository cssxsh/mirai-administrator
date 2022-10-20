package xyz.cssxsh.mirai.admin.cron

import com.cronutils.descriptor.*
import com.cronutils.model.*
import com.cronutils.model.definition.*
import com.cronutils.model.time.*
import com.cronutils.parser.*
import net.mamoe.mirai.console.command.descriptor.*
import java.time.*
import java.util.*

internal const val CRON_TYPE_KEY = "xyz.cssxsh.mirai.admin.cron.type"

public val DefaultCronParser: CronParser by lazy {
    val type = CronType.valueOf(System.getProperty(CRON_TYPE_KEY, CronType.QUARTZ.name))
    CronParser(CronDefinitionBuilder.instanceDefinitionFor(type))
}

internal const val CRON_LOCALE_KEY = "xyz.cssxsh.mirai.admin.cron.locale"

public val DefaultCronDescriptor: CronDescriptor by lazy {
    val locale = System.getProperty(CRON_LOCALE_KEY)?.let { Locale.forLanguageTag(it) } ?: Locale.getDefault()
    CronDescriptor.instance(locale)
}

public fun Cron.asData(): DataCron = this as? DataCron ?: DataCron(delegate = this)

public fun Cron.toExecutionTime(): ExecutionTime = ExecutionTime.forCron((this as? DataCron)?.delegate ?: this)

public fun Cron.description(): String = DefaultCronDescriptor.describe(this)

public val CronCommandArgumentContext: CommandArgumentContext = buildCommandArgumentContext {
    Cron::class with { text ->
        try {
            DefaultCronParser.parse(text)
        } catch (cause: Exception) {
            throw CommandArgumentParserException(
                message = "Cron 表达式读取错误，建议找在线表达式生成器生成",
                cause = cause
            )
        }
    }
    Duration::class with { text ->
        try {
            Duration.parse(text)
        } catch (cause: Exception) {
            throw CommandArgumentParserException(
                message = "Duration 表达式格式为 PnDTnHnMn.nS",
                cause = cause
            )
        }
    }
}