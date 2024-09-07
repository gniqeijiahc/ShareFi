package com.example.sharefi.ui.profile

import android.content.Intent
import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.example.direct_share.DirectNetShare
import com.example.sharefi.MainActivity
import com.example.sharefi.R
import com.example.sharefi.databinding.FragmentHomeBinding
import com.example.sharefi.databinding.FragmentProfileBinding
import com.example.sharefi.ui.home.HomeFragment
import com.example.sharefi.ui.login.LoginActivity
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!


    private lateinit var share: DirectNetShare
    private lateinit var auth: FirebaseAuth
    private lateinit var currentUser: FirebaseUser
    private lateinit var userId: String
    private lateinit var user: HomeFragment.User


    private val database = FirebaseDatabase.getInstance("https://sharefi-84214-default-rtdb.asia-southeast1.firebasedatabase.app/")
    private val usersRef = database.getReference("users")
    companion object {
        fun newInstance() = ProfileFragment()
    }

    private val viewModel: ProfileViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        val root: View = binding.root

        share = (requireActivity() as MainActivity).share
        auth = (requireActivity() as MainActivity).auth
        currentUser = auth.currentUser!!
        userId = currentUser.uid
        user = HomeFragment.User(userId = userId, email = currentUser.email)

        val username = binding.profileUsername
        val useremail = binding.profileEmail

        val email = user.email


        if (email != null) {
            if(email.endsWith("@gmail.com")){
                username.text = email.substring(0, email.length - 10)
            }
        }
        useremail.text = user.email
        val settingButton: Button = binding.signOutButton
        settingButton.setOnClickListener{
            // Sign out the user
            auth.signOut()

            // Optional: Redirect to login screen
            val intent = Intent(requireActivity(), LoginActivity::class.java)
            startActivity(intent)
            requireActivity().finish() // Close the current activity
        }

//        return inflater.inflate(R.layout.fragment_profile, container, false)
        return root
    }

}