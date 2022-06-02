package xyz.cssxsh.mirai.admin.cron

import com.cronutils.model.*
import com.cronutils.model.definition.*
import com.cronutils.parser.*

internal const val CRON_TYPE_KEY = "xyz.cssxsh.mirai.admin.cron"

public val DefaultCronParser: CronParser by lazy {
    val type = System.getProperty(CRON_TYPE_KEY, "QUARTZ")
    CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.valueOf(type)))
}

public fun Cron.asData(): DataCron = DataCron(delegate = this)