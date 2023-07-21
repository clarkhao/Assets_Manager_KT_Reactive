package com.clark.totoro.assets

import com.clark.totoro.assets.service.FileService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import kotlin.test.assertEquals

@ExtendWith(SpringExtension::class)
@SpringBootTest
class FileServiceTest {
    @Autowired
    private lateinit var fileService: FileService

    @Test
    fun `get presigned upload url list`() {
        val list = listOf<String>("a.jpeg", "b.png")
        val userId = "userId"
        val uploadUrls = fileService.clientUpload(list, userId)
        assertEquals(list.size, uploadUrls.size)
    }

}