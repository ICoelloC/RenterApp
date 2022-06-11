package com.icoelloc.renter.utils

import android.Manifest
import android.app.Application
import android.util.Log
import android.widget.Toast
import com.icoelloc.renter.R
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener

class MyApp : Application() {

    var permissionCamera = false
    var permissionGallery = false
    var PERMISSIONSLOCATION = false

    var APP_PERMISOS = false
        private set

    override fun onCreate() {
        super.onCreate()
        Log.i("Config", "Init Configuración")
        Log.i("Config", "Fin Configuración")
    }

    fun initPermisos() {
        Log.i("Config", "Init Permisos")
        Dexter.withContext(this)
            .withPermissions(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.CALL_PHONE
            )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    if (report.areAllPermissionsGranted()) {
                        APP_PERMISOS = true
                    }
                    if (report.isAnyPermissionPermanentlyDenied) {
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: List<PermissionRequest?>?,
                    token: PermissionToken,
                ) {
                    token.continuePermissionRequest()
                }
            }).withErrorListener { Toast.makeText(applicationContext, "¡Error al inicializar los permisos! ", Toast.LENGTH_SHORT).show() }
            .onSameThread()
            .check()
        Log.i("Config", "Fin Permisos")
    }

    fun initPermissesGallery():Boolean {
        //ACTIVIDAD DONDE TRABAJA
        Dexter.withContext(this)
            //PERMISOS
            .withPermissions(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
            )//LISTENER DE MULTIPLES PERMISOS
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    if (report.areAllPermissionsGranted()) {
                        permissionGallery = true
                    }
                    // COMPROBAMOS QUE NO HAY PERMISOS SIN ACEPTAR
                    if (report.isAnyPermissionPermanentlyDenied) {
                        permissionGallery = false
                    }
                }//NOTIFICAR DE LOS PERMISOS

                override fun onPermissionRationaleShouldBeShown(
                    permissions: List<PermissionRequest?>?,
                    token: PermissionToken
                ) {
                    token.continuePermissionRequest()
                }
            }).withErrorListener {
                Toast.makeText(
                    this?.applicationContext,
                    getString(R.string.need_gallery),
                    Toast.LENGTH_SHORT
                ).show()
            }
            .onSameThread()
            .check()
        return permissionGallery
    }

    fun initPermissesCamera(): Boolean {
        //ACTIVIDAD DONDE TRABAJA
        Dexter.withContext(this)
            //PERMISOS
            .withPermissions(
                Manifest.permission.CAMERA,
            )//LISTENER DE MULTIPLES PERMISOS
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    if (report.areAllPermissionsGranted()) {
                        Log.i("util", "todos permisos camara")
                        permissionCamera = true
                    }
                    // COMPROBAMOS QUE NO HAY PERMISOS SIN ACEPTAR
                    if (report.isAnyPermissionPermanentlyDenied) {
                        permissionCamera = false
                    }
                }//NOTIFICAR DE LOS PERMISOS

                override fun onPermissionRationaleShouldBeShown(
                    permissions: List<PermissionRequest?>?,
                    token: PermissionToken
                ) {
                    token.continuePermissionRequest()
                }
            }).withErrorListener {
                Toast.makeText(
                    this?.applicationContext,
                    getString(R.string.need_camera),
                    Toast.LENGTH_SHORT
                ).show()
            }
            .onSameThread()
            .check()
        return permissionCamera
    }

}