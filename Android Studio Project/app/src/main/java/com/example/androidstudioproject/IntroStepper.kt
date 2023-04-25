package com.example.androidstudioproject

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager



class IntroStepper : AppCompatActivity() {
    private lateinit var stepper_view_pager : ViewPager
    private lateinit var prefs: SharedPreferences


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = PreferenceManager.getDefaultSharedPreferences(this)
        if (prefs.getBoolean("isFirstRun", true)) {
            // show the IntroStepper
            setContentView(R.layout.activity_intro_stepper)
            stepper_view_pager = findViewById(R.id.stepper_view_pager)
            val adapter = StepperPagerAdapter(supportFragmentManager)
            stepper_view_pager.adapter = adapter

            // mark first run as false
            prefs.edit().putBoolean("isFirstRun", false).apply()
        } else {
            // intro has already been shown, skip to the next activity
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}

class IntroFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_intro, container, false)
    }
}
class SecondFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.second_stepper, container, false)
    }
}
class ThirdFragment : Fragment() {
    private lateinit var getstarted:Button
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

            val view = inflater.inflate(R.layout.third_stepper, container, false)
             getstarted = view.findViewById(R.id.getstarted)
        getstarted.setOnClickListener {
                val intent = Intent(activity, Login::class.java)
                startActivity(intent)
            }
        return view
    }
}

class StepperPagerAdapter(fragmentManager: FragmentManager) : FragmentPagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getCount(): Int {
        return 3 // Change this to the number of pages you want in the stepper.
    }

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> IntroFragment()
            1 -> SecondFragment()
            2 -> ThirdFragment()
            else -> throw IllegalArgumentException("Invalid position: $position")
        }
    }
}

