package ru.netology.nework.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.yandex.mapkit.Animation
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.InputListener
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.user_location.UserLocationLayer
import com.yandex.runtime.ui_view.ViewProvider
import ru.netology.nework.R
import ru.netology.nework.databinding.FragmentMapBinding
import ru.netology.nework.viewmodel.EventViewModel
import ru.netology.nework.viewmodel.PostViewModel

class MapFragment: Fragment(), InputListener {
    private lateinit var mapView: MapView
    private lateinit var userLocationLayer: UserLocationLayer
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var fragment: String? = null
    private var fragmentBinding: FragmentMapBinding? = null
    private val postViewModel: PostViewModel by activityViewModels()
    private val eventViewModel: EventViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapKitFactory.initialize(requireContext())
    }

    @SuppressLint("MissingPermission")
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                mapView.apply {
                    userLocationLayer.isVisible = true
                    userLocationLayer.isHeadingEnabled = false
                }
            } else {
                context?.let {
                    MaterialAlertDialogBuilder(it)
                        .setMessage(resources.getString(R.string.use_card))
                        .setNegativeButton(resources.getString(R.string.clear)) { dialog, _ ->
                            dialog.dismiss()
                            findNavController().navigateUp()
                        }
                        .show()

                }

            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val binding = FragmentMapBinding.inflate(inflater, container, false)
        fragmentBinding = binding

        mapView = binding.mapview
        userLocationLayer = MapKitFactory.getInstance().createUserLocationLayer(mapView.mapWindow)
        mapView.map.addInputListener(this)

        fusedLocationProviderClient = LocationServices
            .getFusedLocationProviderClient(requireActivity())

        val latitude = if (arguments?.getDouble("latitude") == 0.0) null else arguments?.getDouble("latitude")
        val longitude = if (arguments?.getDouble("longitude") == 0.0) null else arguments?.getDouble("longitude")
        fragment = arguments?.getString("fragment")

        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) -> {
                mapView.apply {
                    userLocationLayer.isVisible = true
                    userLocationLayer.isHeadingEnabled = false
                }

                fusedLocationProviderClient.lastLocation.addOnSuccessListener {

                    if (latitude != null && longitude != null) {
                        mapView.map.mapObjects.clear()
                        moveCamera(Point(latitude, longitude))
                        addMarker(Point(latitude, longitude))
                    } else {
                        moveCamera(Point(it.latitude, it.longitude))
                    }
                }
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }

        binding.locationSearching.setOnClickListener {
            fusedLocationProviderClient.lastLocation.addOnSuccessListener {
                moveCamera(Point(it.latitude, it.longitude))
            }
        }


        return binding.root
    }

    override fun onStop() {
        mapView.onStop()
        MapKitFactory.getInstance().onStop()
        super.onStop()
    }

    override fun onStart() {
        super.onStart()
        MapKitFactory.getInstance().onStart()
        mapView.onStart()
    }

    private fun moveCamera(point: Point) = mapView.map.move(
        CameraPosition(
            point, 15.0f, 0.0f, 0.0f
        ),
        Animation(Animation.Type.SMOOTH, 0F),
        null
    )

    private fun addMarker(point: Point) {
        val marker = View(context).apply {
            background =
                AppCompatResources.getDrawable(context, R.drawable.ic_location)
            backgroundTintList = AppCompatResources.getColorStateList(context, R.color.blue)
        }
        mapView.map.mapObjects.addPlacemark(
            point,
            ViewProvider(marker)
        )
    }

    override fun onMapTap(map: Map, point: Point) {
        mapView.map.mapObjects.clear()
        val setGeoLabel = fragmentBinding?.setGeoLabel
        setGeoLabel?.visibility = View.VISIBLE


        addMarker(point)

        setGeoLabel?.setOnClickListener {

            when (fragment) {
                "newPost" -> postViewModel.saveCoordinates(point.latitude, point.longitude)
                "newEvent" -> eventViewModel.saveCoordinates(point.latitude, point.longitude)
            }
            findNavController().navigateUp()
        }
    }

    override fun onMapLongTap(map: Map, point: Point) {
        mapView.map.mapObjects.clear()
        addMarker(point)

        when (fragment) {
            "newPost" -> postViewModel.saveCoordinates(point.latitude, point.longitude)
            "newEvent" -> eventViewModel.saveCoordinates(point.latitude, point.longitude)
        }
        findNavController().navigateUp()
    }


}
