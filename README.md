# CQHTTP Mirai

![Gradle CI](https://github.com/yyuueexxiinngg/cqhttp-mirai/workflows/Gradle%20CI/badge.svg)

__CQHTTP runs on Mirai__

### 自0.2.0起添加临时[Embedded分支版本](https://github.com/yyuueexxiinngg/cqhttp-mirai/tree/embedded), 与主分支单插件版并行, 支持发送语音, 请看[此说明中第一部分](https://github.com/yyuueexxiinngg/cqhttp-mirai/tree/embedded)进行配置

## 开始使用
0. 请首先运行[Mirai-console](https://github.com/mamoe/mirai-console)相关客户端生成plugins文件夹
1. 将`cqhttp-mirai`生成的`jar包文件`放入`plugins`文件夹中
2. 再次启动[Mirai-console](https://github.com/mamoe/mirai-console)相关客户端，配置文件会自动生成
3. 编辑`config/CQHTTP-Mirai/tech.mihoyo.mirai.Settings.yml`配置文件, 按下放给出配置的注释参考修改
4. 再次启动[Mirai-console](https://github.com/mamoe/mirai-console)相关客户端

## 为什么会有这个Repo
0. 自Mirai-Console更新到1.0-M4版本后，由于启动机制的变更，cqhttp-mirai无法在1.0-M4启动了
1. 截止本日（2020.09.17），cqhttp-mirai依赖的console仍为0.5.2版本
2. 这个repo对cqhttp-mirai进行了调整，让cqhttp-mirai能在1.0-M4启动使用了
3. Embedded版本这里没有发布
4. 此Repo的版本只视为SNAPSHOT，不提供任何保证
5. 我只测试了ws-reverse连接和基础的对话，是否有API失效我也不知道
6. 测试使用环境：MiraiOK, Mirai-Core-QQAndroid 1.2.3, Mirai-Console 1.0-M4, Nonebot 1.7.0

——XZhouQD

## 改动似乎不只是上面那些？
0. 是的，最初尝试setup就遇到了很多麻烦，因此移除了一个总是导致我编译失败的gradle task
1. 原repo没有带着gradle/wrapper，导致很可能需要自己拿一份gradle 6.5的gradle-wrapper.jar，这里直接加上
2. 由于我用idea而且还用的不熟练，因此多了全局的.gitignore
3. 由于我本人几乎不懂kotlin，代码全靠脑补，因此配置文件位置变化，也互不兼容了，支持的功能也变少了

——XZhouQD

## 配置相关
重点：本Repo发布的0.2.4-SNAPSHOT-dev版配置文件(纯Mirai-Console 1.0-M4版本)不与之前版本(0.2.4-SNAPSHOT, 0.2.3及以下)兼容，配置文件位置也不同，敬请留意！

```yaml
# Debug日志输出选项
debug: false
# 下载图片/语音时使用的Proxy, 配置后, 发送图片/语音时指定`proxy=1`以通过Proxy下载, 如[CQ:image,proxy=1,url=http://***]
# 支持HTTP及Sock两种Proxy, 设置举例 proxy: "http=http://127.0.0.1:8888", proxy : "sock=127.0.0.1:1088"
proxy: ''
# 要进行配置的QQ号 (此版本由于个人修改水平问题不支持多账号)
user: 111111111
# 是否缓存所有收到的图片, 默认为否 (仅包含图片信息, 不包含图片本身,  < 0.5KB)
cacheImage: false
# 是否缓存所有收到的语音, 默认为否 (将下载完整语音进行保存)
cacheRecord: false
# 心跳包相关配置
heartbeat: 
  # 是否发送心跳包, 默认为否
  enable: true
  # 心跳包发送间隔, 默认为 15000毫秒
  interval: 15000
# HTTP 相关配置
http: 
  # 可选，是否启用HTTP API服务器, 默认为不启用, 此项开始与否跟postUrl无关
  enable: false
  # 可选，HTTP API服务器监听地址, 默认为0.0.0.0
  host: 0.0.0.0
  # 可选，HTTP API服务器监听端口, 5700
  port: 5700
  # 可选，访问口令, 默认为空, 即不设置Token
  accessToken: ''
  # 可选，事件及数据上报URL, 默认为空, 即不上报
  postUrl: ''
  # 可选，上报消息格式，string 为字符串格式，array 为数组格式, 默认为string
  postMessageFormat: string
  # 可选，上报数据签名密钥, 默认为空
  secret: ''
# 可选，反向客户端服务
ws_reverse: 
  # 可选，是否启用反向客户端，默认启用（自行修改的私心 - Nonebot）
  enable: true
  # 上报消息格式，string 为字符串格式，array 为数组格式
  postMessageFormat: string
  # 反向Websocket主机
  reverseHost: 127.0.0.1
  # 反向Websocket端口
  reversePort: 10676
  # 访问口令, 默认为空, 即不设置Token
  accessToken: ''
  # 反向Websocket路径
  reversePath: /ws
  # 可选, 反向Websocket Api路径, 默认为reversePath
  reverseApiPath: /api
  # 可选, 反向Websocket Event路径, 默认为reversePath
  reverseEventPath: /event
  # 是否使用Universal客户端 默认为true
  useUniversal: true
  # 可选, 是否通过HTTPS连接, 默认为false
  useTLS: false
  # 反向 WebSocket 客户端断线重连间隔，单位毫秒
  reconnectInterval: 3000
  # 由于个人修改水平，分身版无了
# 正向Websocket服务器
ws: 
  # 可选，是否启用正向Websocket服务器，默认不启用
  enable: false
  # 可选，上报消息格式，string 为字符串格式，array 为数组格式, 默认为string
  postMessageFormat: string
  # 可选，访问口令, 默认为空, 即不设置Token
  accessToken: ''
  # 监听主机
  wsHost: 0.0.0.0
  # 监听端口
  wsPort: 8080
# 由于个人修改水平，多Bot配置也无了
```

## 计划

- [x] 反向Websocket客户端
- [x] HTTP上报服务
- [x] Websocket服务端
- [x] HTTP API

#### 实现
<details>
<summary>已实现CQ码</summary>

- [CQ:at]
- [CQ:image]
- [CQ:record] # 目前仅[Embedded版本](https://github.com/yyuueexxiinngg/cqhttp-mirai/tree/embedded)支持
- [CQ:face]
- [CQ:emoji]
- [CQ:share]
- [CQ:contact]
- [CQ:music]
- [CQ:shake]
- [CQ:poke]
- [CQ:xml]
- [CQ:json]

</details>

<details>
<summary>已支持的CQHTTP API</summary>

#### 特别注意, 很多信息Mirai不支持获取, 如群成员的年龄、性别等, 为保证兼容性, 这些项已用`Unknown`, `0`之类的信息填充占位

| API                      | 功能                                                         | 备注                        |
| ------------------------ | ------------------------------------------------------------ | -------------------------- |
| /send_private_msg        | [发送私聊消息](https://github.com/richardchien/cqhttp-protocol/blob/master/v11/specs/api/public.md#send_private_msg-发送私聊消息) | |
| /send_group_msg          | [发送群消息](https://github.com/richardchien/cqhttp-protocol/blob/master/v11/specs/api/public.md#send_group_msg-发送群消息) | |
| /send_msg                | [发送消息](https://github.com/richardchien/cqhttp-protocol/blob/master/v11/specs/api/public.md#send_msg-发送消息) | (不包含讨论组消息) |
| /delete_msg              | [撤回信息](https://github.com/richardchien/cqhttp-protocol/blob/master/v11/specs/api/public.md#delete_msg-撤回消息) | |
| /set_group_kick          | [群组T人](https://github.com/richardchien/cqhttp-protocol/blob/master/v11/specs/api/public.md#set_group_kick-群组踢人) | |
| /set_group_ban           | [群组单人禁言](https://github.com/richardchien/cqhttp-protocol/blob/master/v11/specs/api/public.md#set_group_ban-群组单人禁言) | |
| /set_group_whole_ban     | [群组全员禁言](https://github.com/richardchien/cqhttp-protocol/blob/master/v11/specs/api/public.md#set_group_whole_ban-群组全员禁言) | |
| /set_group_card          | [设置群名片(群备注)](https://github.com/richardchien/cqhttp-protocol/blob/master/v11/specs/api/public.md#set_group_card-设置群名片（群备注）) | |
| /set_group_leave         | [退出群组](https://github.com/richardchien/cqhttp-protocol/blob/master/v11/specs/api/public.md#set_group_leave-退出群组) | |
| /set_group_special_title | [设置群组专属头衔](https://github.com/richardchien/cqhttp-protocol/blob/master/v11/specs/api/public.md#set_group_special_title-设置群组专属头衔) | |
| /set_friend_add_request  | [处理加好友请求](https://github.com/richardchien/cqhttp-protocol/blob/master/v11/specs/api/public.md#set_friend_add_request-处理加好友请求) | |
| /set_group_add_request   | [处理加群请求/邀请](https://github.com/richardchien/cqhttp-protocol/blob/master/v11/specs/api/public.md#set_group_add_request-处理加群请求／邀请) | |
| /get_login_info          | [获取登录号信息](https://github.com/richardchien/cqhttp-protocol/blob/master/v11/specs/api/public.md#get_login_info-获取登录号信息) | |
| /get_friend_list         | [获取好友列表](https://github.com/richardchien/cqhttp-protocol/blob/master/v11/specs/api/public.md#get_friend_list-获取好友列表) | |
| /get_group_list          | [获取群列表](https://github.com/richardchien/cqhttp-protocol/blob/master/v11/specs/api/public.md#get_group_list-获取群列表) | |
| /get_group_info          | [获取群信息](https://github.com/richardchien/cqhttp-protocol/blob/master/v11/specs/api/public.md#get_group_info-获取群信息) | |
| /get_group_member_info   | [获取群成员信息](https://github.com/richardchien/cqhttp-protocol/blob/master/v11/specs/api/public.md#get_group_member_info-获取群成员信息) | |
| /get_group_member_list   | [获取群成员列表](https://github.com/richardchien/cqhttp-protocol/blob/master/v11/specs/api/public.md#get_group_member_list-获取群成员列表) | |
| /can_send_image          | [检查是否可以发送图片](https://github.com/richardchien/cqhttp-protocol/blob/master/v11/specs/api/public.md#can_send_image-检查是否可以发送图片) | (恒为true) |
| /can_send_record         | [检查是否可以发送语音](https://github.com/richardchien/cqhttp-protocol/blob/master/v11/specs/api/public.md#can_send_record-检查是否可以发送语音) | |
| /get_status              | [获取插件运行状态](https://github.com/richardchien/cqhttp-protocol/blob/master/v11/specs/api/public.md#get_status-获取插件运行状态) | (不完全支持, 仅返回`online`和`good`两项) |
| /get_version_info        | [获取 酷Q 及 CQHTTP插件的版本信息](https://github.com/richardchien/cqhttp-protocol/blob/master/v11/specs/api/public.md#get_version_info-获取-酷q-及-cqhttp-插件的版本信息) | |
| /set_group_name          | 设置群组名(拓展API)                                         |

</details>

<details>
<summary>尚未支持的CQHTTP API</summary>

| API                      | 功能                                                         | 备注                        |
| ------------------------ | ------------------------------------------------------------ | -------------------------- |
| /get_image               | [获取图片](https://github.com/richardchien/cqhttp-protocol/blob/master/v11/specs/api/public.md#get_image-获取图片) | |
| /get_record              | [获取语音](https://github.com/richardchien/cqhttp-protocol/blob/master/v11/specs/api/public.md#get_record-获取语音) | |
| /send_discuss_msg        | [发送讨论组消息](https://github.com/richardchien/cqhttp-protocol/blob/master/v11/specs/api/public.md#send_discuss_msg-发送讨论组消息) | 已无讨论组 |
| /set_discuss_leave       | [退出讨论组](https://github.com/richardchien/cqhttp-protocol/blob/master/v11/specs/api/public.md#set_discuss_leave-退出讨论组) | 已无讨论组 |
| /get_stranger_info       | [获取陌生人信息](https://github.com/richardchien/cqhttp-protocol/blob/master/v11/specs/api/public.md#get_stranger_info-获取陌生人信息) | |
| /set_group_anonymous_ban | [群组匿名用户禁言](https://github.com/richardchien/cqhttp-protocol/blob/master/v11/specs/api/public.md#set_group_anonymous_ban-群组匿名用户禁言) | |
| /set_group_admin         | [群组设置管理员](https://github.com/richardchien/cqhttp-protocol/blob/master/v11/specs/api/public.md#set_group_admin-群组设置管理员) | |
| /send_like               | [发送好友赞](https://github.com/richardchien/cqhttp-protocol/blob/master/v11/specs/api/public.md#end_like-发送好友赞) | Mirai不会支持 |
| /get_cookies             | [获取 Cookies](https://github.com/richardchien/cqhttp-protocol/blob/master/v11/specs/api/public.md#get_cookies-获取-cookies) | Mirai不会支持 |
| /get_csrf_token          | [获取 CSRF Token](https://github.com/richardchien/cqhttp-protocol/blob/master/v11/specs/api/public.md#get_csrf_token-获取-csrf-token) | Mirai不会支持 |
| /get_credentials         | [获取 QQ 相关接口凭证](https://github.com/richardchien/cqhttp-protocol/blob/master/v11/specs/api/public.md#get_credentials-获取-qq-相关接口凭证) | Mirai不会支持 |
| /set_restart_plugin      | [重启 CQHTTP](https://github.com/richardchien/cqhttp-protocol/blob/master/v11/specs/api/public.md#set_restart_plugin-重启-cqhttp) | |
| /clean_data_dir          | [清理数据目录](https://github.com/richardchien/cqhttp-protocol/blob/master/v11/specs/api/public.md#clean_data_dir-清理数据目录) | |
| /clean_plugin_log        | [清理日志](https://github.com/richardchien/cqhttp-protocol/blob/master/v11/specs/api/public.md#clean_plugin_log-清理日志) | |

</details>

## 开源协议

[AGPL-3.0](LICENSE) © yyuueexxiinngg

## 直接或间接引用到的其他开源项目

- [mirai-api-http](https://github.com/mamoe/mirai-api-http) -  [LICENSE](https://github.com/mamoe/mirai-api-http/blob/master/LICENSE)
- [Mirai Native](https://github.com/iTXTech/mirai-native)  -  [LICENSE](https://github.com/iTXTech/mirai-native/blob/master/LICENSE)
- [CQHTTP](https://github.com/richardchien/coolq-http-api) -  [LICENSE](https://github.com/richardchien/coolq-http-api/blob/master/LICENSE)
- [cqhttp-protocol](https://github.com/richardchien/cqhttp-protocol) 
- [go-cqhttp](https://github.com/Mrs4s/go-cqhttp)  -  [LICENSE](https://github.com/Mrs4s/go-cqhttp/blob/master/LICENSE)
