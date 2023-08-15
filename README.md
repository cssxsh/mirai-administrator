# Mirai Administrator

> 基于 Mirai Console 的 管理员 Bot 及其标准

[![Release](https://img.shields.io/github/v/release/cssxsh/mirai-administrator)](https://github.com/cssxsh/mirai-administrator/releases)
[![Downloads](https://img.shields.io/github/downloads/cssxsh/mirai-administrator/total)](https://repo1.maven.org/maven2/xyz/cssxsh/mirai/mirai-administrator/)
[![maven-central](https://img.shields.io/maven-central/v/xyz.cssxsh.mirai/mirai-administrator)](https://search.maven.org/artifact/xyz.cssxsh.mirai/mirai-administrator)
[![Codacy Badge](https://app.codacy.com/project/badge/Grade/8be173fe96c74059bfedd6268b8e6f0c)](https://www.codacy.com/gh/cssxsh/mirai-administrator/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=cssxsh/mirai-administrator&amp;utm_campaign=Badge_Grade)

**使用前应该查阅的相关文档或项目**

*   [User Manual](https://github.com/mamoe/mirai/blob/dev/docs/UserManual.md)
*   [Permission Command](https://github.com/mamoe/mirai/blob/dev/mirai-console/docs/BuiltInCommands.md#permissioncommand)
*   [Chat Command](https://github.com/project-mirai/chat-command)

本插件实现的功能有:

*   联系人相关 自动审批，指令查看，用户留言
*   消息相关 发送消息，撤回消息
*   群管理相关 群消息审核，自动宵禁，自动清理不发言，禁言自动退群
*   机器人异常下线时发送邮件

本插件提供[服务接口](#服务接口)以供其他插件拓展功能  
例如使用 [Mirai Content Censor](https://github.com/gnuf0rce/mirai-content-censor) 依靠百度API审查群消息  
例如使用 [Mirai Authenticator](https://github.com/cssxsh/mirai-authenticator) 验证加群请求  

## MCL 指令安装

**请确认 mcl.jar 的版本是 2.1.0+**  
`./mcl --update-package xyz.cssxsh.mirai:mirai-administrator --channel maven-stable --type plugins`

## 指令

注意: 使用前请确保可以 [在聊天环境执行指令](https://github.com/project-mirai/chat-command)   
`<...>`中的是指令名  
`[...]`表示参数，当`[...]`后面带`?`时表示参数可选  
代之自身或者群聊可以用 `~`, 例如 `/group quiet ~`  

本插件指令权限ID 格式为 `xyz.cssxsh.mirai.plugin.mirai-administrator:command.*`, `*` 是指令的第一指令名  
例如 `/send to 12345` 的权限ID为 `xyz.cssxsh.mirai.plugin.mirai-administrator:command.send`  
对 机器人发送的**联系人请求**通知消息 回复 `同意` 或 `不同意` 或 `拉黑` 即可处理  
插件提供黑名单功能，使用指令 `/contact black u12345`, 即可拉黑用户，Bot将不响应用户动作（包括其他插件的功能）  
发送异常下线邮件通知需要配置邮箱账户和密码

### AdminContactCommand

| Command                                       | Description |
|:----------------------------------------------|:------------|
| `/<contact> <delete> [contact]`               | 删除联系人       |
| `/<contact> <handle> [id] [accept]? [black]?` | 处理联系人申请     |
| `/<contact> <request>`                        | 查看申请列表      |
| `/<contact> <black> {permitteeIds}`           | 拉黑          |
| `/<contact> <white> {permitteeIds}`           | 取消拉黑        |
| `/<contact> <screen> [page]?`                 | 列出黑名单       |
| `/<contact> <backup>`                         | 触发备份功能      |

1.  `id` 是 事件id 或 好友id 或 群id
2.  `accept` 和 `black` 参数为 `true`, `yes`, `enabled`, `on`, `1` 时表示 `true` (不区分大小写)
3.  对 机器人发送的新联系人通知消息 回复 `同意` 或 `不同意` 或 `拉黑` 即可处理，详见 [联系人审批配置](#联系人审批配置)
4.  `permitteeIds` 是 权限系统的用户标识符，例如 `m12345.6789`, 可以提供多个 [PermitteeId](https://github.com/mamoe/mirai/blob/dev/mirai-console/docs/Permissions.md#%E8%A2%AB%E8%AE%B8%E5%8F%AF%E4%BA%BA-id) 一次性拉黑/取消拉黑
5.  黑名单通过 `@EventHandler(priority = EventPriority.HIGH, concurrency = ConcurrencyKind.LOCKED)` 拦截消息

### AdminFriendCommand

| Command                       | Description |
|:------------------------------|:------------|
| `/<friend> <list>`            | 好友列表        |
| `/<friend> <delete> [friend]` | 删除好友        |

### AdminGroupCommand

| Command                                       | Description |
|:----------------------------------------------|:------------|
| `/<group> <list>`                             | 群列表         |
| `/<group> <member> [group] [page]?`           | 群成员         |
| `/<group> <quit> [group]`                     | 退出群聊        |
| `/<group> <kick> [member] [reason]? [black]?` | 踢出群员        |
| `/<group> <nick> [member] [nick]`             | 群昵称         |
| `/<group> <title> [member] [title]`           | 群头衔         |
| `/<group> <mute> [member] [second]`           | 禁言          |
| `/<group> <quiet> [group] [open]?`            | 全体禁言        |
| `/<group> <admin> [member] [operation]?`      | 设置管理员       |
| `/<group> <announce> [group]`                 | 设置公告        |
| `/<group> <rank> [group] {levels}`            | 设置等级头衔      |

### AdminRecallCommand

| Command                | Description |
|:-----------------------|:------------|
| `/<recall> [contact]?` | 撤回消息        |

1.  不指定`contact`时，可以通过**回复消息**指定要撤销的消息，如果没有指定，将尝试撤销最后一条不是由指令发送者发送的消息
2.  `contact`是群员时，将尝试撤销这个群员的最后一条消息
3.  `contact`是群或好友时，将尝试撤销bot的最后一条消息

### AdminRegisteredCommand

| Command         | Description |
|:----------------|:------------|
| `/<registered>` | 查看已注册指令     |
| `/<reg>`        | 查看已注册指令     |

### AdminSendCommand

| Command                                   | Description |
|:------------------------------------------|:------------|
| `/<send> <groups> [bot]? [at]? [second]?` | 发送给所有群      |
| `/<send> <friends> [bot]? [second]?`      | 发送给所有好友     |
| `/<send> <to> [contact] [at]?`            | 发送给指定联系人    |
| `/<send> <nudge> [user]`                  | 戳一戳指定联系人    |
| `/<send> <log> {addresses}`               | 备份日志到邮箱     |

1.  `bot` 参数在命令行模式下需要指定
2.  `at` 参数为 `true`, `yes`, `enabled`, `on`, `1` 时表示 `true`, 将附加一个At
3.  `second` 参数为 延迟的秒数 例如 `/send groups 123456 false 10`
4.  `addresses` 参数为 邮箱地址

### AdminTimerCommand

| Command                                    | Description |
|:-------------------------------------------|:------------|
| `/<timer> <config>`                        | 显示当前设置      |
| `/<timer> <mute> [moment] [cron] [group]?` | 宵禁          |
| `/<timer> <cleaner> [day] [cron] [group]?` | 清理不发言       |
| `/<timer> <status> [cron] [bot]?`          | 定时发送机器人状态   |
| `/<timer> <message> [cron] [target] [at]`  | 定时发送消息      |

1.  `group` 为 要操作的群，在群聊中可以不指定

2.  `cron`, 为 CRON 表达式, 由 `秒 分钟 小时 日 月 周` 组成  
    例如 `0 0 1 * * ?` 表示每天 01:00 执行一次，`0 30 2 ? * 2-6` 表示星期一至星期五 每天 02:30 执行一次  
    可以使用在线编辑器生成 <https://www.bejson.com/othertools/cron/>  
    为防止被 空格 分成多个参数，请使用 `"` 包裹参数  

3.  `moment` 为 DURATION 表达式, 由 `PnDTnHnMn.nS` 组成  
    例如 `P1DT2H3M4.5S` 表示 一天二小时三分钟四点五秒，`PT5H` 表示 五小时

4.  mute 指令，`moment` 为零 `PT0S` 宵禁就会关闭  
    例如 `/timer mute PT5H "0 0 1 ? * 2-6"`, 将会在 星期一到星期五的凌晨01:00 禁言 5 小时  
    例如 `/timer mute PT0S "0 0 0 1 * ?"`, 将会 取消 禁言定时器  

5.  cleaner 指令，`day` 单位为天数的发言期限, 小于等于 `0` 清理不发言就会关闭  
    例如 `/timer cleaner 30 "0 0 12 ? * 1,7"`, 将会在 星期六、星期天的12:00 清理 30 天未发言的用户  
    例如 `/timer cleaner 0 "0 0 0 1 * ?"`, 将会 取消 清理不发言定时器

## 配置

### 联系人审批配置

1.  `AdminAutoApproverConfig.yml`

### 禁言自动退群配置

1.  `AdminAutoQuitConfig.yml`
2.  `mute_limit` 大于这个设置秒数的禁言会触发自动退群

### 留言配置

1.  `AdminCommentConfig.yml`
2.  `xyz.cssxsh.mirai.plugin.mirai-administrator:comment.include` 作用: 拥有此权限的用户，可以给机器人留言  

### 机器人上线消息配置

1.  `AdminOnlineMessageConfig.yml`
2.  `xyz.cssxsh.mirai.plugin.mirai-administrator:online.include` 作用: 拥有此权限的群，会发送上线通知  

### 消息审查及机器人所有者

1.  `AdminSetting.yml`
2.  `censor_types` 可选值 `IMAGE, FLASH, SERVICE, APP, AUDIO, FORWARD, VIP, MARKET, MUSIC, POKE`
3.  正则词库, 须手动添加，将会加载 censor 文件夹中的 txt 文件，每一行对应一个正则匹配，会监听文件改动，无需重启

### 邮件配置

1.  `AdminMailConfig.yml` 配置一些默认的发送对象
2.  `admin.mail.properties` 配置邮箱账号等，需要自己配置邮箱的 `mail.host`, `mail.user`, `mail.password`, `mail.from`

格式参考

QQ Mail https://service.mail.qq.com/detail/0/427
```properties
mail.host=smtp.qq.com
mail.auth=true
mail.user=xxx
mail.password=***
mail.from=cssxsh@qq.com
mail.store.protocol=smtp
mail.transport.protocol=smtp
# smtp
mail.smtp.starttls.enable=true
mail.smtp.auth=true
mail.smtp.timeout=15000
```

Gmail https://support.google.com/mail/answer/7126229  
```properties
mail.host=smtp.gmail.com
mail.auth=true
mail.user=xxx
mail.password=***
mail.from=cssxsh@gmail.com
mail.store.protocol=smtp
mail.transport.protocol=smtp
# smtp
mail.smtp.starttls.enable=true
mail.smtp.starttls.required=true
mail.smtp.auth=true
mail.smtp.timeout=15000
mail.smtp.port=587
```

## [爱发电](https://afdian.net/@cssxsh)

![afdian](.github/afdian.jpg)

## 服务接口

SPI接口 [ComparableService](src/main/kotlin/xyz/cssxsh/mirai/spi/ComparableService.kt)  
Wiki [Service Provider Interface](https://en.wikipedia.org/wiki/Service_provider_interface)  
举例 [JvmPlugin](src/main/resources/META-INF/services/net.mamoe.mirai.console.plugin.jvm.JvmPlugin)

### 内容审核

接口 [ContentCensor](src/main/kotlin/xyz/cssxsh/mirai/spi/ContentCensor.kt)  
实例 [MiraiContentCensor](src/main/kotlin/xyz/cssxsh/mirai/admin/MiraiContentCensor.kt)

### 联系人审批

接口 [FriendApprover](src/main/kotlin/xyz/cssxsh/mirai/spi/FriendApprover.kt)  
接口 [MemberApprover](src/main/kotlin/xyz/cssxsh/mirai/spi/MemberApprover.kt)  
接口 [GroupApprover](src/main/kotlin/xyz/cssxsh/mirai/spi/GroupApprover.kt)  
实例 [MiraiAutoApprover](src/main/kotlin/xyz/cssxsh/mirai/admin/MiraiAutoApprover.kt)

### 定时消息

接口 [BotTimingMessage](src/main/kotlin/xyz/cssxsh/mirai/spi/BotTimingMessage.kt)  
实例 [MiraiMessageTimer](src/main/kotlin/xyz/cssxsh/mirai/admin/MiraiMessageTimer.kt)  
实例 [MiraiStatusMessage](src/main/kotlin/xyz/cssxsh/mirai/admin/MiraiStatusMessage.kt)

### 上线操作

接口 [BotOnlineAction](src/main/kotlin/xyz/cssxsh/mirai/spi/BotOnlineAction.kt)  
实例 [MiraiOnlineMessage](src/main/kotlin/xyz/cssxsh/mirai/admin/MiraiOnlineMessage.kt)  

### 宵禁(群定时禁言)

接口 [GroupCurfewTimer](src/main/kotlin/xyz/cssxsh/mirai/spi/GroupCurfewTimer.kt)  
实例 [MiraiCurfewTimer](src/main/kotlin/xyz/cssxsh/mirai/admin/MiraiCurfewTimer.kt)

### 放风(限时权限)

接口 [GroupAllowTimer](src/main/kotlin/xyz/cssxsh/mirai/spi/GroupAllowTimer.kt)

### 群成员清理

接口 [MemberCleaner](src/main/kotlin/xyz/cssxsh/mirai/spi/MemberCleaner.kt)  
实例 [MiraiMemberCleaner](src/main/kotlin/xyz/cssxsh/mirai/admin/MiraiMemberCleaner.kt)

### 群昵称检查

接口 [MemberNickCensor](src/main/kotlin/xyz/cssxsh/mirai/spi/MemberNickCensor.kt)

### 群头衔检查

接口 [MemberTitleCensor](src/main/kotlin/xyz/cssxsh/mirai/spi/MemberTitleCensor.kt)

### 黑名单

接口 [BlackListService](src/main/kotlin/xyz/cssxsh/mirai/spi/BlackListService.kt)  
实例 [MiraiBlackList](src/main/kotlin/xyz/cssxsh/mirai/admin/MiraiBlackList.kt)

### 联系人备份

接口 [BackupService](src/main/kotlin/xyz/cssxsh/mirai/spi/BackupService.kt)  
实例 [MiraiBackupService](src/main/kotlin/xyz/cssxsh/mirai/admin/MiraiBackupService.kt)