package xyz.cssxsh.mirai.plugin.data

import net.mamoe.mirai.console.data.*
import xyz.cssxsh.mirai.plugin.*

public object AdminSetting : ReadOnlyPluginConfig("AdminSetting"),
    MiraiAutoApproverConfig, MiraiOnlineMessageConfig, MiraiStatusMessageConfig, MiraiContentCensorConfig {

    internal const val OWNER_DEFAULT = 12345L

    @ValueName("owner")
    @ValueDescription("机器人所有者")
    public val owner: Long by value(OWNER_DEFAULT)

    @ValueName("auto_friend_request")
    @ValueDescription("自动同意好友请求")
    public override val autoFriendAccept: Boolean by value(false)

    @ValueName("auto_group_request")
    @ValueDescription("自动同意加群请求")
    public override val autoGroupAccept: Boolean by value(false)

    @ValueName("auto_member_accept")
    @ValueDescription("自动同意新成员请求")
    public override val autoMemberAccept: Boolean by value(false)

    @ValueName("reply_accept")
    @ValueDescription("自动同意新成员请求")
    public override val replyAccept: String by value("同意|OK|没问题")

    @ValueName("reply_black")
    @ValueDescription("自动同意新成员请求")
    public override val replyBlack: String by value("拉黑|黑名单")

    @ValueName("send_status_interval")
    @ValueDescription("自动发送机器人状态到所有者的间隔，单位为分钟，为零时不开启此项功能")
    public override val sendStatusInterval: Long by value(60L)

    @ValueName("online_message_type")
    @ValueDescription("自动保存特殊消息内容，比如闪照")
    public override val onlineMessageType: MiraiOnlineMessageConfig.Type by value(MiraiOnlineMessageConfig.Type.XML)

    @ValueName("duration")
    @ValueDescription("逐个发送消息延时，单位秒")
    public override val duration: Long by value(10L)

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