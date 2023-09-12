package iplacex.evaluacion3

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.camera.core.CameraSelector
import androidx.camera.view.LifecycleCameraController
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel

enum class Pantalla {
    Form, Camara, Mapa
}

class MainActivity : ComponentActivity() {

    val mainVM: MainVM by viewModels()

    lateinit var cameraController: LifecycleCameraController

    val Permisos = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        when {
            (it[android.Manifest.permission.WRITE_EXTERNAL_STORAGE] ?: false) -> {
                Log.v("callback RequestMultiplePermissions", "permiso camara granted")
                mainVM.onPermisoCamaraOk()
            }

            (it[android.Manifest.permission.ACCESS_COARSE_LOCATION] ?: false) or
                    (it[android.Manifest.permission.ACCESS_FINE_LOCATION] ?: false) -> {
                Log.v("callback RequestMultiplePermissions", "permiso ubicacion granted")
                mainVM.onPermisoUbicacionOk()
            }

            else -> {
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        cameraController = LifecycleCameraController(this)
        cameraController.bindToLifecycle(this)
        cameraController.cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        mainVM.Permisos = Permisos

        setContent {
            Main(Permisos, cameraController)
        }
    }
}

@Composable
fun Main(
    permisos: ActivityResultLauncher<Array<String>>,
    cameraController: LifecycleCameraController
) {
    val mainVM: MainVM = viewModel()

    when (mainVM.PantallaActual.value) {
        Pantalla.Form -> {
            FormUI()
        }

        Pantalla.Camara -> {
            CamaraUI(permisos, cameraController)
        }

        Pantalla.Mapa -> {
            MapaUI()
        }
    }
}

