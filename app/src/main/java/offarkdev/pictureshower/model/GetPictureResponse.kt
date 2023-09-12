package offarkdev.pictureshower.model

import kotlinx.serialization.Serializable

@Serializable
data class GetPictureResponse(
    val fileSizeBytes: Long,
    val url: String
)