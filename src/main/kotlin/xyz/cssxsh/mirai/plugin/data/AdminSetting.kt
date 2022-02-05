package xyz.cssxsh.mirai.plugin.data

import net.mamoe.mirai.console.data.*
import xyz.cssxsh.mirai.plugin.*

public object AdminSetting : ReadOnlyPluginConfig("AdminSetting"), MiraiStatusMessageConfig, MiraiContentCensorConfig {

    internal const val OWNER_DEFAULT = 12345L

    @ValueName("owner")
    @ValueDescription("机器人所有者")
    public val owner: Long by value(OWNER_DEFAULT)

    @ValueName("send_status_interval")
    @ValueDescription("自动发送机器人状态到所有者的间隔，单位为分钟，为零时不开启此项功能")
    public override val sendStatusInterval: Long by value(60L)

    @ValueName("censor_regex")
    @ValueDescription("消息审查，正则表达式")
    override val censorRegex: String by value("")

    @ValueName("censor_types")
    @ValueDescription("消息审查，类型")
    override val censorTypes: Set<MiraiContentType> by value()

    @ValueName("censor_mute")
    @ValueDescription("消息审查，禁言时间 单位秒")
    override val censorMute: Int by value(0)
}