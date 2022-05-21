package xyz.cssxsh.mirai.admin.data

import net.mamoe.mirai.console.data.*
import xyz.cssxsh.mirai.admin.*

public object AdminOnlineMessageConfig : ReadOnlyPluginConfig("AdminOnlineMessageConfig"), MiraiOnlineMessageConfig {

    @ValueName("type")
    @ValueDescription("发送上线消息的类型 XML, PLAIN, CUSTOM")
    public override val type: MiraiOnlineMessageConfig.Type by value(MiraiOnlineMessageConfig.Type.XML)

    @ValueName("custom")
    @ValueDescription("CUSTOM 上线消息的内容")
    public override val custom: String by value("哈哈哈，鸡汤来啦 [mirai:atall]")

    @ValueName("duration")
    @ValueDescription("逐个发送消息延时，单位秒")
    public override val duration: Long by value(10L)
}