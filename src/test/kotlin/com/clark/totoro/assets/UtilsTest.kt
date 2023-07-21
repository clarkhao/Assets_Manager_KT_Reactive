package com.clark.totoro.assets

import com.clark.totoro.assets.utils.Utils
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.junit.jupiter.SpringExtension
import kotlin.test.Test
import kotlin.test.assertEquals

@ExtendWith(SpringExtension::class)
@SpringBootTest
class UtilsTest() {
    @Autowired
    private lateinit var utils: Utils

    @Test
    fun `should encode userId to base64`() {
        val userId = "user123"
        val encoded = utils.base64Encoding(userId)
        println("encoded: $encoded")
        val expected = "dXNlcjEyMw=="
        assertEquals(expected, encoded)
    }

    @Test
    fun `should return token after bearer prefix`() {
        val token = "Bearer abc123"
        val expected = "abc123"

        val actual = utils.resolveToken(token)

        assertEquals(expected, actual)
    }
}