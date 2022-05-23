package com.icoelloc.renter.screens

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.icoelloc.renter.R
import de.hdodenhof.circleimageview.CircleImageView


class MyAccountFragment : Fragment() {


    private lateinit var personName: String
    private lateinit var personEmail: String
    private var personPhoto: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val acct = GoogleSignIn.getLastSignedInAccount(activity)
        if (acct != null) {
            personName = acct.displayName.toString()
            personEmail = acct.email.toString()
            personPhoto = acct.photoUrl
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_my_account, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //val imageView: CircleImageView = view.findViewById(R.id.frgmnt_myacc_profpic)
        val email: TextView = view.findViewById(R.id.myaccc_email)
        val accName: TextView = view.findViewById(R.id.myaccc_name)

        accName.text = personName
        email.text = personEmail
        //imageView.setImageURI(personPhoto)

    }
}