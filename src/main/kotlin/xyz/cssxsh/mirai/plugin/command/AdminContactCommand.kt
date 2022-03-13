package xyz.cssxsh.mirai.plugin.command

import net.mamoe.mirai.console.command.*
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.utils.*
import xyz.cssxsh.mirai.plugin.*
import xyz.cssxsh.mirai.plugin.data.*

public object AdminContactCommand : CompositeCommand(
    owner = MiraiAdminPlugin,
    primaryName = "contact",
    description = "联系人处理相关操作"
) {
    @SubCommand
    @Description("删除联系人")
    public suspend fun CommandSender.delete(contact: Contact) {
        val message = try {
            when (contact) {
                is Friend -> contact.delete()
                is Group -> contact.quit()
                is NormalMember -> contact.kick(message = "")
                is Stranger -> contact.delete()
                else -> throw UnsupportedOperationException("delete $contact")
            }
            "删除成功"
        } catch (cause: Throwable) {
            logger.warning({ "删除错误" }, cause)
            "删除错误"
        }

        sendMessage(message)
    }

    @SubCommand
    @Description("处理请求")
    public suspend fun CommandSender.handle(
        id: Long,
        accept: Boolean = true,
        black: Boolean = false,
        message: String = ""
    ) {
        val result = try {
            val request = requireNotNull(AdminRequestEventData.handle(id, accept, black, message)) { "找不到事件 $id" }
            "请求已处理 $request"
        } catch (cause: Throwable) {
            logger.warning({ "出现错误" }, cause)
            "出现错误: ${cause.message}"
        }

        sendMessage(result)
    }

    @SubCommand
    @Description("申请列表")
    public suspend fun CommandSender.request() {
        val message = try {
            AdminRequestEventData.render()
        } catch (cause: Throwable) {
            logger.warning({ "出现错误" }, cause)
            "出现错误"
        }

        sendMessage(message)
    }
}