package offarkdev.pictureshower

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import offarkdev.pictureshower.picture.ImageState
import offarkdev.pictureshower.picture.PictureEvent.ShowNoInternetException
import offarkdev.pictureshower.picture.PictureEvent.ShowUnknownException
import offarkdev.pictureshower.picture.PictureScreen
import offarkdev.pictureshower.picture.PictureViewModel
import offarkdev.pictureshower.picture.saveImageToGallery
import offarkdev.pictureshower.ui.theme.PictureShowerTheme
import org.koin.androidx.viewmodel.ext.android.viewModel


class MainActivity : ComponentActivity() {

    private val vm: PictureViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        collectSingleEvents()
        vm.getPicture()
        setContent {
            PictureShowerTheme {
                PictureScreen(vm.state,
                    onSwipe = { vm.getPicture() },
                    onImageStateChanged = { vm.updateImageState(it) },
                    onShare = { vm.onSharePicture() },
                    onSave = {
                        if (checkStoragePermissions()) {
                            saveImage()
                        } else {
                            requestForStoragePermissions()
                        }
                    })
            }
        }
    }

    private fun collectSingleEvents() {
        lifecycleScope.launch {
            vm.event.collect { event ->
                when (event) {
                    is ShowNoInternetException -> showToast(getString(R.string.no_internet))
                    is ShowUnknownException -> showToast(getString(R.string.unknown_exception))
                }
            }
        }
    }

    private fun saveImage() {
        val bitmap = (vm.state.imageState as? ImageState.Success)?.bitmap ?: return
        saveImageToGallery(bitmap, this)
    }


    private fun checkStoragePermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            ContextCompat.checkSelfPermission(
                this, WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestForStoragePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val intent = Intent()
            try {
                intent.action = Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
                val uri = Uri.fromParts("package", this.packageName, null)
                intent.data = uri
                storageActivityResultLauncher.launch(intent)
            } catch (e: Exception) {
                intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                storageActivityResultLauncher.launch(intent)
            }
        } else {
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    WRITE_EXTERNAL_STORAGE
                ), STORAGE_PERMISSION_CODE
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private val storageActivityResultLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (Environment.isExternalStorageManager()) {
                saveImage()
            } else {
                showToast("Storage Permissions Denied")
            }

        }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty()) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    saveImage()
                } else {
                    showToast("Storage Permissions Denied")
                }
            }
        }
    }

    private fun showToast(text: String) =
        Toast.makeText(
            this, text, Toast.LENGTH_SHORT
        ).show()


}

private const val STORAGE_PERMISSION_CODE = 42
