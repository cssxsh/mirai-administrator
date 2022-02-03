package xyz.cssxsh.mirai.spi

/**
 * 审核状态
 */
public sealed class ApproveResult {
    /**
     * 接受
     */
    public object Accept : ApproveResult()

    /**
     * 拒绝
     */
    public data class Reject
    @JvmOverloads constructor(val black: Boolean = false, val message: String) : ApproveResult()

    /**
     * 忽略, 无法处理
     */
    public object Ignore : ApproveResult()
}