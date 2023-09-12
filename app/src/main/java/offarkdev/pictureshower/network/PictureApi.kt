package offarkdev.pictureshower.network

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import offarkdev.pictureshower.model.GetPictureResponse

class PictureApi {

    private val client: HttpClient = httpClientAndroid

    suspend fun getPicture() = client.get<GetPictureResponse>(END_POINT_GET_PICTURE)
}

private const val END_POINT_GET_PICTURE = "https://random.dog/woof.json"
