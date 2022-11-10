package xyz.cssxsh.mirai.admin

@PublishedApi
internal interface MiraiAutoApproverConfig {

    val autoFriendAccept: Boolean

    val autoGroupAccept: Boolean

    val autoMemberAccept: Boolean

    val replyAccept: String

    val replyBlack: String
}