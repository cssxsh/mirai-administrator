package xyz.cssxsh.mirai.admin

import net.mamoe.mirai.console.permission.*

public interface MiraiBlackListConfig {

    public val ids: Iterable<PermitteeId>
}