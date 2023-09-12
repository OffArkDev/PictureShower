package offarkdev.pictureshower

import android.net.Uri
import androidx.compose.runtime.MutableState

/** Сахар, чтобы не писать каждый раз object.value = object.value.copy **/
fun <T> MutableState<T>.self(valueAction: T.(it: T) -> T) {
    this.value = this.value.valueAction(this.value)
}

fun <T> T?.orDefault(default: T): T {
    return this ?: default
}

fun String.toUri(): Uri? {
    return try {
        Uri.parse(this)
    } catch (e: Throwable) {
        null
    }
}

