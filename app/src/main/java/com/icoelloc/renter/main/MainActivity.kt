package com.icoelloc.renter.main

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.icoelloc.renter.R
import com.icoelloc.renter.screens.*


class MainActivity : AppCompatActivity() {

    private lateinit var toggle:ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val drawerLayout : DrawerLayout = findViewById(R.id.drawer_layout)
        toggle = ActionBarDrawerToggle(this,drawerLayout,R.string.open,R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val navView : NavigationView = findViewById(R.id.nav_view)
        navView.setNavigationItemSelectedListener {
            when(it.itemId){
                R.id.nav_mi_domicilio -> replaceFragment(MyHomeFragment())
                R.id.nav_mis_propiedades -> replaceFragment(MyPropertiesFragment())
                R.id.nav_cerca_mi -> replaceFragment(CloseToMeFragment())
                R.id.nav_buscar -> replaceFragment(SearchFragment())
                R.id.nav_mi_perfil -> replaceFragment(MyAccountFragment())
            }
            true
        }

        //set email and name in header
        val headerView : View = navView.getHeaderView(0)
        val email : TextView = headerView.findViewById(R.id.nav_drawer_header_email)
        val name : TextView = headerView.findViewById(R.id.nav_drawer_header_username)

        email.text = FirebaseAuth.getInstance().currentUser?.email
        name.text = FirebaseAuth.getInstance().currentUser?.displayName

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (toggle.onOptionsItemSelected(item)){
            return true
        }

        return super.onOptionsItemSelected(item)
    }


    private fun replaceFragment(fragment:Fragment){
        val fragmentManager = supportFragmentManager
        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout, fragment)
        fragmentTransaction.commit()
    }

}