package xyz.cssxsh.mirai.admin.command

import net.mamoe.mirai.console.command.*
import net.mamoe.mirai.console.permission.*
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.utils.*
import xyz.cssxsh.mirai.admin.*
import xyz.cssxsh.mirai.admin.data.*

/**
 * 联系人相关指令
 */
public object AdminContactCommand : CompositeCommand(
    owner = MiraiAdminPlugin,
    primaryName = "contact",
    description = "联系人处理相关操作"
) {
    /**
     * 删除联系人
     * @param contact 联系人，好友/群/群员/陌生人
     */
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
        } catch (cause: Exception) {
            logger.warning({ "删除错误" }, cause)
            "删除错误"
        }

        sendMessage(message)
    }

    /**
     * 处理 加群/好友 请求
     * @param id 事件ID 或者 邀请人 ID
     * @param accept 接受
     * @param black 拉黑
     * @param message 回复消息
     */
    @SubCommand
    @Description("处理请求")
    public suspend fun CommandSender.handle(
        id: Long,
        accept: Boolean = true,
        black: Boolean = false,
        message: String = ""
    ) {
        val result = try {
            val request = AdminRequestEventData.handle(id, accept, black, message)
            "请求已处理 $request"
        } catch (cause: Exception) {
            logger.warning({ "出现错误" }, cause)
            "出现错误: ${cause.message}"
        }

        sendMessage(result)
    }

    /**
     * 打印申请列表
     */
    @SubCommand
    @Description("申请列表")
    public suspend fun CommandSender.request() {
        val message = try {
            AdminRequestEventData.render()
        } catch (cause: Exception) {
            logger.warning({ "出现错误" }, cause)
            "出现错误"
        }

        sendMessage(message)
    }

    /**
     * 设置黑名单
     * @param permitteeIds 被拉黑的许可人标识符
     */
    @SubCommand
    @Description("拉黑")
    public suspend fun CommandSender.black(vararg permitteeIds: String) {
        val message = try {
            val args = permitteeIds.mapTo(HashSet(), AbstractPermitteeId::parseFromString)
            AdminBlackListData.ids.addAll(args)
            AdminBlackListData.ids.joinToString("\n") { it.asString() }
        } catch (cause: Exception) {
            logger.warning({ "出现错误" }, cause)
            "出现错误"
        }

        sendMessage(message)
    }

    /**
     * 取消黑名单
     * @param permitteeIds 被拉黑的许可人标识符
     */
    @SubCommand
    @Description("取消拉黑")
    public suspend fun CommandSender.white(vararg permitteeIds: String) {
        val message = try {
            val args = permitteeIds.mapTo(HashSet(), AbstractPermitteeId::parseFromString)
            AdminBlackListData.ids.removeAll(args)
            AdminBlackListData.ids.joinToString("\n") { it.asString() }
        } catch (cause: Exception) {
            logger.warning({ "出现错误" }, cause)
            "出现错误"
        }

        sendMessage(message)
    }

    /**
     * 备份联系人数据
     */
    @SubCommand
    @Description("备份联系人数据")
    public suspend fun CommandSender.backup() {
        val message = try {
            xyz.cssxsh.mirai.admin.backup()
            "备份已开始"
        } catch (cause: Exception) {
            logger.warning({ "出现错误" }, cause)
            "出现错误"
        }

        sendMessage(message)
    }
}