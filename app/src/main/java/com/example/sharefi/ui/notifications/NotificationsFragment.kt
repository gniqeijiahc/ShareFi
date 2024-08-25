package com.example.sharefi.ui.notifications

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.sharefi.MainActivity
import com.example.sharefi.databinding.FragmentNotificationsBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.loader.MapboxMaps
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager


class NotificationsFragment : Fragment() {
    val FILE_PICKER_REQUEST_CODE = 123
    private var _binding: FragmentNotificationsBinding? = null
    private lateinit var mapView: MapView
    private lateinit var mapboxMaps: MapboxMaps
    private lateinit var pointAnnotationManager: PointAnnotationManager
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val LOCATION_PERMISSION_REQUEST_CODE = 1000
    private val binding get() = _binding!!
    private val database = FirebaseDatabase.getInstance("https://sharefi-84214-default-rtdb.asia-southeast1.firebasedatabase.app/")
    private val usersRef = database.getReference("users")
    private lateinit var auth: FirebaseAuth
    private lateinit var currentUser: FirebaseUser
    private lateinit var userId: String
    private lateinit var userRef: DatabaseReference

    data class User(
        var userId: String? = null,
        var SSID: String? = null,
        var email: String? = null,
        var point: Int = 0,
        var password: String? = null,
        var isSharing: Boolean? = null,
        var latitude: Double = 0.0,
        var longitude: Double = 0.0
    )
    private lateinit var user: User
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val notificationsViewModel =
            ViewModelProvider(this).get(NotificationsViewModel::class.java)
//        Plugin.Mapbox.getInstance(context, "YOUR_MAPBOX_ACCESS_TOKEN")


        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        val root: View = binding.root
        mapView = binding.mapView

        mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        requestLocationPermission()

        auth = (requireActivity() as MainActivity).auth
        currentUser = auth.currentUser!!
        userId = currentUser.uid
        userRef = usersRef.child(userId)
        usersRef.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    user = snapshot.getValue(User::class.java)!!
                    if (user != null) {
                        // Access user data here, e.g.
                        val retrievedLatitude = user.latitude
                        val retrievedLongitude = user.longitude

                    } else {
                    // Handle case where data is not found
                    Log.w("User", "User with ID $userId not found in database")
                }
                } else {
                    // Handle case where the snapshot doesn't exist
                    Log.w("User", "No data found in database for user ID $userId")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
                Log.w("User", "Error retrieving user data: $error")
            }
        })





//        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
//
//        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
//        val mapFragment = childFragmentManager
//            .findFragmentById(R.id.map) as SupportMapFragment
//        mapFragment.getMapAsync(this)

        return root
    }
    private fun getLastKnownLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val latitude = location.latitude
                    val longitude = location.longitude
                    val updates = mapOf<String, Any>(
                        "latitude" to latitude,
                        "longitude" to longitude
                    )
                    userRef.updateChildren(updates).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d("FirebaseUpdate", "Latitude updated successfully")
                        } else {
                            Log.e("FirebaseUpdate", "Failed to update latitude", task.exception)
                        }
                    }
                } else {
                    // Handle location null case
                }
            }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                getLastKnownLocation()
            } else {
                // Permission denied, handle it appropriately
            }
        }
    }

    private fun requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            getLastKnownLocation()
        }
    }
    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
        }
        startActivityForResult(intent, FILE_PICKER_REQUEST_CODE)
    }

    // Dummy function to get other users' locations
    private fun getUserLocations(): List<LatLng> {
        // Replace with actual logic to get user locations
        return listOf(
            LatLng(-34.0, 151.0),
            LatLng(-35.0, 150.0),
            LatLng(-33.0, 152.0)
        )
    }
}
