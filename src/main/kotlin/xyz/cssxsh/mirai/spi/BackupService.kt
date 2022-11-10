
package xyz.cssxsh.mirai.spi

import kotlin.jvm.*

/**
 * 备份服务, 当操作不支持时 throw [UnsupportedOperationException]
 */
public interface BackupService : ComparableService {

    /**
     * 备份群
     */
    @Throws(UnsupportedOperationException::class)
    public fun group()

    /**
     * 备份好友
     */
    @Throws(UnsupportedOperationException::class)
    public fun friend()

    /**
     * 备份机器人
     */
    @Throws(UnsupportedOperationException::class)
    public fun bot()
}