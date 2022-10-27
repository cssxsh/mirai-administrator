
package xyz.cssxsh.mirai.spi

import kotlin.jvm.*

/**
 * 备份服务, 当操作不支持时 throw [UnsupportedOperationException]
 */
public interface BackupService : ComparableService {

    @Throws(UnsupportedOperationException::class)
    public fun group()

    @Throws(UnsupportedOperationException::class)
    public fun friend()

    @Throws(UnsupportedOperationException::class)
    public fun bot()
}