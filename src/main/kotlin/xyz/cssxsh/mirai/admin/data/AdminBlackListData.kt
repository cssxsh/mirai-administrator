package xyz.cssxsh.mirai.admin.data

import net.mamoe.mirai.console.data.*
import net.mamoe.mirai.console.permission.*
import xyz.cssxsh.mirai.admin.*

public object AdminBlackListData : AutoSavePluginData("AdminBlackListData"), MiraiBlackListConfig {

    @ValueName("ids")
    @ValueDescription("黑名单，由标识符组成")
    public override val ids: MutableSet<AbstractPermitteeId> by value()
}