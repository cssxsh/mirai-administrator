package xyz.cssxsh.mirai.plugin

import kotlinx.serialization.*
import net.mamoe.mirai.console.permission.PermissionId
import java.time.*

@Serializable
public data class LocalTimeAllow(
    @SerialName("start")
    @Serializable(with = LocalTimeSerializer::class)
    override val start: LocalTime,
    @SerialName("end")
    @Serializable(with = LocalTimeSerializer::class)
    override val endInclusive: LocalTime,
    @SerialName("id")
    val permissionId: PermissionId
): ClosedRange<LocalTime> {
    override fun contains(value: LocalTime): Boolean {
        return when {
            start < endInclusive -> value >= start && value <= endInclusive
            start > endInclusive -> value >= start || value <= endInclusive
            else -> false
        }
    }

    override fun isEmpty(): Boolean = (start == endInclusive)

    override fun toString(): String = "$start~$endInclusive"
}
