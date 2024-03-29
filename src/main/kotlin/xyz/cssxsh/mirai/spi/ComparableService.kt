package xyz.cssxsh.mirai.spi

import java.util.*
import kotlin.collections.*

/**
 * Admin 插件对接服务接口
 */
public sealed interface ComparableService : Comparable<ComparableService> {

    /**
     * 优先等级, 越高越优先
     */
    public val level: Int

    /**
     * 用来查找服务
     */
    public val id: String

    /**
     * 简介
     */
    public val description: String get() = ""

    public override fun compareTo(other: ComparableService): Int {
        return other.level.compareTo(level).takeUnless { it == 0 } ?: id.compareTo(other.id)
    }

    /**
     * 加载器
     */
    public companion object Loader {

        internal val instances: MutableSet<ComparableService> = TreeSet()

        /**
         * 获取所有已注册实例
         */
        public inline operator fun <reified S> invoke(): List<S> = registered(S::class.java)

        /**
         * 获取指定ID实例
         */
        public inline operator fun <reified S : ComparableService> get(id: String): S = get(S::class.java, id)

        /**
         * 获取指定Class实例
         */
        public fun <S> registered(clazz: Class<S>): List<S> {
            return instances.filterIsInstance(clazz)
        }

        /**
         * 获取指定Class和ID实例
         */
        public fun <S : ComparableService> get(clazz: Class<S>, id: String): S {
            return registered(clazz).find { it.id == id }
                ?: throw NoSuchElementException("${clazz.simpleName} No Such $id")
        }
    }
}