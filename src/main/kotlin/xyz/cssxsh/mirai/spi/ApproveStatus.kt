package xyz.cssxsh.mirai.spi

/**
 * 审核状态
 */
public sealed class ApproveStatus {
    /**
     * 接受
     */
    public object Accept : ApproveStatus()

    /**
     * 拒绝
     */
    public data class Reject
    @JvmOverloads constructor(val black: Boolean = false, val message: String) : ApproveStatus()

    /**
     * 忽略, 无法处理
     */
    public object Ignore : ApproveStatus()
}