package xyz.cssxsh.mirai.admin

import net.mamoe.mirai.message.data.*

public enum class MiraiContentType(public val key: MessageKey<*>) {
    IMAGE(key = Image),
    FLASH(key = FlashImage),
    SERVICE(key = ServiceMessage),
    APP(key = LightApp),
    AUDIO(key = Audio),
    FORWARD(key = ForwardMessage),
    VIP(key = VipFace),
    MARKET(key = MarketFace),
    MUSIC(key = MusicShare),
    POKE(key = PokeMessage)
}