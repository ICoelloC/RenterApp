package com.icoelloc.renter.main

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.icoelloc.renter.R

class MainActivity : AppCompatActivity() {

    private lateinit var toggle:ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val drawerLayout : DrawerLayout = findViewById(R.id.drawer_layout)
        val navView : NavigationView = findViewById(R.id.nav_view)

        toggle = ActionBarDrawerToggle(this,drawerLayout,R.string.open,R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        navView.setNavigationItemSelectedListener {
            when(it.itemId){
                R.id.nav_mi_domicilio -> Toast.makeText(applicationContext,"clicked my home",Toast.LENGTH_SHORT).show()
                R.id.nav_mis_propiedades -> Toast.makeText(applicationContext,"clicked my properties",Toast.LENGTH_SHORT).show()
                R.id.nav_cerca_mi -> Toast.makeText(applicationContext,"clicked close to me",Toast.LENGTH_SHORT).show()
                R.id.nav_buscar -> Toast.makeText(applicationContext,"clicked search",Toast.LENGTH_SHORT).show()
                R.id.nav_mi_perfil -> Toast.makeText(applicationContext,"clicked my account",Toast.LENGTH_SHORT).show()
            }
            true
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (toggle.onOptionsItemSelected(item)){
            return true
        }

        return super.onOptionsItemSelected(item)
    }
}