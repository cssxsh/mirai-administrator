package xyz.cssxsh.mirai.plugin

import net.mamoe.mirai.console.permission.*

public interface MiraiBlackListConfig {

    public val ids: Iterable<PermitteeId>
}