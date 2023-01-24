@file:JvmName("BuildKt")

package xyz.cssxsh.mirai.admin.mail

import jakarta.activation.*
import jakarta.mail.*
import jakarta.mail.internet.*
import java.io.*
import java.nio.file.*
import java.util.*

/**
 * [Environment Properties](https://jakarta.ee/specifications/mail/2.1/jakarta-mail-spec-2.1.html#a823)
 */
public fun buildMailSession(block: Properties.() -> Unit): Session {
    val props = Properties(System.getProperties())
    val current = Thread.currentThread()
    val oc = current.contextClassLoader
    try {
        block.invoke(props)
    } finally {
        current.contextClassLoader = oc
    }

    if (props.getProperty("mail.smtp.localhost") == null) {
        props.setProperty("mail.smtp.localhost", props.getProperty("mail.host"))
    }

    val auth = PropsAuthenticator(props = props)

    return try {
        current.contextClassLoader = block::class.java.classLoader
        Session.getDefaultInstance(props, auth)
    } finally {
        current.contextClassLoader = oc
    }
}

/**
 * 构建邮件
 * @see MailContentBuilder
 */
public fun buildMailContent(session: Session, block: MailContentBuilder.(MimeMessage) -> Unit): MimeMessage {
    val message = MimeMessage(session)
    val builder: MailContentBuilder

    val current = Thread.currentThread()
    val oc = current.contextClassLoader
    try {
        current.contextClassLoader = block::class.java.classLoader
        builder = MailContentBuilder(session)
        block.invoke(builder, message)

        message.setFrom(builder.from)

        message.setRecipients(MimeMessage.RecipientType.TO, builder.to)

        if (builder.title.isEmpty()) throw throw IllegalArgumentException("title is empty")
        message.setSubject(builder.title, "UTF-8")

        if (builder.content.count == 0) throw throw IllegalArgumentException("content is empty")
        message.setContent(builder.content)

    } finally {
        current.contextClassLoader = oc
    }

    return message
}

@DslMarker
public annotation class MailDsl

public class MailContentBuilder(session: Session) {
    @MailDsl
    public var from: String? = session.getProperty("mail.from")

    @MailDsl
    public var to: String? = null

    @MailDsl
    public var title: String = ""

    @MailDsl
    public var content: MimeMultipart = MimeMultipart()

    @MailDsl
    public fun text(type: String = "plain", builderAction: StringBuilder.() -> Unit) {
        val part = MimeBodyPart()
        part.setText(StringBuilder().apply(builderAction).toString(), "UTF-8", type)
        content.addBodyPart(part)
    }

    @MailDsl
    public fun file(filename: String? = null, builderAction: () -> Any?) {
        val part = MimeBodyPart()
        when (val target = builderAction()) {
            is File -> {
                part.dataHandler = DataHandler(FileDataSource(target))
                part.fileName = filename ?: target.name
            }
            is Path -> {
                val file = target.toFile()
                part.dataHandler = DataHandler(FileDataSource(file))
                part.fileName = filename ?: file.name
            }
            is DataSource -> {
                part.dataHandler = DataHandler(target)
                part.fileName = filename ?: target.name ?: "data.bin"
            }
            is DataHandler -> {
                part.dataHandler = target
                part.fileName = filename ?: target.name ?: "data.bin"
            }
            is String -> {
                val file = File(target)
                part.dataHandler = DataHandler(FileDataSource(file))
                part.fileName = filename ?: file.name
            }
            Unit, null -> return
            else -> throw IllegalArgumentException("file")
        }
        content.addBodyPart(part)
    }
}

@PublishedApi
internal class PropsAuthenticator(private val props: Properties) : Authenticator() {
    override fun getPasswordAuthentication(): PasswordAuthentication {
        val user = props["mail.${requestingProtocol}.user"]?.toString()
            ?: props["mail.user"]?.toString()
            ?: System.getenv("MAIL_USER")
            ?: throw NoSuchElementException("mail.user")
        val password = props["mail.${requestingProtocol}.password"]?.toString()
            ?: props["mail.password"]?.toString()
            ?: System.getenv("MAIL_PASSWORD")
            ?: throw NoSuchElementException("mail.password")
        return PasswordAuthentication(user, password)
    }
}