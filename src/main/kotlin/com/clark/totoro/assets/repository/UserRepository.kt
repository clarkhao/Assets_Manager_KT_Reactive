package com.clark.totoro.assets.repository

import com.clark.totoro.assets.model.PublicUser
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
    fun createUser(user: User): Unit {
        val conn: SurrealConnection = SurrealWebSocketConnection(host, 443, true)
        conn.connect(30)
        try {
            SyncSurrealDriver(conn).let { driver ->
                driver.signIn("clark", "hao102681")
                driver.use("test", "test")
                if (driver.select("user:${user.publicUser}", User::class.java).isEmpty()) {
                    println(
                        driver.create(
                            "user:${user.publicUser}",
                            user.copy(publicUser = "publicUser:${user.publicUser}")
                        )
                    )
                    println(driver.create("publicUser:${user.publicUser}", PublicUser(user.name, user.avatar)))
                } else {
                    driver.update("user:${user.publicUser}", user.copy(publicUser = "publicUser:${user.publicUser}"))
                    driver.update("publicUser:${user.publicUser}", PublicUser(user.name, user.avatar))
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
                val record = if (id.startsWith("user")) id else "user:${id}"
                val query = driver.select(record, User::class.java)
                return if (query.isEmpty()) 0 else query.first().limit
            }
        } catch (e: Exception) {
            throw Exception("query errors")
        } finally {
            conn.disconnect()
        }
    }

    fun updateUserAdmin(id: String, user: UserAuthor): List<User> {
        val conn: SurrealConnection = SurrealWebSocketConnection(host, 443, true)
        conn.connect(30)
        try {
            SyncSurrealDriver(conn).let { driver ->
                driver.signIn("clark", "hao102681")
                driver.use("test", "test")
                val fullUser = driver.select("user:$id", User::class.java)[0]
                return driver.update("user:${id}", fullUser.copy(limit = user.limit, role = user.role))
            }
        } catch (e: Exception) {
            throw Exception("query errors")
        } finally {
            conn.disconnect()
        }
    }
}