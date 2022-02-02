package xyz.cssxsh.mirai.spi

import java.util.*
import kotlin.collections.*

public sealed interface ComparableService : Comparable<ComparableService> {

    /**
     * 优先等级, 越高越优先
     */
    public val level: Int get() = 0

    public override fun compareTo(other: ComparableService): Int = level.compareTo(other.level)

    public override fun toString(): String

    public companion object Loader {

        private val cache: MutableMap<Class<*>, ServiceLoader<*>> = WeakHashMap()

        public inline operator fun <reified S : ComparableService> invoke(): List<S> = registered(S::class.java)

        public fun <S : ComparableService> registered(clazz: Class<S>): List<S> {
            @Suppress("UNCHECKED_CAST")
            return (cache.getOrPut(clazz) { ServiceLoader.load(clazz, clazz.classLoader) } as ServiceLoader<S>)
                .sortedDescending()
        }
    }
}