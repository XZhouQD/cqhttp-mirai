package tech.mihoyo.mirai

import kotlinx.coroutines.*
import net.mamoe.mirai.Bot
import tech.mihoyo.mirai.util.logger
import tech.mihoyo.mirai.web.http.HttpApiServer
import tech.mihoyo.mirai.web.http.ReportService
import tech.mihoyo.mirai.web.websocket.WebSocketReverseClient
import tech.mihoyo.mirai.web.websocket.WebSocketServer
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

internal object SessionManager {

    val allSession: MutableMap<Long, Session> = mutableMapOf()

    operator fun get(botId: Long) = allSession[botId]

    fun closeSession(botId: Long) = allSession.remove(botId)?.also { it.close() }

    fun createBotSession(bot: Bot, config: Settings): BotSession =
        BotSession(bot, config, EmptyCoroutineContext).also { session -> allSession[bot.id] = session }
}

/**
 * @author NaturalHG
 * 这个用于管理不同Client与Mirai HTTP的会话
 *
 * [Session]均为内部操作用类
 * 需使用[SessionManager]
 */
abstract class Session internal constructor(
    coroutineContext: CoroutineContext,
    open val botId: Long
) : CoroutineScope {
    val supervisorJob = SupervisorJob(coroutineContext[Job])
    final override val coroutineContext: CoroutineContext = supervisorJob + coroutineContext

    internal open fun close() {
        supervisorJob.complete()
    }
}

class BotSession internal constructor(val bot: Bot, val config: Settings, coroutineContext: CoroutineContext) :
    Session(coroutineContext, bot.id) {
    private val heartbeatConfig = config.heartbeat
    val shouldCacheImage = config.cacheImage
    val shouldCacheRecord = config.cacheRecord
    val heartbeatEnabled =
        heartbeatConfig.enable
    val heartbeatInterval =
        heartbeatConfig.interval
    val cqApiImpl = MiraiApi(bot)
    private val httpApiServer = HttpApiServer(this)
    private val websocketClient = WebSocketReverseClient(this)
    private val websocketServer = WebSocketServer(this)
    private val httpReportService = ReportService(this)

    init {
        if (shouldCacheImage) logger.info("Bot: ${bot.id} 已开启接收图片缓存, 将会缓存收取到的所有图片")
        else logger.info("Bot: ${bot.id} 未开启接收图片缓存, 将不会缓存收取到的所有图片, 如需开启, 请在当前Bot配置中添加cacheImage=true")

        if(shouldCacheRecord) logger.info("Bot: ${bot.id} 已开启接收语音缓存, 将会缓存收取到的所有语音")
        else logger.info("Bot: ${bot.id} 未开启接收语音缓存, 将不会缓存收取到的所有语音, 如需开启, 请在当前Bot配置中添加cacheRecord=true")

        if (heartbeatEnabled) logger.info("Bot: ${bot.id} 已开启心跳机制, 设定的心跳发送频率为 $heartbeatInterval 毫秒")
    }

    override fun close() {
        runBlocking {
            websocketClient.close()
            websocketServer.close()
            httpApiServer.close()
            httpReportService.close()
        }
        super.close()
    }
}
