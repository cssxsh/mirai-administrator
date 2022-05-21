package xyz.cssxsh.mirai.admin.data

import net.mamoe.mirai.console.data.*
import xyz.cssxsh.mirai.admin.*

public object AdminAutoApproverConfig : ReadOnlyPluginConfig("AdminAutoApproverConfig"), MiraiAutoApproverConfig {

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
    @ValueDescription("回复触发同意请求")
    public override val replyAccept: String by value("同意|OK|没问题")

    @ValueName("reply_black")
    @ValueDescription("回复触发拉黑请求")
    public override val replyBlack: String by value("拉黑|黑名单")
}