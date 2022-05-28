package xyz.cssxsh.mirai.admin

import com.cronutils.model.*
import com.cronutils.model.definition.*
import com.cronutils.parser.*
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*

internal object CronSerializer : KSerializer<Cron> {

    private val parser: CronParser by lazy {
        val type = System.getProperty("xyz.cssxsh.mirai.admin.cron", "QUARTZ")
        CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.valueOf(type)))
    }

    fun parse(expression: String): Cron {
        return parser.parse(expression.replace('_', ' '))
    }

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor(this::class.simpleName!!, PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Cron) = encoder.encodeString(value.asString())

    override fun deserialize(decoder: Decoder): Cron = parse(decoder.decodeString())
}