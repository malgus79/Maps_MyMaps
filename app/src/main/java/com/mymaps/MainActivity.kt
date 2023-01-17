package com.mymaps

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
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

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
        createMarker()
        map.setOnMyLocationButtonClickListener(this)  //suscribirse al listener
        map.setOnMyLocationClickListener(this)  //suscribirse al listener
        enableLocation()
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