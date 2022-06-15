package com.icoelloc.renter.main

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.icoelloc.renter.R
import com.icoelloc.renter.login.LoginActivity
import com.icoelloc.renter.utils.CirculoTransformacion
import com.icoelloc.renter.utils.MyApp
import com.icoelloc.renter.utils.Utils
import com.squareup.picasso.Picasso


class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var auth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_Renter)
        setContentView(R.layout.activity_main)

        auth = Firebase.auth



        configuracionNavDrawer()

        initPermisos()
        comprobarConexion()
        initUI()

    }

    override fun onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() == 0) {
            super.onBackPressed();
        } else {
            getFragmentManager().popBackStack();
        }
    }

    private fun configuracionNavDrawer() {
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_mi_domicilio,
                R.id.nav_mis_propiedades,
                R.id.nav_cerca_mi,
                R.id.nav_buscar,
                R.id.nav_mi_perfil,
                R.id.domicilio_Detalles
            ), drawerLayout
        )
        navView.setupWithNavController(navController)
    }


    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }


    private fun initUI() {
        mostrarDatosUsuarioMenu()
    }

    private fun mostrarDatosUsuarioMenu() {
        val navigationView: NavigationView = findViewById(R.id.nav_view)
        val headerView: View = navigationView.getHeaderView(0)
        val navUsername: TextView = headerView.findViewById(R.id.nav_drawer_header_username)
        val navUserEmail: TextView = headerView.findViewById(R.id.nav_drawer_header_email)
        val navUserImage: ImageView = headerView.findViewById(R.id.nav_drawer_header_profile_pi)
        navUsername.text = auth.currentUser?.displayName
        navUserEmail.text = auth.currentUser?.email

        if (auth.currentUser?.photoUrl != null) {
            Picasso.get()
                .load(auth.currentUser?.photoUrl)
                .transform(CirculoTransformacion())
                .into(navUserImage)
        }

        navUserImage.setOnClickListener{
            salirSesion()
        }
    }

    private fun salirSesion() {
        Log.i("Sesion", "Saliendo...")
        AlertDialog.Builder(this)
            .setIcon(R.drawable.exit_icon)
            .setTitle("Cerrar sesión actual")
            .setMessage("¿Desea salir de la sesión actual?")
            .setPositiveButton(getString(R.string.accept)) { dialog, which -> cerrarSesion() }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    /**
     * Cerra la sesión Actual
     */
    private fun cerrarSesion() {
        // Cerramos en Firebase
        auth.signOut()
        Log.i("Sesion", "sesionDelete ok")
        Toast.makeText(applicationContext, "Sesión cerrada", Toast.LENGTH_SHORT)
            .show()
        // Y vamos a login
        val login = Intent(applicationContext, LoginActivity::class.java)
        startActivity(login)
        finish()
    }

    private fun comprobarConexion() {
        comprobarRed()
        comprobarGPS()
    }

    private fun comprobarGPS() {
        if (!Utils.isGPSAvaliable(applicationContext)) {
            val snackbar = Snackbar.make(
                findViewById(android.R.id.content),
                "Es necesaria una conexión a GPS",
                Snackbar.LENGTH_INDEFINITE
            )
            snackbar.setActionTextColor(getColor(R.color.renter_nav_drawer))
            snackbar.setAction("Conectar") {
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
            snackbar.show()
        }
    }

    private fun comprobarRed() {
        if (!Utils.isNetworkAvailable(applicationContext)) {
            val snackbar = Snackbar.make(
                findViewById(android.R.id.content),
                "Es necesaria una conexión a internet",
                Snackbar.LENGTH_INDEFINITE
            )
            snackbar.setActionTextColor(getColor(R.color.renter_nav_drawer))
            snackbar.setAction("Conectar") {
                val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
                startActivity(intent)
            }
            snackbar.show()
        }
    }

    private fun initPermisos() {
        if (!(this.application as MyApp).APP_PERMISOS)
            (this.application as MyApp).initPermisos()
    }

}