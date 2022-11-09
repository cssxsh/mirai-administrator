package xyz.cssxsh.mirai.admin.data

import net.mamoe.mirai.console.data.*

@PublishedApi
internal object AdminAutoQuitConfig : ReadOnlyPluginConfig("AdminAutoQuitConfig") {

    @ValueName("include_admin")
    @ValueDescription("身为管理员也会退群")
    val admin: Boolean by value(false)

    @ValueName("include_owner")
    @ValueDescription("操作人是机器人所有者也退群")
    val owner: Boolean by value(false)

    @ValueName("wait_duration")
    @ValueDescription("防止误操作的延时(单位：毫秒)")
    val duration: Long by value(60_000L)

    @ValueName("mute_limit")
    @ValueDescription("超过这个时间(单位：秒)的禁言会触发自动退群")
    val limit: Long by value(30 * 24 * 3600L)
}