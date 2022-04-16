package xyz.cssxsh.mirai.spi


/**
 * 宵禁, [run] 返回指定群是否禁言
 */
public interface GroupCurfewTimer : GroupTimerService<Boolean?> {

    /**
     * 睡眠状态，为真时，定期器不工作
     */
    public var sleep: Boolean
}