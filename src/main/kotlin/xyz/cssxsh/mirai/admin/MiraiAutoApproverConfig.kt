package xyz.cssxsh.mirai.admin

public interface MiraiAutoApproverConfig {

    public val autoFriendAccept: Boolean

    public val autoGroupAccept: Boolean

    public val autoMemberAccept: Boolean

    public val replyAccept: String

    public val replyBlack: String
}