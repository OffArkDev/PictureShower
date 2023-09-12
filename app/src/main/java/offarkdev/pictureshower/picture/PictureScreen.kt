package offarkdev.pictureshower.picture

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.MediaStore.Images.Media.IS_PENDING
import android.provider.MediaStore.Images.Media.RELATIVE_PATH
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter.State
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import offarkdev.pictureshower.R
import offarkdev.pictureshower.picture.ImageState.Error
import offarkdev.pictureshower.picture.ImageState.Loading
import offarkdev.pictureshower.picture.ImageState.Success
import offarkdev.pictureshower.ui.theme.PictureShowerTheme
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@Composable
fun PictureScreen(
    state: PictureState,
    onSwipe: () -> Unit = {},
    onImageStateChanged: (ImageState) -> Unit = {},
    onShare: () -> Unit = {},
    onSave: () -> Unit = {},
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        val context = LocalContext.current

        SwipeRefresh(
            state = rememberSwipeRefreshState(isRefreshing = state.isRefreshing),
            onRefresh = { onSwipe() },
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
               if (state.url.isNotEmpty()) AsyncImage(
                    modifier = Modifier.fillMaxWidth().weight(1f), model = state.url, onState = {
                        val newState = when (it) {
                            is State.Loading -> Loading
                            is State.Error -> Error
                            is State.Success -> Success(it.result.drawable.toBitmap())
                            else -> ImageState.Empty
                        }
                        onImageStateChanged(newState)
                    }, contentDescription = stringResource(R.string.dog_picture)
                )
            }

        }
        when (state.imageState) {
            is Loading -> Box(
                modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }

            is Error -> Box(
                modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(id = R.string.image_loading_failure),
                    fontSize = 20.sp,
                    color = Color.Black
                )
            }

            is Success -> Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 50.dp)
            ) {
                Button(shape = RoundedCornerShape(12.dp), onClick = {
                    onShare()
                    context.sharePicture(state.url)
                }) {
                    Text(text = stringResource(R.string.share))
                }
                Button(modifier = Modifier.padding(start = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    onClick = {
                        onSave()
                    }) {
                    Text(text = stringResource(R.string.save))
                }
            }

            else -> {}
        }
    }
}


private fun Context.sharePicture(url: String) {
    val shareIntent = Intent()
    shareIntent.action = Intent.ACTION_SEND
    shareIntent.putExtra(Intent.EXTRA_TEXT, url)
    shareIntent.type = "text/plain"
    startActivity(shareIntent)
}


fun saveImageToGallery(bitmap: Bitmap, context: Context) {
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val displayName = "DOG_${timeStamp}.jpg"

    val contentValues = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, displayName)
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
    }

    val resolver: ContentResolver = context.contentResolver

    val imageUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        contentValues.put(RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
        contentValues.put(IS_PENDING, 1)
        resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
    } else {
        resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
    }
    try {
        imageUri?.let {
            val outputStream = resolver.openOutputStream(it)
            if (outputStream != null) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                outputStream.close()

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.clear()
                    contentValues.put(IS_PENDING, 0)
                    resolver.update(it, contentValues, null, null)
                }
                Toast.makeText(context, "Image saved to gallery", Toast.LENGTH_SHORT).show()
                return
            }
        }
        notifySaveImageFail(context)
    } catch (e: Exception) {
        Timber.i(e)
        notifySaveImageFail(context)
    }
}

private fun notifySaveImageFail(context: Context) =
    Toast.makeText(context, context.getString(R.string.failed_to_save_image), Toast.LENGTH_SHORT).show()


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    PictureShowerTheme {
        PictureScreen(PictureState())
    }
}