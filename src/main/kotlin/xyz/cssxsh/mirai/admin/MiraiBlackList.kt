package xyz.cssxsh.mirai.admin

import net.mamoe.mirai.console.permission.PermitteeId.Companion.hasChild
import net.mamoe.mirai.console.permission.PermitteeId.Companion.permitteeId
import net.mamoe.mirai.contact.*
import xyz.cssxsh.mirai.admin.data.*
import xyz.cssxsh.mirai.spi.*

@PublishedApi
internal object MiraiBlackList : BlackListService, MiraiBlackListConfig by AdminBlackListData {
    override val level: Int = 0
    override val id: String = "default-blacklist"

    override suspend fun check(user: User): Boolean = ids.any { it.hasChild(user.permitteeId) }
}