package xyz.cssxsh.mirai.spi

import net.mamoe.mirai.contact.*

/**
 * 黑名单服务, [check] 返回拉黑状态
 */
public interface BlackListService : ComparableService {

    /**
     * 审批用户是否被拉黑。
     * @return 返回 true 表示被拉黑
     */
    public suspend fun check(user: User): Boolean
}