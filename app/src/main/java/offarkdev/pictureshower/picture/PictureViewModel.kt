package offarkdev.pictureshower.picture

import android.graphics.Bitmap
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import offarkdev.pictureshower.InternetConnectionException
import offarkdev.pictureshower.analytics.AnalyticsManager
import offarkdev.pictureshower.analytics.PictureUpdateEvent
import offarkdev.pictureshower.analytics.SharePictureEvent
import offarkdev.pictureshower.picture.ImageState.Empty
import offarkdev.pictureshower.self
import timber.log.Timber

class PictureViewModel(
    private val getDogPicture: GetDogPicture,
    private val analytics: AnalyticsManager,
) : ViewModel() {

    private val _state = mutableStateOf(PictureState())
    val state
        get() = _state.value

    private val _event = MutableSharedFlow<PictureEvent>()
    val event: Flow<PictureEvent> = _event

    private val errorHandler = CoroutineExceptionHandler { context, error ->
        Timber.i(error)
        _state.self { copy(isRefreshing = false) }
        if (error is InternetConnectionException)
            sendSingleEvent(PictureEvent.ShowNoInternetException)
        else sendSingleEvent(PictureEvent.ShowNoInternetException)
    }

    fun getPicture() {
        if (state.isRefreshing) return

        _state.self { copy(isRefreshing = true) }
        analytics.sendEvent(PictureUpdateEvent())

       viewModelScope.launch(errorHandler) {
            val response = getDogPicture()
            _state.self { copy(response.url, false) }
        }
    }

    fun updateImageState(newState: ImageState) {
        _state.self { copy(imageState = newState) }
    }

    fun onSharePicture() {
        analytics.sendEvent(SharePictureEvent())
    }

    private fun sendSingleEvent(event: PictureEvent) =
        viewModelScope.launch { _event.emit(event) }
}


data class PictureState(
    val url: String = "",
    val isRefreshing: Boolean = false,
    val imageState: ImageState = Empty,
)

sealed class ImageState {
    object Empty : ImageState()
    object Error : ImageState()
    object Loading : ImageState()
    data class Success(val bitmap: Bitmap) : ImageState()
}

sealed class PictureEvent {
    object ShowNoInternetException : PictureEvent()
    object ShowUnknownException : PictureEvent()
}
