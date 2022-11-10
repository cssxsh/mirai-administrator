package xyz.cssxsh.mirai.admin

import net.mamoe.mirai.console.permission.*

@PublishedApi
internal interface MiraiBlackListConfig {

    val ids: Iterable<PermitteeId>
}