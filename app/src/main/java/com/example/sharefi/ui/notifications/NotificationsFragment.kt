package com.example.sharefi.ui.notifications

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.sharefi.MainActivity
import com.example.sharefi.R
import com.example.sharefi.databinding.FragmentNotificationsBinding
import com.example.sharefi.databinding.ItemCalloutViewBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.animation.easeTo
import com.mapbox.maps.plugin.animation.flyTo
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.OnPointAnnotationClickListener
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.gestures.OnMapClickListener
import com.mapbox.maps.plugin.gestures.addOnMapClickListener
import com.mapbox.maps.viewannotation.ViewAnnotationManager
import com.mapbox.maps.viewannotation.viewAnnotationOptions

import com.google.gson.Gson
import com.google.gson.JsonElement
import kotlin.math.log

class NotificationsFragment : Fragment() , OnMapClickListener {

    private var _binding: FragmentNotificationsBinding? = null
    private lateinit var mapView: MapView
    private lateinit var mapboxMap: MapboxMap
    private lateinit var pointAnnotationManager: PointAnnotationManager
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val LOCATION_PERMISSION_REQUEST_CODE = 1000
    private lateinit var red_maker : Bitmap
    private lateinit var viewAnnotationManager: ViewAnnotationManager
    private val viewAnnotationViews = mutableListOf<View>()
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
    private var userList = mutableListOf<User>()
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

        //map
        mapView = binding.mapView
        mapboxMap = mapView.getMapboxMap()
        mapboxMap.loadStyleUri(Style.MAPBOX_STREETS)
        //mapboxMap.addOnMapClickListener(this@NotificationsFragment)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        requestLocationPermission()

        val annotationApi = mapView.annotations
        pointAnnotationManager = annotationApi.createPointAnnotationManager()
        viewAnnotationManager = binding.mapView.viewAnnotationManager

        val drawable = ContextCompat.getDrawable(requireContext(), R.mipmap.red_marker)
        red_maker = (drawable as BitmapDrawable).bitmap

//        val jsonElement: JsonElement = Gson().toJsonTree(user)
//        val pointAnnotationOptions: PointAnnotationOptions = PointAnnotationOptions()
//            .withPoint(Point.fromLngLat(18.06, 59.31))
//            .withIconImage(red_maker)
//            .withData(jsonElement)
//        pointAnnotationManager.create(pointAnnotationOptions)


        pointAnnotationManager.addClickListener(OnPointAnnotationClickListener {annotation ->

            Toast.makeText(requireContext(),"point clicked", Toast.LENGTH_SHORT).show()
            val jsonData = annotation.getData() // 获取 JsonElement 数据
            val user = Gson().fromJson(jsonData, User::class.java)
            addViewAnnotation(user)
            true // 返回 true 表示您已經處理了點擊事件
        })
        val cameraPosition = CameraOptions.Builder()
            .zoom(2.0)
            .center(Point.fromLngLat(18.06, 59.31))
            .build()

        // set camera position
        mapView.getMapboxMap().setCamera(cameraPosition)


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
                        mapboxMap.flyTo(
                            CameraOptions.Builder()
                                .zoom(14.0)
                                .center(Point.fromLngLat(user.longitude, user.latitude)) // Note: longitude comes first in Point.fromLngLat
                                .build(),
                            MapAnimationOptions.Builder()
                            .duration(3000) // Animation duration in milliseconds
                            .build()
                        )

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


        usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    userList = mutableListOf<User>()

                    for (userSnapshot in dataSnapshot.children) {
                        val user = userSnapshot.getValue(User::class.java)

                        user?.let { userList.add(it) }
                    }

                    // Now you have a list of User objects (userList)
                    for (user in userList) {
                        val jsonElement: JsonElement = Gson().toJsonTree(user)
                        val pointAnnotationOptions: PointAnnotationOptions = PointAnnotationOptions()
                            .withPoint(Point.fromLngLat(user.longitude, user.latitude))
                            .withIconImage(red_maker)
                            .withData(jsonElement)
                        pointAnnotationManager.create(pointAnnotationOptions)
                        Log.d("Firebase", "User: ${user.userId}, ${user.email}")
                    }
                } else {
                    Log.d("Firebase", "No users found")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("Firebase", "Error reading data: ${databaseError.message}")
            }
        })

        usersRef.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, previousChildName: String?)
            {
                val newUser = dataSnapshot.getValue(User::class.java)
                newUser?.let {
                    userList.add(it)
                    // Update your UI to reflect the new user addition
                    Log.d("Firebase", "New user added: ${newUser.userId}")
                }
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, previousChildName: String?) {
                val updatedUser = dataSnapshot.getValue(User::class.java)
                updatedUser?.let {
                    val index = userList.indexOfFirst { it.userId == updatedUser.userId }
                    if (index != -1) {
                        userList[index] = updatedUser
                        // Update your UI to reflect the user data change
                        Log.d("Firebase", "User updated: ${updatedUser.userId}")
                    }
                }
            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                val removedUser = dataSnapshot.getValue(User::class.java)
                removedUser?.let {
                    userList.removeIf { it.userId == removedUser.userId }
                    // Update your UI to reflect the user removal
                    Log.d("Firebase", "User removed: ${removedUser.userId}")
                }
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            // ... other ChildEventListener methods (onChildMoved, onCancelled)
        })
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
                    pointAnnotationManager.create(
                        PointAnnotationOptions()
                        .withPoint(Point.fromLngLat(longitude, latitude))
                        .withIconImage(red_maker)
                    )
                    moveCamerala_lo(latitude, longitude)
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

    private fun moveCamerala_lo(latitude: Double, longitude: Double) {
        mapView.getMapboxMap().easeTo(
            CameraOptions.Builder()
                .zoom(14.0)
                .center(Point.fromLngLat(longitude, latitude)) // Note: longitude comes first in Point.fromLngLat
                .build()
        )
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

    @SuppressLint("SetTextI18n")
    private fun addViewAnnotation(user: User) {
        val point = Point.fromLngLat(user.longitude, user.latitude)

        val viewAnnotation = viewAnnotationManager.addViewAnnotation(
            resId = R.layout.item_callout_view,
            options = viewAnnotationOptions {
                geometry(point)
                allowOverlap(true)
//                ignoreCameraPadding(true)
//                allowOverlapWithPuck(true)
            }
        )
        viewAnnotationViews.add(viewAnnotation)
        ItemCalloutViewBinding.bind(viewAnnotation).apply {
//            mapSSID.text = "lat=%.2f\nlon=%.2f".format(point.latitude(), point.longitude())
            try{
                mapSSID.text = user.email
                mapUser.text = user.SSID
            }
            catch (e: Exception){
                Log.d("userData", "addViewAnnotation: ")
            }

            closeNativeView.setOnClickListener {
                viewAnnotationManager.removeViewAnnotation(viewAnnotation)
                viewAnnotationViews.remove(viewAnnotation)
            }
            connectButton.setOnClickListener { b ->
                val button = b as Button
                val isSelected = button.text.toString().equals("SELECT", true)
                val pxDelta = if (isSelected) SELECTED_ADD_COEF_PX else -SELECTED_ADD_COEF_PX
                button.text = if (isSelected) "DESELECT" else "SELECT"
                viewAnnotationManager.updateViewAnnotation(
                    viewAnnotation,
                    viewAnnotationOptions {
                        selected(isSelected)
                    }
                )
                (button.layoutParams as ViewGroup.MarginLayoutParams).apply {
                    bottomMargin += pxDelta
                    rightMargin += pxDelta
                    leftMargin += pxDelta
                }
                button.requestLayout()
            }
        }
    }
    private companion object {
        const val SELECTED_ADD_COEF_PX = 25
        const val STARTUP_TEXT = "Click on a map to add a view annotation."
        const val ADD_VIEW_ANNOTATION_TEXT = "Add view annotations to re-frame map camera"
    }

    override fun onMapClick(point: Point): Boolean {

        return true
    }
}
