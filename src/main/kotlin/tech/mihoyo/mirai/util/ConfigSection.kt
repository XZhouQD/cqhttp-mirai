/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("MemberVisibilityCanBePrivate")

package tech.mihoyo.mirai.util

import kotlinx.serialization.Serializable
import net.mamoe.mirai.utils.MiraiInternalAPI
import org.yaml.snakeyaml.Yaml
import java.io.File
import java.io.InputStream
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.NoSuchElementException
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.full.isSubclassOf


/**
 * 标注一个即将在将来版本删除的 API
 *
 * 目前 [Config] 的读写设计语义不明, list 和 map 读取效率低, 属性委托写入语义不明
 * 将在未来重写并变为 `ReadOnlyConfig` 和 `ReadWriteConfig`.
 * 并计划添加图形端配置绑定, 和带注释的序列化, 自动保存的配置文件, 与指令绑定的属性.
 */
@RequiresOptIn("将在未来进行不兼容的修改", RequiresOptIn.Level.WARNING)
annotation class ToBeRemoved

/**
 * 可读可写配置文件.
 *
 * @suppress 注意: 配置文件正在进行不兼容的修改
 */
interface Config {
    @ToBeRemoved
    fun getConfigSection(key: String): ConfigSection

    fun getString(key: String): String
    fun getInt(key: String): Int
    fun getFloat(key: String): Float
    fun getDouble(key: String): Double
    fun getLong(key: String): Long
    fun getBoolean(key: String): Boolean

    @ToBeRemoved
    fun getList(key: String): List<*>


    @ToBeRemoved
    fun getStringList(key: String): List<String>

    @ToBeRemoved
    fun getIntList(key: String): List<Int>

    @ToBeRemoved
    fun getFloatList(key: String): List<Float>

    @ToBeRemoved
    fun getDoubleList(key: String): List<Double>

    @ToBeRemoved
    fun getLongList(key: String): List<Long>

    @ToBeRemoved
    fun getConfigSectionList(key: String): List<ConfigSection>

    operator fun set(key: String, value: Any)
    operator fun get(key: String): Any?
    operator fun contains(key: String): Boolean

    @ToBeRemoved
    fun exist(key: String): Boolean

    /**
     * 设置 key = value (如果value不存在则valueInitializer会被调用)
     * 之后返回当前key对应的值
     * */
    @ToBeRemoved
    fun <T : Any> setIfAbsent(key: String, value: T)

    @ToBeRemoved
    fun <T : Any> setIfAbsent(key: String, valueInitializer: Config.() -> T)

    @ToBeRemoved
    fun asMap(): Map<String, Any>

    @ToBeRemoved
    fun save()

    companion object {
        @ToBeRemoved
        fun load(fileName: String): Config {
            return load(
                File(
                    fileName.replace(
                        "//",
                        "/"
                    )
                )
            )
        }

        /**
         * create a read-write config
         * */
        @ToBeRemoved
        fun load(file: File): Config {
            if (!file.exists()) {
                file.createNewFile()
            }
            return when (file.extension.toLowerCase()) {
                "yml" -> YamlConfig(file)
                "yaml" -> YamlConfig(file)
                "mirai" -> YamlConfig(file)
                else -> error("Unsupported file config type ${file.extension.toLowerCase()}")
            }
        }

        /**
         * create a read-only config
         */
        @ToBeRemoved
        fun load(content: String, type: String): Config {
            return when (type.toLowerCase()) {
                "yml" -> YamlConfig(content)
                "yaml" -> YamlConfig(content)
                "mirai" -> YamlConfig(content)
                else -> error("Unsupported file config type $content")
            }
        }

        /**
         * create a read-only config
         */
        @ToBeRemoved
        fun load(inputStream: InputStream, type: String): Config {
            return load(inputStream.readBytes().toString(), type)
        }

    }
}


@ToBeRemoved
fun File.loadAsConfig(): Config {
    return Config.load(this)
}

/* 最简单的代理 */
@ToBeRemoved
inline operator fun <reified T : Any> Config.getValue(thisRef: Any?, property: KProperty<*>): T {
    return smartCast(property)
}

@ToBeRemoved
inline operator fun <reified T : Any> Config.setValue(thisRef: Any?, property: KProperty<*>, value: T) {
    this[property.name] = value
}

/* 带有默认值的代理 */
@Suppress("unused")
@ToBeRemoved
inline fun <reified T : Any> Config.withDefault(
    crossinline defaultValue: () -> T
): ReadWriteProperty<Any, T> {
    return object : ReadWriteProperty<Any, T> {
        override fun getValue(thisRef: Any, property: KProperty<*>): T {
            if (this@withDefault.exist(property.name)) {//unsafe
                return this@withDefault.smartCast(property)
            }
            return defaultValue()
        }

        override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
            this@withDefault[property.name] = value
        }
    }
}

/* 带有默认值且如果为空会写入的代理 */
@Suppress("unused")
@ToBeRemoved
inline fun <reified T : Any> Config.withDefaultWrite(
    noinline defaultValue: () -> T
): WithDefaultWriteLoader<T> {
    return WithDefaultWriteLoader(
        T::class,
        this,
        defaultValue,
        false
    )
}

/* 带有默认值且如果为空会写入保存的代理 */
@ToBeRemoved
inline fun <reified T : Any> Config.withDefaultWriteSave(
    noinline defaultValue: () -> T
): WithDefaultWriteLoader<T> {
    return WithDefaultWriteLoader(T::class, this, defaultValue, true)
}

@ToBeRemoved
class WithDefaultWriteLoader<T : Any>(
    private val _class: KClass<T>,
    private val config: Config,
    private val defaultValue: () -> T,
    private val save: Boolean
) {
    operator fun provideDelegate(
        thisRef: Any,
        prop: KProperty<*>
    ): ReadWriteProperty<Any, T> {
        val defaultValue by lazy { defaultValue.invoke() }
        if (!config.contains(prop.name)) {
            config[prop.name] = defaultValue
            if (save) {
                config.save()
            }
        }
        return object : ReadWriteProperty<Any, T> {
            override fun getValue(thisRef: Any, property: KProperty<*>): T {
                if (config.exist(property.name)) {//unsafe
                    return config.smartCastInternal(property.name, _class)
                }
                return defaultValue
            }

            override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
                config[property.name] = value
            }
        }
    }
}

@PublishedApi
internal inline fun <reified T : Any> Config.smartCast(property: KProperty<*>): T {
    return smartCastInternal(property.name, T::class)
}

@OptIn(ToBeRemoved::class)
@PublishedApi
@Suppress("IMPLICIT_CAST_TO_ANY", "UNCHECKED_CAST")
internal fun <T : Any> Config.smartCastInternal(propertyName: String, _class: KClass<T>): T {
    return when (_class) {
        String::class -> this.getString(propertyName)
        Int::class -> this.getInt(propertyName)
        Float::class -> this.getFloat(propertyName)
        Double::class -> this.getDouble(propertyName)
        Long::class -> this.getLong(propertyName)
        Boolean::class -> this.getBoolean(propertyName)
        else -> when {
            _class.isSubclassOf(ConfigSection::class) -> this.getConfigSection(propertyName)
            _class == List::class || _class == MutableList::class -> {
                val list = this.getList(propertyName)
                return if (list.isEmpty()) {
                    list
                } else {
                    when (list[0]!!::class) {
                        String::class -> getStringList(propertyName)
                        Int::class -> getIntList(propertyName)
                        Float::class -> getFloatList(propertyName)
                        Double::class -> getDoubleList(propertyName)
                        Long::class -> getLongList(propertyName)
                        //不去支持getConfigSectionList(propertyName)
                        // LinkedHashMap::class -> getConfigSectionList(propertyName)//faster approach
                        else -> {
                            //if(list[0]!! is ConfigSection || list[0]!! is Map<*,*>){
                            // getConfigSectionList(propertyName)
                            //}else {
                            error("unsupported type" + list[0]!!::class)
                            //}
                        }
                    }
                } as T
            }
            else -> {
                error("unsupported type")
            }
        }
    } as T
}


@ToBeRemoved
interface ConfigSection : Config, MutableMap<String, Any> {
    companion object {
        fun create(): ConfigSection {
            return ConfigSectionImpl()
        }

        fun new(): ConfigSection {
            return this.create()
        }
    }

    override fun getConfigSection(key: String): ConfigSection {
        val content = get(key) ?: throw NoSuchElementException(key)
        if (content is ConfigSection) {
            return content
        }
        @Suppress("UNCHECKED_CAST")
        return ConfigSectionDelegation(
            Collections.synchronizedMap(
                (get(key) ?: throw NoSuchElementException(key)) as LinkedHashMap<String, Any>
            )
        )
    }

    override fun getString(key: String): String {
        return (get(key) ?: throw NoSuchElementException(key)).toString()
    }

    override fun getInt(key: String): Int {
        return (get(key) ?: throw NoSuchElementException(key)).toString().toInt()
    }

    override fun getFloat(key: String): Float {
        return (get(key) ?: throw NoSuchElementException(key)).toString().toFloat()
    }

    override fun getBoolean(key: String): Boolean {
        return (get(key) ?: throw NoSuchElementException(key)).toString().toBoolean()
    }

    override fun getDouble(key: String): Double {
        return (get(key) ?: throw NoSuchElementException(key)).toString().toDouble()
    }

    override fun getLong(key: String): Long {
        return (get(key) ?: throw NoSuchElementException(key)).toString().toLong()
    }

    override fun getList(key: String): List<*> {
        return ((get(key) ?: throw NoSuchElementException(key)) as List<*>)
    }

    override fun getStringList(key: String): List<String> {
        return ((get(key) ?: throw NoSuchElementException(key)) as List<*>).filterNotNull().map { it.toString() }
    }

    override fun getIntList(key: String): List<Int> {
        return ((get(key) ?: throw NoSuchElementException(key)) as List<*>).map { it.toString().toInt() }
    }

    override fun getFloatList(key: String): List<Float> {
        return ((get(key) ?: throw NoSuchElementException(key)) as List<*>).map { it.toString().toFloat() }
    }

    override fun getDoubleList(key: String): List<Double> {
        return ((get(key) ?: throw NoSuchElementException(key)) as List<*>).map { it.toString().toDouble() }
    }

    override fun getLongList(key: String): List<Long> {
        return ((get(key) ?: throw NoSuchElementException(key)) as List<*>).map { it.toString().toLong() }
    }

    override fun getConfigSectionList(key: String): List<ConfigSection> {
        return ((get(key) ?: throw NoSuchElementException(key)) as List<*>).map {
            if (it is ConfigSection) {
                it
            } else {
                @Suppress("UNCHECKED_CAST")
                ConfigSectionDelegation(
                    Collections.synchronizedMap(
                        it as MutableMap<String, Any>
                    )
                )
            }
        }
    }

    override fun exist(key: String): Boolean {
        return get(key) != null
    }

    override fun <T : Any> setIfAbsent(key: String, value: T) {
        putIfAbsent(key, value)
    }

    override fun <T : Any> setIfAbsent(key: String, valueInitializer: Config.() -> T) {
        if (this.exist(key)) {
            put(key, valueInitializer.invoke(this))
        }
    }
}

@OptIn(ToBeRemoved::class)
internal inline fun <reified T : Any> ConfigSection.smartGet(key: String): T {
    return this.smartCastInternal(key, T::class)
}

@Serializable
@ToBeRemoved
open class ConfigSectionImpl : ConcurrentHashMap<String, Any>(),

    ConfigSection {
    override fun set(key: String, value: Any) {
        super.put(key, value)
    }

    override operator fun get(key: String): Any? {
        return super.get(key)
    }

    @Suppress("RedundantOverride")
    override fun contains(key: String): Boolean {
        return super.contains(key)
    }

    override fun exist(key: String): Boolean {
        return containsKey(key)
    }

    override fun asMap(): Map<String, Any> {
        return this
    }

    override fun save() {

    }
}

@ToBeRemoved
open class ConfigSectionDelegation(
    private val delegate: MutableMap<String, Any>
) : ConfigSection, MutableMap<String, Any> by delegate {
    override fun set(key: String, value: Any) {
        delegate[key] = value
    }

    override fun contains(key: String): Boolean {
        return delegate.containsKey(key)
    }

    override fun asMap(): Map<String, Any> {
        return delegate
    }

    override fun save() {

    }
}


@ToBeRemoved
interface FileConfig : Config {
    fun deserialize(content: String): ConfigSection

    fun serialize(config: ConfigSection): String
}


@MiraiInternalAPI
@ToBeRemoved
abstract class FileConfigImpl internal constructor(
    private val rawContent: String
) : FileConfig,
    ConfigSection {

    internal var file: File? = null


    @Suppress("unused")
    constructor(file: File) : this(file.readText()) {
        this.file = file
    }


    private val content by lazy {
        deserialize(rawContent)
    }


    override val size: Int get() = content.size
    override val entries: MutableSet<MutableMap.MutableEntry<String, Any>> get() = content.entries
    override val keys: MutableSet<String> get() = content.keys
    override val values: MutableCollection<Any> get() = content.values
    override fun containsKey(key: String): Boolean = content.containsKey(key)
    override fun containsValue(value: Any): Boolean = content.containsValue(value)
    override fun put(key: String, value: Any): Any? = content.put(key, value)
    override fun isEmpty(): Boolean = content.isEmpty()
    override fun putAll(from: Map<out String, Any>) = content.putAll(from)
    override fun clear() = content.clear()
    override fun remove(key: String): Any? = content.remove(key)

    override fun save() {
        if (isReadOnly) {
            error("Config is readonly")
        }
        if (!((file?.exists())!!)) {
            file?.createNewFile()
        }
        file?.writeText(serialize(content))
    }

    val isReadOnly: Boolean get() = file == null

    override fun contains(key: String): Boolean {
        return content.contains(key)
    }

    override fun get(key: String): Any? {
        return content[key]
    }

    override fun set(key: String, value: Any) {
        content[key] = value
    }

    override fun asMap(): Map<String, Any> {
        return content.asMap()
    }

}

@ToBeRemoved
@OptIn(MiraiInternalAPI::class)
class YamlConfig internal constructor(content: String) : FileConfigImpl(content) {
    constructor(file: File) : this(file.readText()) {
        this.file = file
    }

    override fun deserialize(content: String): ConfigSection {
        if (content.isEmpty() || content.isBlank()) {
            return ConfigSectionImpl()
        }
        return ConfigSectionDelegation(
            Collections.synchronizedMap(
                Yaml().load(content) as LinkedHashMap<String, Any>
            )
        )
    }

    override fun serialize(config: ConfigSection): String {
        return Yaml().dumpAsMap(config)
    }

}
