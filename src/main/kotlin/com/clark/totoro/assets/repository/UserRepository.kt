package com.clark.totoro.assets.repository

import com.clark.totoro.assets.model.PublicUser
import com.clark.totoro.assets.model.PublicUserWithId
import com.clark.totoro.assets.model.User
import com.clark.totoro.assets.model.UserAuthor
import com.clark.totoro.assets.utils.Utils
import com.surrealdb.connection.SurrealConnection
import com.surrealdb.connection.SurrealWebSocketConnection
import com.surrealdb.driver.SyncSurrealDriver
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Repository

@Repository
class UserRepository() {
    @Value("\${db.test_db_host}")
    val host: String = ""

    @Autowired
    private lateinit var utils: Utils
    fun createUser(user: PublicUserWithId): Unit {
        val conn: SurrealConnection = SurrealWebSocketConnection(host, 443, true)
        conn.connect(30)
        try {
            SyncSurrealDriver(conn).let { driver ->
                driver.signIn("clark", "hao102681")
                driver.use("test", "test")
                if (driver.select("publicUser:${user.id}", PublicUser::class.java).isEmpty()) {
                    println(
                        driver.create(
                            "publicUser:${user.id}",
                            PublicUser(user.name, user.avatar, user.email, user.limit, user.uploaded, user.role)
                        )
                    )
                }
            }
        } catch (e: Exception) {
            throw Exception("query errors")
        } finally {
            conn.disconnect()
        }
    }

    fun getUploadLimit(id: String): Int {
        val conn: SurrealConnection = SurrealWebSocketConnection(host, 443, true)
        conn.connect(30)
        try {
            SyncSurrealDriver(conn).let { driver ->
                driver.signIn("clark", "hao102681")
                driver.use("test", "test")
                val record = if (id.startsWith("publicUser")) id else "publicUser:${id}"
                val query = driver.select(record, PublicUser::class.java)
                return if (query.isEmpty()) -1 else query.first().limit
            }
        } catch (e: Exception) {
            throw Exception("query errors")
        } finally {
            conn.disconnect()
        }
    }

    fun updateUserAdmin(id: String, user: UserAuthor): List<PublicUser> {
        val conn: SurrealConnection = SurrealWebSocketConnection(host, 443, true)
        conn.connect(30)
        try {
            SyncSurrealDriver(conn).let { driver ->
                driver.signIn("clark", "hao102681")
                driver.use("test", "test")
                val fullUser = driver.select("publicUser:$id", PublicUser::class.java)[0]
                return driver.update("publicUser:${id}", fullUser.copy(limit = user.limit, role = user.role))
            }
        } catch (e: Exception) {
            throw Exception("query errors")
        } finally {
            conn.disconnect()
        }
    }
}