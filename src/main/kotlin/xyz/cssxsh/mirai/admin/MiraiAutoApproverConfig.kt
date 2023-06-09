package xyz.cssxsh.mirai.admin

@PublishedApi
internal interface MiraiAutoApproverConfig {

    val autoFriendAccept: Boolean

    val autoGroupAccept: Boolean

    val autoMemberAccept: Boolean

    val remindFriendRequest: Boolean

    val remindGroupRequest: Boolean

    val remindMemberRequest: Boolean

    val replyAccept: String

    val replyReject: String

    val replyBlack: String
}