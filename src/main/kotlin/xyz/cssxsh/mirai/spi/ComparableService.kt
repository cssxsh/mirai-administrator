package xyz.cssxsh.mirai.spi

import java.util.*
import kotlin.collections.*

public sealed interface ComparableService : Comparable<ComparableService> {

    /**
     * 优先等级, 越高越优先
     */
    public val level: Int get() = 0

    /**
     * 用来查找服务
     */
    public val id: String

    public override fun compareTo(other: ComparableService): Int = level.compareTo(other.level)

    public companion object Loader {

        public val loader: ServiceLoader<ComparableService> by lazy {
            ServiceLoader.load(ComparableService::class.java, ComparableService::class.java.classLoader)
        }

        public val instances: MutableSet<ComparableService> = HashSet()

        public inline operator fun <reified S> invoke(): List<S> = registered(S::class.java)

        public inline operator fun <reified S: ComparableService> get(id: String): S = get(S::class.java, id)

        public fun <S> registered(clazz: Class<S>): List<S> = (instances + loader).filterIsInstance(clazz)

        public fun <S : ComparableService> get(clazz: Class<S>, id: String): S {
            return registered(clazz).find { it.id == id }
                ?: throw NoSuchElementException("${clazz.simpleName} No Such $id")
        }
    }
}