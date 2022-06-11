package xyz.cssxsh.mirai.admin.data

import net.mamoe.mirai.console.command.*
import net.mamoe.mirai.console.data.*
import net.mamoe.mirai.console.permission.*
import net.mamoe.mirai.console.util.*
import xyz.cssxsh.mirai.admin.*

public object AdminCommentConfig : ReadOnlyPluginConfig("AdminCommentConfig") {

    public var permission: Permission = Permission.getRootPermission()
        private set

    @OptIn(ConsoleExperimentalApi::class)
    override fun onInit(owner: PluginDataHolder, storage: PluginDataStorage) {
        if (owner is CommandOwner) {
            permission = owner.registerPermission(name = "comment.include", description = "拥有此权限的发送者可以给机器人留言")
        }
    }

    @ValueName("reply")
    @ValueDescription("回复评论者消息")
    public val reply: String by value("成功留言")

    @ValueName("interval")
    @ValueDescription("两次评论之间的间隔，毫秒")
    public val interval: Long by value(600_000L)

    @ValueName("user")
    @ValueDescription("两次评论之间的间隔，毫秒")
    public val user: Boolean by value(true)

    @ValueName("at")
    @ValueDescription("at 触发评论")
    public val at: Boolean by value(true)
}