package xyz.cssxsh.mirai.plugin

import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.*

@OptIn(MiraiExperimentalApi::class)
public enum class MiraiContentType(public val key: MessageKey<*>) {
    IMAGE(key = Image),
    FLASH(key = FlashImage),
    RICH(key = RichMessage),
    AUDIO(key = Audio),
    FORWARD(key = ForwardMessage),
    VIP(key = VipFace),
    Market(key = MarketFace),
    MUSIC(key = MusicShare),
    Poke(key = PokeMessage)
}