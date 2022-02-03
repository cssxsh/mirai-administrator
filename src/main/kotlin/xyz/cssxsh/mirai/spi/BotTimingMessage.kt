package xyz.cssxsh.mirai.spi

import kotlinx.coroutines.flow.*
import net.mamoe.mirai.*
import net.mamoe.mirai.message.*

/**
 * 定时消息的服务, [run] 返回消息
 */
public interface BotTimingMessage : TimerService<Bot, Flow<MessageReceipt<*>>>