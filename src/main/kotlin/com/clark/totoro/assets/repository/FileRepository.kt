package com.clark.totoro.assets.repository

import com.clark.totoro.assets.config.VerifyConfig
import com.clark.totoro.assets.model.*
import com.clark.totoro.assets.service.FileService
import com.clark.totoro.assets.utils.Utils
import com.surrealdb.connection.SurrealConnection
import com.surrealdb.connection.SurrealWebSocketConnection
import com.surrealdb.driver.SyncSurrealDriver
import com.surrealdb.driver.model.QueryResult
import jakarta.annotation.PreDestroy
import kotlinx.coroutines.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Repository

@Repository
class FileRepository(val verify: VerifyConfig, val aws: FileService) {
    @Value("\${db.test_db_host}")
    val host: String = ""

    @Autowired
    private lateinit var utils: Utils
    fun createImage(id: String, files: List<String>): Unit {
        val conn: SurrealConnection = SurrealWebSocketConnection(host, 443, true)
        conn.connect(30)
        try {
            SyncSurrealDriver(conn).let { driver ->
                driver.signIn("clark", "hao102681")
                driver.use("test", "test")
                files.map {
                    val key = "assets/users/${id}/${it}"
                    val map = verify.getContentType()
                    val contentType = map[it.split(".").last()] as String
                    val image = S3File(key, "", contentType)
                    val current = utils.currentDatetime()
                    val userId = image.key.substring(13).split("/").first()
                    val name = image.key.substring(13).split("/").last()
                    val url = "https://doggycatty.s3.amazonaws.com/$key"
                    val presigned = PresignedImage(name, current, current, current, url, "publicUser:$userId")
                    driver.create(
                        "presignedImage:`${presigned.publicUser.split(":").last()}:${presigned.name}`",
                        presigned
                    )
                }
            }
        } catch (e: Exception) {
            println(e.message)
            throw Exception("query errors")
        } finally {
            conn.disconnect()
        }
    }

    fun getFiles(userId: String): List<PresignedImage> {
        val conn: SurrealConnection = SurrealWebSocketConnection(host, 443, true)
        conn.connect(30)
        try {
            SyncSurrealDriver(conn).let { driver ->
                driver.signIn("clark", "hao102681")
                driver.use("test", "test")
                val current = utils.currentDatetime()
                val query = driver.query(
                    """select * from presignedImage where publicUser='publicUser:${userId}';""",
                    null,
                    PresignedImage::class.java
                )
                return query[0].result
            }
        } catch (e: Exception) {
            println(e.message)
            throw Exception("query errors")
        } finally {
            conn.disconnect()
        }
    }

    fun updateFile(file: PresignedImage) {
        val conn: SurrealConnection = SurrealWebSocketConnection(host, 443, true)
        conn.connect(30)
        try {
            SyncSurrealDriver(conn).let { driver ->
                driver.signIn("clark", "hao102681")
                driver.use("test", "test")
                val key = "assets/users/${file.publicUser.split(":").last()}/${file.name}"
                val map = verify.getContentType()
                val contentType = map[file.name.split(".").last()] as String
                val image = S3File(key, "", contentType)
                val presigned = aws.getPresignedFileUrl(image)
                val copy = file.copy(
                    updatedTime = presigned.updatedTime,
                    expiredTime = presigned.updatedTime,
                    url = presigned.url
                )
                println("presignedImage:<${copy.publicUser.split(":").last()}:${copy.name}>")
                //driver.update("""presignedImage:<"${copy.publicUser.split(":").last()}:${copy.name}">""", copy)
                driver.query(
                    """update presignedImage:`${
                        copy.publicUser.split(":").last()
                    }:${copy.name}` set updatedTime='${presigned.updatedTime}', expiredTime='${presigned.expiredTime}', url='${presigned.url}';""",
                    null,
                    PresignedImage::class.java
                )
            }
        } catch (e: Exception) {
            println(e.message)
            throw Exception("query errors")
        } finally {
            conn.disconnect()
        }
    }

    fun deleteFiles(names: List<String>) {
        val conn: SurrealConnection = SurrealWebSocketConnection(host, 443, true)
        conn.connect(30)
        try {
            SyncSurrealDriver(conn).let { driver ->
                driver.signIn("clark", "hao102681")
                driver.use("test", "test")
                names.forEach {
                    driver.delete(it)
                }
            }
        } catch (e: Exception) {
            println(e.message)
            throw Exception("query errors")
        } finally {
            conn.disconnect()
        }
    }

    fun createLike(key: String, user: String) {
        val conn: SurrealConnection = SurrealWebSocketConnection(host, 443, true)
        conn.connect(30)
        try {
            SyncSurrealDriver(conn).let { driver ->
                driver.signIn("clark", "hao102681")
                driver.use("test", "test")
                val isExisted = driver.query(
                    """
                    select count() as count from like where in=$user and out=$key;
                """.trimIndent(), null, LikeCount::class.java
                )
                println(isExisted[0].result)
                if (isExisted[0].result.isEmpty()) {
                    driver.query(
                        """
                    relate $user->like->$key set time.created=time::now()+8h;
                """.trimIndent(), null, LikeQuery::class.java
                    )
                } else {
                    println("not add")
                }
            }
        } catch (e: Exception) {
            println(e.message)
            throw Exception("query errors")
        } finally {
            conn.disconnect()
        }
    }

    fun deleteLike(key: String, user: String) {
        val conn: SurrealConnection = SurrealWebSocketConnection(host, 443, true)
        conn.connect(30)
        try {
            SyncSurrealDriver(conn).let { driver ->
                driver.signIn("clark", "hao102681")
                driver.use("test", "test")
                driver.query(
                    """
                    delete like where in=$user and out=$key;
                """.trimIndent(), null, LikeQuery::class.java
                )
            }
        } catch (e: Exception) {
            println(e.message)
            throw Exception("query errors")
        } finally {
            conn.disconnect()
        }
    }

    fun getLikeData(user: String): LikeModel {
        val conn: SurrealConnection = SurrealWebSocketConnection(host, 443, true)
        conn.connect(30)
        try {
            SyncSurrealDriver(conn).let { driver ->
                driver.signIn("clark", "hao102681")
                driver.use("test", "test")
                val upload = aws.isOutLimit(user.split(":").last())
                val likeData =
                    driver.query(
                        """
                    select count(->like->(presignedImage where publicUser='${user}')) as liked, ((select count() as count from like where in='${user}' group all) || [{count: 0}]) as likes from publicUser;
                """.trimIndent(), null, LikeFullQuery::class.java
                    )[0].result.reduce { result, likeFullQuery ->
                        LikeFullQuery(
                            result.liked + likeFullQuery.liked,
                            likeFullQuery.likes
                        )
                    }
                        .let {
                            LikeModel(0, 0, it.likes.first().count, it.liked)
                        }
                return likeData.copy(limit = upload.limit, uploaded = upload.uploaded)
            }
        } catch (e: Exception) {
            println(e.message)
            throw Exception("query errors")
        } finally {
            conn.disconnect()
        }
    }
}