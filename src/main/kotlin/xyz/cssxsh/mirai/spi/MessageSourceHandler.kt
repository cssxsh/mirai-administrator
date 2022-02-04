package xyz.cssxsh.mirai.spi

import net.mamoe.mirai.contact.*
import net.mamoe.mirai.event.events.*
import net.mamoe.mirai.message.data.*

public interface MessageSourceHandler : ComparableService {

    public fun find(contact: Contact?, event: MessageEvent?): MessageSource?
}