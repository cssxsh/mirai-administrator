package xyz.cssxsh.mirai.admin.data

import net.mamoe.mirai.console.command.*
import net.mamoe.mirai.console.data.*
import net.mamoe.mirai.console.permission.*
import net.mamoe.mirai.console.util.*
import xyz.cssxsh.mirai.admin.*

@PublishedApi
internal object AdminCommentConfig : ReadOnlyPluginConfig("AdminCommentConfig") {

    public var permission: Permission = Permission.getRootPermission()
        private set

    @OptIn(ConsoleExperimentalApi::class)
    override fun onInit(owner: PluginDataHolder, storage: PluginDataStorage) {
        if (owner is CommandOwner) {
            permission = owner.registerPermission(name = "comment.include", description = "拥有此权限可以给机器人留言")
        }
    }

    @ValueName("reply")
    @ValueDescription("回复评论者消息")
    val reply: String by value("成功留言")

    @ValueName("interval")
    @ValueDescription("两次评论之间的间隔，毫秒")
    val interval: Long by value(600_000L)

    @ValueName("user")
    @ValueDescription("私聊是否能够留言")
    val user: Boolean by value(true)

    @ValueName("at")
    @ValueDescription("群聊 at 触发留言")
    val at: Boolean by value(true)

    @ValueName("quote")
    @ValueDescription("群聊 quote 触发留言")
    val quote: Boolean by value(true)
}