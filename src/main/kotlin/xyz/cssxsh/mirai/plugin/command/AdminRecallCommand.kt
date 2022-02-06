package xyz.cssxsh.mirai.plugin.command

import net.mamoe.mirai.console.command.*
import net.mamoe.mirai.console.util.ContactUtils.render
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.message.data.MessageSource.Key.recall
import net.mamoe.mirai.utils.*
import xyz.cssxsh.mirai.plugin.*
import xyz.cssxsh.mirai.spi.*

public object AdminRecallCommand : SimpleCommand(
    owner = MiraiAdminPlugin,
    primaryName = "recall",
    description = "撤回消息"
) {
    private val handler get() = ComparableService<MessageSourceHandler>().first()

    @Handler
    public suspend fun CommandSender.handle(contact: Contact? = null) {
        val message = try {
            val source = handler.find(contact = contact, event = (this as? CommandSenderOnMessage<*>)?.fromEvent)
            if (source != null) {
                source.recall()
                "${contact?.render() ?: source.fromId} 的消息撤回成功"
            } else {
                "${contact?.render()?.orEmpty()} 未找到消息"
            }
        } catch (cause: Throwable) {
            logger.warning({ "出现错误" }, cause)
            "出现错误"
        }

        sendMessage(message)
    }
}