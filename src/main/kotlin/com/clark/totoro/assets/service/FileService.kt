package com.clark.totoro.assets.service

import com.clark.totoro.assets.config.S3Config
import com.clark.totoro.assets.config.VerifyConfig
import com.clark.totoro.assets.model.PresignedImage
import com.clark.totoro.assets.model.S3File
import com.clark.totoro.assets.model.S3FileUrl
import com.clark.totoro.assets.model.Uploaded
import com.clark.totoro.assets.repository.UserRepository
import com.clark.totoro.assets.utils.Utils
import jakarta.annotation.PreDestroy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.ListObjectsRequest
import software.amazon.awssdk.services.s3.model.ListObjectsResponse
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest

@Service
class FileService(val s3: S3Config, val verify: VerifyConfig, val userRep: UserRepository) {
    @Value("\${s3.bucket}")
    private val bucket: String = ""

    @Value("\${files.upload.limit}")
    val limit: Int = 0

    @Autowired
    private lateinit var utils: Utils
    fun clientUpload(names: Array<String>, id: String): List<S3FileUrl> {
        return try {
            names.map {
                PutObjectRequest.builder()
                    .bucket(bucket)
                    .key("assets/users/$id/$it")
                    .contentType(verify.getContentType()[it.split(".").last()])
                    .build()
            }
                .map {
                    PutObjectPresignRequest.builder()
                        .signatureDuration(java.time.Duration.ofMinutes(10))
                        .putObjectRequest(it)
                        .build()
                }
                .map { s3.presigner().presignPutObject(it).url().toString() }
                .zip(names) { a, b -> S3FileUrl(b, a) }
        } catch (e: Exception) {
            throw e
        }
    }

    fun getFileList(page: Int, limit: Int, prefix: String = "assets/users/"): Sequence<S3File> {
        return getFileListAsync().contents().asSequence()
            .filter { it.key().startsWith(prefix) }
            .sortedBy { it.lastModified() }
            .filterIndexed { index, _ -> index > (page - 1) * limit }
            .take(limit)
            .map {
                S3File(it.key(), it.lastModified().toString(), it.key().split(".").last())
            }
    }

    fun getPresignedFileUrl(file: S3File): PresignedImage {
        return file.let {
            GetObjectRequest.builder()
                .bucket(bucket)
                .key(it.key)
                .responseContentType(it.contentType)
                .build()
        }.let {
            GetObjectPresignRequest.builder()
                .signatureDuration(java.time.Duration.ofDays(7))
                .getObjectRequest(it)
                .build()
        }.let {
            s3.presigner().presignGetObject(it).url().toString()
        }.let {
            val current = utils.currentDatetime()
            val expired = utils.expirtedDatetime()
            val userId = file.key.substring(13).split("/").first()
            val name = file.key.substring(13).split("/").last()
            PresignedImage(name, current, current, expired, it, "publicUser:$userId")
        }
    }

    //访问单个文件
    fun getPresignedFile(name: String): String {
        val request =
            GetObjectRequest.builder()
                .bucket("doggycatty")
                .key("assets/users/$name")
                .responseContentType(verify.getContentType()[name.split(".").last()])
                .build()
        val presignRequest =
            GetObjectPresignRequest.builder()
                .signatureDuration(java.time.Duration.ofMinutes(10))
                .getObjectRequest(request)
                .build()
        return s3.presigner().presignGetObject(presignRequest).url().toString()
    }

    //获取s3桶中的对象列表
    fun getFileListAsync(): ListObjectsResponse {
        val request: ListObjectsRequest = ListObjectsRequest.builder()
            .bucket("doggycatty")
            .build()
        return s3.s3Client().listObjects(request)
    }

    suspend fun isOutLimit(id: String): Uploaded {
        val scope = CoroutineScope(Dispatchers.IO)
        return try {
            scope.async {
                try {
                    val current = async { getUploadCurrent(id) }
                    val limit = async { userRep.getUploadLimit(id) }
                    Uploaded(current.await(), limit.await())
                } catch (e: Exception) {
                    println(e.message)
                    throw e
                }
            }.await()
        } catch (e: Exception) {
            throw e
        } finally {
            scope.cancel()
        }
    }

    fun getUploadCurrent(id: String): Int {
        return getFileListAsync().contents().asSequence()
            .filter { it.key().startsWith("assets/users/$id") }
            .count()
    }
}