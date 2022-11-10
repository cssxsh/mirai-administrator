package xyz.cssxsh.mirai.admin.cron

import com.cronutils.model.*
import kotlinx.serialization.*
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.*

/**
 * Cron 的封装，用于序列化
 */
@Serializable(with = DataCron.Serializer::class)
public data class DataCron(public val delegate: Cron) : Cron by delegate {

    override fun toString(): String = asString()

    /**
     * [Cron] 序列化为字符串
     */
    public companion object Serializer : KSerializer<DataCron> {

        override val descriptor: SerialDescriptor =
            PrimitiveSerialDescriptor(this::class.qualifiedName!!, PrimitiveKind.STRING)

        override fun serialize(encoder: Encoder, value: DataCron) {
            encoder.encodeString(value.asString())
        }

        override fun deserialize(decoder: Decoder): DataCron {
            return DataCron(delegate = DefaultCronParser.parse(decoder.decodeString()))
        }
    }
}