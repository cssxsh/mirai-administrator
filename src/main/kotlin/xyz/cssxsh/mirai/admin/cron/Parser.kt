package xyz.cssxsh.mirai.admin.cron

import com.cronutils.descriptor.*
import com.cronutils.model.*
import com.cronutils.model.definition.*
import com.cronutils.model.time.*
import com.cronutils.parser.*
import net.mamoe.mirai.console.command.descriptor.*
import java.time.*
import java.time.format.*
import java.util.*

internal const val CRON_TYPE_KEY = "xyz.cssxsh.mirai.cron.type"

/**
 * 默认 [Cron] 解析器
 */
public val DefaultCronParser: CronParser by lazy {
    val type = CronType.valueOf(System.getProperty(CRON_TYPE_KEY, CronType.QUARTZ.name))
    CronParser(CronDefinitionBuilder.instanceDefinitionFor(type))
}

internal const val CRON_LOCALE_KEY = "xyz.cssxsh.mirai.cron.locale"

/**
 * 默认 [Cron] 解释器
 */
public val DefaultCronDescriptor: CronDescriptor by lazy {
    val locale = System.getProperty(CRON_LOCALE_KEY)?.let { Locale.forLanguageTag(it) } ?: Locale.getDefault()
    CronDescriptor.instance(locale)
}

/**
 * 包装成 [DataCron]
 */
public fun Cron.asData(): DataCron = this as? DataCron ?: DataCron(delegate = this)

/**
 * 获取 [ExecutionTime]
 */
public fun Cron.toExecutionTime(): ExecutionTime = ExecutionTime.forCron((this as? DataCron)?.delegate ?: this)

/**
 * 解释 [Cron]
 * @see DefaultCronDescriptor
 */
public fun Cron.description(): String = DefaultCronDescriptor.describe(this)

/**
 * [Cron], [Duration] 指令参数解析
 * @see DefaultCronDescriptor
 */
public val CronCommandArgumentContext: CommandArgumentContext = buildCommandArgumentContext {
    Cron::class with { text ->
        try {
            DefaultCronParser.parse(text)
        } catch (cause: IllegalArgumentException) {
            throw CommandArgumentParserException(
                message = "Cron 表达式读取错误，请检查是否添加了双引号",
                cause = cause
            )
        }
    }
    Duration::class with { text ->
        try {
            Duration.parse(text)
        } catch (cause: DateTimeParseException) {
            throw CommandArgumentParserException(
                message = "Duration 表达式格式为 PnDTnHnMn.nS",
                cause = cause
            )
        }
    }
}