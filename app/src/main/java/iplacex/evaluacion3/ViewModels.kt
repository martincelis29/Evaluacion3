package iplacex.evaluacion3

import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class MainVM : ViewModel() {
    val PantallaActual = mutableStateOf(Pantalla.Form)

    var onPermisoCamaraOk: () -> Unit = {}
    var onPermisoUbicacionOk: () -> Unit = {}

    var Permisos: ActivityResultLauncher<Array<String>>? = null

    val fotosConUbicacion = mutableStateListOf<FotoConUbicacion>()
    val latitud = mutableStateOf(0.0)
    val longitud = mutableStateOf(0.0)
}

data class FotoConUbicacion(
    val nombre: String,
    val foto: Uri,
    val latitud: Double,
    val longitud: Double
)