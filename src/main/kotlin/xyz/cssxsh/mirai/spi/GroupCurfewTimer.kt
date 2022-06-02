package xyz.cssxsh.mirai.spi

/**
 * 宵禁, [run] 返回指定群禁言时间（毫秒），null 不禁言
 */
public interface GroupCurfewTimer : GroupTimerService<Long?>