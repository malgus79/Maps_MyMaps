package com.mymaps

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener
import com.google.android.gms.maps.GoogleMap.OnMyLocationClickListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*

class MainActivity : AppCompatActivity(),
    OnMapReadyCallback,
    OnMyLocationButtonClickListener,
    OnMyLocationClickListener {

    private lateinit var map: GoogleMap

    companion object {
        const val REQUEST_CODE_LOCATION = 0
    }

    //se llama cuando el mapa ya esté creado
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        //createMarker()
        createPolylines()
        map.setOnMyLocationButtonClickListener(this)  //suscribirse al listener
        map.setOnMyLocationClickListener(this)  //suscribirse al listener
        enableLocation()
    }

    private fun createPolylines() {
        val polylinesOptions = PolylineOptions()
            .add(LatLng(-31.4413, -64.1811))
            .add(LatLng(-31.4419, -64.1824))
            .add(LatLng(-31.4407, -64.1819))
            .add(LatLng(-31.4398, -64.1815))
            .add(LatLng(-31.4403, -64.1802))
            .add(LatLng(-31.4408, -64.1785))
            .add(LatLng(-31.4414, -64.1780))
            .add(LatLng(-31.4415, -64.1792))
            .add(LatLng(-31.4416, -64.1804))
            .width(15f)
            .color(ContextCompat.getColor(this, R.color.purple_500))

        val polyline: Polyline = map.addPolyline(polylinesOptions)

        polyline.isClickable = true
        map.setOnPolylineClickListener { polyline -> changeColor(polyline) }

/*
        val pattern = listOf(Dot(), Gap(10f), Dash(50f), Gap(10f))
        polyline.pattern = pattern
*/

/*
        polyline.startCap = RoundCap()
        polyline.endCap = CustomCap(BitmapDescriptorFactory.fromResource(R.drawable.the_end))
        //BitmapDescriptorFactory -> no acepta vectores
        //con un simple png funciona ok
*/

    }

    private fun changeColor(polyline: Polyline) {
        val color = (0..3).random()
        when (color) {
            0 -> polyline.color = ContextCompat.getColor(this, R.color.black)
            1 -> polyline.color = ContextCompat.getColor(this, R.color.teal_200)
            2 -> polyline.color = ContextCompat.getColor(this, R.color.purple_500)
            3 -> polyline.color = ContextCompat.getColor(this, R.color.purple_700)
        }
    }

    private fun createMarker() {
        val coordinates = LatLng(-31.416838, -64.183583)
        val marker: MarkerOptions = MarkerOptions().position(coordinates).title("Mi plaza favorita")
        map.addMarker(marker)
        map.animateCamera(
            CameraUpdateFactory.newLatLngZoom(coordinates, 16f),
            4000,
            null
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        createMapFragment()
    }

    private fun createMapFragment() {
        val mapFragmnet: SupportMapFragment =
            supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragmnet.getMapAsync(this)
    }

    //primero ver si tiene otorgados los permisos
    private fun isLocationPermissionGranted() = ContextCompat.checkSelfPermission(
        this,
        android.Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    //comprobar si realmente tiene los permisos -> si se encuentra activado entonces activar
    //la ubicacion en tiempo real
    //si no -> se le pide al usuario que los acepte
    @SuppressLint("MissingPermission")
    private fun enableLocation() {
        if (!::map.isInitialized) return  //si el mapa NO ha sido inicializado -> return

        //si ha aceptado los permisos (intentara activar la localizacion):
        if (isLocationPermissionGranted()) {
            map.isMyLocationEnabled = true

        } else {
            requestLocationPermission()  //volver a pesirselos
        }
    }

    //logica para solicitar los permisos al usuario
    private fun requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )
        ) {
            //si entra aca -> volver a pedir los permisos porque ya los habia rechazado
            Toast.makeText(this, "Ve a ajustes y acepta los permisos", Toast.LENGTH_SHORT).show()
        } else {
            //si no -> es porque es la primera vez vez que se le solicitan los permisos
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_CODE_LOCATION
            )
        }
    }

    //capturar la respuesta de cuando el usuario acepta los permisos
    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            REQUEST_CODE_LOCATION -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                //si acepto el permiso -> activar la ubicacion
                map.isMyLocationEnabled = true

            } else {
                Toast.makeText(
                    this,
                    "Para activar la localizacion va a ajustes y acepta los permisos",
                    Toast.LENGTH_LONG
                ).show()
            }
            else -> {}
        }
    }

    //cuando la aplicacion este en segundo plano y vuelva a la misma -> volver a comprobar si los permisos siguen activos
    @SuppressLint("MissingPermission")
    override fun onResumeFragments() {
        super.onResumeFragments()
        if (!::map.isInitialized) return  //si el mapa NO ha sido inicializado -> return
        if (!isLocationPermissionGranted()) {
            map.isMyLocationEnabled = false
            Toast.makeText(
                this,
                "Para activar la localizacion va a ajustes y acepta los permisos",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    //cada vez que se pulse en el radar del mapa se llama a esta fun
    override fun onMyLocationButtonClick(): Boolean {
        Toast.makeText(this, "Boton pulsado", Toast.LENGTH_SHORT).show()
        return false
    }

    //se llamara cuando se pulse en un punto "azul"
    override fun onMyLocationClick(p0: Location) {
        Toast.makeText(this, "Estas en ${p0.longitude}, ${p0.latitude}", Toast.LENGTH_SHORT).show()
    }
}