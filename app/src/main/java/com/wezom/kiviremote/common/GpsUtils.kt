package com.wezom.kiviremote.common

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsStatusCodes

object GpsUtils {

    val RESULT_CODE = 1000

    fun enableGPS(activity: Activity, connectionCallbacks: GoogleApiClient.ConnectionCallbacks? = null, connectionFailedListener: GoogleApiClient.OnConnectionFailedListener? = null) {
        allowGpsPermissions(activity)

        val googleApiClient = GoogleApiClient.Builder(activity).apply {
            addApi(LocationServices.API)
            connectionCallbacks?.let { addConnectionCallbacks(it) }
            connectionFailedListener?.let { addOnConnectionFailedListener(it) }
        }.build()
        googleApiClient.connect()

        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            interval = (30 * 1000).toLong()
            fastestInterval = (5 * 1000).toLong()
        }

        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest).apply {
            setAlwaysShow(true)
        }

        val result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build())
        result.setResultCallback {
            when (it.status.statusCode) {
                LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                    it.status.startResolutionForResult(activity, RESULT_CODE)
                }
            }
        }
    }

    private fun allowGpsPermissions(activity: Activity) {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        }
    }

}