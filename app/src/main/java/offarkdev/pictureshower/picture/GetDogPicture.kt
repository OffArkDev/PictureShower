package offarkdev.pictureshower.picture

import offarkdev.pictureshower.InternetChecker
import offarkdev.pictureshower.InternetConnectionException
import offarkdev.pictureshower.network.PictureApi

class GetDogPicture(private val api: PictureApi, private val internetChecker: InternetChecker) {

    suspend operator fun invoke() = if (internetChecker.isConnected) api.getPicture()
    else throw InternetConnectionException()

}