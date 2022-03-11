package com.iodaniel.notesio

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayoutMediator
import com.google.firebase.auth.FirebaseAuth
import com.iodaniel.notesio.databinding.ActivitySignInSignUpBinding
import com.iodaniel.notesio.databinding.FragmentSignInBinding
import com.iodaniel.notesio.databinding.FragmentSignUpBinding

class SignInSignUp : AppCompatActivity() {

    private val binding by lazy { ActivitySignInSignUpBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        val fragmentSignUp = FragmentSignUp()
        val fragmentSignIn = FragmentSignIn()
        val dataset = arrayListOf("SIGN IN", "SIGN UP")
        val adapter = ViewPagerAdapter(this)
        adapter.dataset = arrayListOf(fragmentSignIn, fragmentSignUp)
        binding.signInSignUpViewpager.adapter = adapter
        TabLayoutMediator(
            binding.signInSignUpTablayout,
            binding.signInSignUpViewpager
        ) { tab, position ->
            tab.text = dataset[position]
        }.attach()

        if (binding.activitySignInUpTab != null) {
            binding.activitySignInUpExitApp!!.setOnClickListener {
                finish()
                onBackPressed()
            }
        }
    }

    inner class ViewPagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {

        lateinit var dataset: ArrayList<Fragment>

        override fun getItemCount(): Int = dataset.size

        override fun createFragment(position: Int): Fragment = dataset[position]
    }
}

interface ProgressBarStateListener {
    fun showProgress()
    fun hideProgress()
}

class FragmentSignIn : Fragment(), ProgressBarStateListener {

    private lateinit var binding: FragmentSignInBinding
    private var auth = FirebaseAuth.getInstance()
    private lateinit var progressBarStateListener: ProgressBarStateListener

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSignInBinding.inflate(inflater, container, false)
        binding.signInBtn.setOnClickListener {
            checkInput()
        }
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        progressBarStateListener = this
    }

    private fun checkInput() {
        val email = binding.fragmentSignInEmail.text.trim().toString()
        val password = binding.fragmentSignInPassword.text.trim().toString()
        if (email == "" || password == "") return
        progressBarStateListener.showProgress()
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                progressBarStateListener.hideProgress()
                val intent = Intent(requireContext(), MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                requireActivity().overridePendingTransition(0, 0)
            }.addOnFailureListener {
                progressBarStateListener.hideProgress()
                Snackbar.make(binding.root, it.localizedMessage!!, Snackbar.LENGTH_LONG).show()
            }
    }

    override fun showProgress() {
        binding.fragmentSignInProgressbar.visibility = View.GONE
        binding.fragmentSignInText.visibility = View.INVISIBLE
    }

    override fun hideProgress() {
        binding.fragmentSignInProgressbar.visibility = View.GONE
        binding.fragmentSignInText.visibility = View.VISIBLE
    }
}

class FragmentSignUp : Fragment(), ProgressBarStateListener {

    private lateinit var binding: FragmentSignUpBinding
    private var auth = FirebaseAuth.getInstance()
    private lateinit var progressBarStateListener: ProgressBarStateListener

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSignUpBinding.inflate(inflater, container, false)
        binding.signUpBtn.setOnClickListener {
            checkInput()
        }
        return binding.root
    }

    private fun checkInput() {
        val email = binding.fragmentSignUpEmail.text.trim().toString()
        val password = binding.fragmentSignUpPassword.text.trim().toString()
        if (email == "" || password == "") return
        progressBarStateListener.showProgress()
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                progressBarStateListener.hideProgress()
                val intent = Intent(requireContext(), MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                requireActivity().overridePendingTransition(0, 0)
            }.addOnFailureListener {
                progressBarStateListener.hideProgress()
                Snackbar.make(binding.root, it.localizedMessage!!, Snackbar.LENGTH_LONG).show()
            }
    }

    override fun onStart() {
        super.onStart()
        progressBarStateListener = this
    }

    override fun showProgress() {
        binding.fragmentSignInProgressbar.visibility = View.VISIBLE
        binding.fragmentSignInText.visibility = View.GONE
    }

    override fun hideProgress() {
        binding.fragmentSignInProgressbar.visibility = View.GONE
        binding.fragmentSignInText.visibility = View.VISIBLE
    }
}