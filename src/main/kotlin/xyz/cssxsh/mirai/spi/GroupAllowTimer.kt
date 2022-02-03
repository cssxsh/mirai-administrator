package xyz.cssxsh.mirai.spi

import net.mamoe.mirai.console.permission.*

/**
 * 放风, [run] 返回指定群员有那些权限
 */
public interface GroupAllowTimer : GroupTimerService<List<Pair<PermissionId, Boolean>>>