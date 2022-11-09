package xyz.cssxsh.mirai.admin.data

import net.mamoe.mirai.*
import net.mamoe.mirai.console.data.*
import net.mamoe.mirai.data.*
import net.mamoe.mirai.data.RequestEventData.Factory.toRequestEventData
import net.mamoe.mirai.event.events.*

@PublishedApi
internal object AdminRequestEventData : AutoSavePluginData("AdminRequestEventData"),
    Sequence<Map.Entry<Long, List<RequestEventData>>> {

    private val friends: MutableMap<Long, List<RequestEventData.NewFriendRequest>> by value()

    private val groups: MutableMap<Long, List<RequestEventData.BotInvitedJoinGroupRequest>> by value()

    private val members: MutableMap<Long, List<RequestEventData.MemberJoinRequest>> by value()

    override operator fun iterator(): Iterator<Map.Entry<Long, List<RequestEventData>>> = iterator {
        yieldAll(friends.entries)
        yieldAll(groups.entries)
        yieldAll(members.entries)
    }

    private operator fun List<RequestEventData>.get(id: Long): RequestEventData? {
        for (request in this) {
            if (request.eventId == id) return request
            when (request) {
                is RequestEventData.NewFriendRequest -> {
                    if (request.requester == id) return request
                }
                is RequestEventData.BotInvitedJoinGroupRequest -> {
                    if (request.groupId == id) return request
                    if (request.invitor == id) return request
                }
                is RequestEventData.MemberJoinRequest -> {
                    if (request.requester == id) return request
                    if (request.invitor == id) return request
                }
            }
        }
        return null
    }

    suspend fun handle(id: Long, accept: Boolean, black: Boolean, message: String): RequestEventData {
        for ((qq, list) in this) {
            val bot = Bot.getInstanceOrNull(qq) ?: continue
            val request = list[id] ?: continue
            if (accept) {
                request.accept(bot)
            } else {
                when (request) {
                    is RequestEventData.NewFriendRequest -> request.reject(bot, black)
                    is RequestEventData.BotInvitedJoinGroupRequest -> request.reject(bot)
                    is RequestEventData.MemberJoinRequest -> request.reject(bot, black, message)
                }
            }
            return request
        }
        throw NoSuchElementException("Not found event with $id")
    }

    operator fun plusAssign(event: NewFriendRequestEvent) {
        friends.compute(event.bot.id) { _, list ->
            list.orEmpty() + event.toRequestEventData()
        }
    }

    operator fun plusAssign(event: BotInvitedJoinGroupRequestEvent) {
        groups.compute(event.bot.id) { _, list ->
            list.orEmpty() + event.toRequestEventData()
        }
    }

    operator fun plusAssign(event: MemberJoinRequestEvent) {
        members.compute(event.bot.id) { _, list ->
            list.orEmpty() + event.toRequestEventData()
        }
    }

    operator fun minusAssign(event: FriendAddEvent) {
        friends.compute(event.bot.id) { _, list ->
            list.orEmpty().filterNot { it.requester == event.friend.id }
        }
    }

    operator fun minusAssign(event: BotJoinGroupEvent) {
        groups.compute(event.bot.id) { _, list ->
            list.orEmpty().filterNot { it.groupId == event.group.id }
        }
    }

    operator fun minusAssign(event: MemberJoinEvent) {
        members.compute(event.bot.id) { _, list ->
            list.orEmpty().filterNot { it.groupId == event.groupId && it.requester == event.member.id }
        }
    }
}