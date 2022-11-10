package xyz.cssxsh.mirai.admin.command

import net.mamoe.mirai.console.command.*
import net.mamoe.mirai.console.util.ContactUtils.render
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.message.data.MessageSource.Key.recall
import net.mamoe.mirai.utils.*
import xyz.cssxsh.mirai.admin.*

/**
 * 撤回相关指令
 */
public object AdminRecallCommand : SimpleCommand(
    owner = MiraiAdminPlugin,
    primaryName = "recall",
    description = "撤回消息"
) {

    @Handler
    public suspend fun CommandSender.handle(contact: Contact? = null) {
        val message = try {
            val source = when {
                contact is Member -> from(member = contact)
                contact != null -> target(contact = contact)
                this is CommandSenderOnMessage<*> -> quote(event = fromEvent)
                else -> throw IllegalCommandArgumentException("参数不足以定位消息")
            }
            if (source != null) {
                source.recall()
                "${contact?.render() ?: source.fromId} 的消息撤回成功"
            } else {
                "${contact?.render().orEmpty()} 未找到消息"
            }
        } catch (cause: Exception) {
            logger.warning({ "出现错误" }, cause)
            "出现错误"
        }

        sendMessage(message)
    }
}