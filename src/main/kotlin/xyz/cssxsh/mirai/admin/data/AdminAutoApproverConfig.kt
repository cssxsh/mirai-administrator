package xyz.cssxsh.mirai.admin.data

import net.mamoe.mirai.console.data.*
import xyz.cssxsh.mirai.admin.*

@PublishedApi
internal object AdminAutoApproverConfig : ReadOnlyPluginConfig("AdminAutoApproverConfig"), MiraiAutoApproverConfig {

    @ValueName("auto_friend_accept")
    @ValueDescription("自动同意好友请求")
    override val autoFriendAccept: Boolean by value(false)

    @ValueName("auto_group_accept")
    @ValueDescription("自动同意加群请求")
    override val autoGroupAccept: Boolean by value(false)

    @ValueName("auto_member_accept")
    @ValueDescription("自动同意新成员请求")
    override val autoMemberAccept: Boolean by value(false)

    @ValueName("remind_friend_request")
    @ValueDescription("提醒好友请求")
    override val remindFriendRequest: Boolean by value(true)

    @ValueName("remind_group_request")
    @ValueDescription("提醒加群请求")
    override val remindGroupRequest: Boolean by value(true)

    @ValueName("remind_member_request")
    @ValueDescription("提醒新成员请求")
    override val remindMemberRequest: Boolean by value(false)

    @ValueName("reply_accept")
    @ValueDescription("回复触发同意请求")
    override val replyAccept: String by value("^(?:同意|OK|没问题)")

    @ValueName("reply_reject")
    @ValueDescription("回复触发拒绝请求")
    override val replyReject: String by value("^(?:拒绝|不同意)")

    @ValueName("reply_black")
    @ValueDescription("回复触发拉黑请求")
    override val replyBlack: String by value("^(?:拉黑|黑名单)")
}