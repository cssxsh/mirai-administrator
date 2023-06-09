package xyz.cssxsh.mirai.admin

@PublishedApi
internal interface MiraiAutoApproverConfig {

    val autoFriendAccept: Boolean

    val autoGroupAccept: Boolean

    val autoMemberAccept: Boolean

    val remindFriendAccept: Boolean

    val remindGroupAccept: Boolean

    val remindMemberAccept: Boolean

    val replyAccept: String

    val replyReject: String

    val replyBlack: String
}