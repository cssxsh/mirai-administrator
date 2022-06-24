package xyz.cssxsh.mirai.admin.data

import net.mamoe.mirai.console.data.*
import xyz.cssxsh.mirai.admin.*

public object AdminSetting : ReadOnlyPluginConfig("AdminSetting"), MiraiContentCensorConfig {

    internal const val OWNER_DEFAULT = 12345L

    @ValueName("owner")
    @ValueDescription("机器人所有者")
    public val owner: Long by value(OWNER_DEFAULT)

    @ValueName("censor_regex")
    @ValueDescription("消息审查，正则表达式")
    override val censorRegex: String by value("")

    @ValueName("censor_types")
    @ValueDescription("消息审查，类型, IMAGE, FLASH, SERVICE, APP, AUDIO, FORWARD, VIP, MARKET, MUSIC, POKE")
    override val censorTypes: Set<MiraiContentType> by value()

    @ValueName("censor_mute")
    @ValueDescription("消息审查，禁言时间 单位秒")
    override val censorMute: Int by value(0)

    @ValueName("record_limit")
    @ValueDescription("联系人（群/好友/...）聊天记录上限，（用于消息撤销等操作）")
    public val recordLimit: Int by value(100)
}