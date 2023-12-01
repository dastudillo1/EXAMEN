package com.example.examen


import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text

import androidx.compose.ui.tooling.preview.Preview
import com.example.examen.ui.theme.EXAMENTheme
import android.content.Context
import android.icu.number.Scale
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.registerForActivityResult
import androidx.activity.viewModels
import androidx.camera.core.CameraSelector
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import kotlinx.coroutines.Dispatchers
import com.example.examen.db.AppDatabase
import com.example.examen.db.Lugar
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.*

import androidx.compose.material3.Button
import androidx.compose.material3.TextField
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

import androidx.compose.ui.unit.dp


import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter

import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign

import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import androidx.compose.foundation.layout.Column as Column1

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import kotlinx.coroutines.withContext


class MainActivity : ComponentActivity() {
    // Se inicializa la ViewModel AppVM usando viewModels()
    val appVM:AppVM by viewModels()
    // Se registra un lanzador de actividad para solicitar permisos
    val lanzadorPermisos = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ){
        if(
            (it[android.Manifest.permission.ACCESS_FINE_LOCATION]?:false) or
            (it[android.Manifest.permission.ACCESS_COARSE_LOCATION]?:false)
        ) {
            appVM.permisosUbicacionOK()
        }else{
            Log.v("lanzadorPermisos callback","Se denegaron los permisos")
        }
        appVM.permisosUbicacionOK()
    }
    // Se llama cuando se crea la actividad
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Se establece HomeScreen como la pantalla principal y se pasan la ViewModel y el lanzador
        setContent {
            HomeScreen(appVM,lanzadorPermisos)
        }
    }
}

@Composable
fun HomeScreen(appVM: AppVM, lanzadorPermisos: ActivityResultLauncher<Array<String>>) {

    val contexto = LocalContext.current

    var isFormDisplayed by remember { mutableStateOf(false) }
    var savedLugar by remember { mutableStateOf<Lugar?>(null) }
    var listaDeLugares by remember { mutableStateOf<List<Lugar>>(emptyList()) }

    val modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)

    LaunchedEffect(key1 = true) {
        // Acceder a la base de datos en un hilo secundario


        withContext(Dispatchers.IO) {

            listaDeLugares = AppDatabase.getInstance(contexto).lugarDao().getAll()
        }
    }

    if (isFormDisplayed) {
        AddPlaceForm(
            onDismiss = { isFormDisplayed = false },
            onSave = { lugar ->
                savedLugar = lugar
                isFormDisplayed = false
            }
        )
    } else if (savedLugar != null) {
        ShowPlaceDetails(
            lugar = savedLugar!!,
            appVM = appVM,
            lanzadorPermisos = lanzadorPermisos,
            // Añadimos el manejo del botón de regreso
            onBack = {
                savedLugar = null // Limpiamos el lugar actual para volver a la lista de lugares
            }
        )
    } else {
        // Verificar si la lista de lugares está vacía
        if (listaDeLugares.isNotEmpty()) {
            // Mostrar la lista de lugares solo si hay lugares en la lista
            Column1(
                modifier = modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listaDeLugares.forEach { lugar ->
                    Column1(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                savedLugar = lugar
                            }
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        Text(text = "${lugar.lugar}",fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = buildAnnotatedString {
                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold,fontSize = 18.sp),) {
                                    append("Costo x noche: ")
                                }
                                append(lugar.costoa) // Valor sin negrita
                            }
                        )
                        Text(
                            text = buildAnnotatedString {
                                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold,fontSize = 18.sp),) {
                                    append("Traslado: ")
                                }
                                append(lugar.costot) // Valor sin negrita
                            }
                        )

                    }
                }
            }
        } else {
            // No hay lugares disponibles en la lista
            Text(text = "No hay lugares guardados")
        }

        // Mostrar el botón para agregar un nuevo lugar
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_add),
                contentDescription = "Agregar lugar",
                modifier = Modifier
                    .size(48.dp)
                    .clickable { isFormDisplayed = true }
            )
            Text(
                text = "Agregar Lugar",
                modifier = Modifier
                    .padding(start = 16.dp)
                    .clickable { isFormDisplayed = true }
            )
        }
    }
}

@Composable
fun AddPlaceForm(onDismiss: () -> Unit, onSave: (lugar: Lugar) -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    val contexto = LocalContext.current
    // Variable que almacena el lugar y su estado
    var lugar by remember { mutableStateOf(Lugar(0, "", "", "", "", "", "", "")) }
    // Superficie de fondo del formulario
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White // Color de fondo del formulario
    ) {
        Column1(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Varias secciones de TextFields para ingresar datos del lugar
            TextAndTextField(
                label = "Lugar",
                placeholder = "Ingrese un lugar",
                value = lugar.lugar,
                onValueChange = { lugar = lugar.copy(lugar = it) }
            )
            TextAndTextField(
                label = "Imagen Ref.",
                placeholder = "Ingrese una imagen",
                value = lugar.imagen,
                onValueChange = { lugar = lugar.copy(imagen = it) }
            )
            TextAndTextField(
                label = "Lat, Long.",
                placeholder = "Ingrese latitud y longitud",
                value = lugar.latlong,
                onValueChange = { lugar = lugar.copy(latlong = it) }
            )
            TextAndTextField(
                label = "Orden",
                placeholder = "Ingrese un valor de orden",
                value = lugar.orden,
                onValueChange = { lugar = lugar.copy(orden = it) }
            )
            TextAndTextField(
                label = "Costo Alojamiento",
                placeholder = "Ingrese el costo de alojamiento",
                value = lugar.costoa,
                onValueChange = { lugar = lugar.copy(costoa = it) }
            )
            TextAndTextField(
                label = "Costo Traslados",
                placeholder = "Ingrese el costo de traslados",
                value = lugar.costot,
                onValueChange = { lugar = lugar.copy(costot = it) }
            )
            TextAndTextField(
                label = "Comentarios",
                placeholder = "Ingrese comentarios",
                value = lugar.comentarios,
                onValueChange = { lugar = lugar.copy(comentarios = it) }
            )
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    coroutineScope.launch(Dispatchers.IO) {
                        val nuevoLugar = lugar.copy(
                            lugar = lugar.lugar,
                            imagen = lugar.imagen,
                            //imagen = "https://es.wikipedia.org/wiki/Parque_nacional_Torres_del_Paine#/media/Archivo:Torres_del_Paine_y_cuernos_del_Paine,_montaje.jpg",

                            latlong = lugar.latlong,
                            orden = lugar.orden,
                            costoa = lugar.costoa,
                            costot = lugar.costot,
                            comentarios = lugar.comentarios
                        )
                        // Se inserta el nuevo lugar en la base de datos
                        val lugarDao = AppDatabase.getInstance(
                            contexto
                        ).lugarDao()
                        lugarDao.insertar(nuevoLugar)

                        // Se llama a la función onSave para guardar el lugar y luego se cierra el formulario
                        onSave(nuevoLugar)
                        onDismiss()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextAndTextField(label: String, placeholder: String, value: String, onValueChange: (String) -> Unit) {
    Column1(
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        //etiqueta para el campo de texto
        Text(text = label)
        //campo de texto para ingresar info
        TextField(
            value = value,
            onValueChange = { newValue -> onValueChange(newValue) },
            placeholder = { Text(placeholder) },
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color.Black, RoundedCornerShape(4.dp))
                .padding(8.dp)
        )
    }
}


@Composable
fun ShowPlaceDetails(lugar: Lugar, appVM:AppVM,lanzadorPermisos:ActivityResultLauncher<Array<String>>,onBack: () -> Unit) {
    val contexto = LocalContext.current
    Column1(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Column1(
            modifier = Modifier.weight(0.8f)
        ) {
            // Campo lugar
            Column1(
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(text = "${lugar.lugar}", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }
            //campo imagen

            /*Column1(
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                val url = lugar.imagen // Obtener la URL de la imagen desde el campo correspondiente

                Image(
                    painter = rememberImagePainter(data = url),
                    contentDescription = "Imagen",
                    modifier = Modifier
                        .size(100.dp)
                        .fillMaxHeight() // Cambiado a fillMaxHeight
                        .background(Color.Gray),
                    contentScale = ContentScale.Crop
                )
            */

            // Costo x noche y traslado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Column1 {
                    Text(text = "Costo x noche:", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(text = "${lugar.costoa}", textAlign = TextAlign.Center)
                }
                Spacer(modifier = Modifier.width(30.dp))
                Column1 {
                    Text(text = "Traslado:", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(text = "${lugar.costot}", textAlign = TextAlign.Center)
                }
            }

            // Comentarios
            Column1(
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(text = "Comentarios:", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(text = lugar.comentarios, textAlign = TextAlign.Center)


            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Botón de actualizar ubicación
                Button(
                    onClick = {
                        appVM.permisosUbicacionOK = {
                            conseguirUbicacion(contexto) {
                                appVM.latitud.value = it.latitude
                                appVM.longitud.value = it.longitude
                            }
                        }
                        lanzadorPermisos.launch(
                            arrayOf(
                                android.Manifest.permission.ACCESS_FINE_LOCATION,
                                android.Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    },
                    modifier = Modifier.weight(1f).padding(16.dp)
                ) {
                    Text("Actualizar Ubicación")
                }

                // Botón para volver a la lista de lugares
                Button(
                    onClick = { onBack() },
                    modifier = Modifier.weight(1f).padding(16.dp)
                ) {
                    Text("Volver")
                }
            }
            Text("Lat:${appVM.latitud.value} Long:${appVM.longitud.value}")
            Spacer(Modifier.height(50.dp))
            MapaOsmUI(appVM)
        }
    }
}

// Composable UI para mostrar un mapa
@Composable
fun MapaOsmUI(appVM:AppVM) {
    val contexto = LocalContext.current
    AndroidView(
        factory = {
            MapView(it).also {
                it.setTileSource(TileSourceFactory.MAPNIK)
                Configuration.getInstance().userAgentValue = contexto.packageName
                it.controller.setZoom(15.0)
            }
        }, update = {
            it.overlays.removeIf { true }
            it.invalidate()
            //it.controller.setZoom(18.0)
            val geoPoint = GeoPoint(appVM.latitud.value, appVM.longitud.value)
            it.controller.animateTo(geoPoint)

            val marcador = Marker(it)
            marcador.position = geoPoint
            marcador.setAnchor(
                Marker.ANCHOR_CENTER,
                Marker.ANCHOR_CENTER)
            it.overlays.add(marcador)
        }
    )
}
class SinPermisoException(mensaje:String) : Exception(mensaje)

fun getUbicacion(contexto: Context, onUbicacionOk:(location: Location) ->
Unit):Unit {
    try {
        val servicio =
            LocationServices.getFusedLocationProviderClient(contexto)
        val tarea =
            servicio.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
        tarea.addOnSuccessListener {
            onUbicacionOk(it)
        }
    } catch (e:SecurityException) {
        throw SinPermisoException(e.message?:"No tiene permisos para conseguir la ubicación")
    }
}

class FaltaPermisoException(mensaje: String) : Exception(mensaje)

fun conseguirUbicacion(contexto: Context, onSuccess: (ubicacion: Location) -> Unit) {
    try {
        val servicio = LocationServices.getFusedLocationProviderClient(contexto)
        val tarea = servicio.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            null
        )
        tarea.addOnSuccessListener { ubicacion ->
            ubicacion?.let {
                onSuccess(it)
            }
        }
    } catch (se: SecurityException) {
        throw FaltaPermisoException("Sin permisos de ubicación")
    }
}

class AppVM: ViewModel(){
    val latitud = mutableStateOf(0.0)
    val longitud = mutableStateOf(0.0)
    var permisosUbicacionOK:()->Unit={}
}