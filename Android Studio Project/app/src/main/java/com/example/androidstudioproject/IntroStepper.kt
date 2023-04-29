package com.example.androidstudioproject

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.AnimationSet
import android.view.animation.TranslateAnimation
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager


class IntroStepper : AppCompatActivity() {
    private lateinit var stepper_view_pager: ViewPager
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

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        lateinit var textView3: TextView
        lateinit var textView5: TextView
        lateinit var textView6: TextView

        // Get references to the TextViews
        textView3 = requireView().findViewById(R.id.textView3)
        textView5 = requireView().findViewById(R.id.textView5)
        textView6 = requireView().findViewById(R.id.textView6)

        // Create the animations
        val animation1 = AnimationSet(true).apply {
            addAnimation(TranslateAnimation(0f, 0f, -200f, 0f))
            addAnimation(AlphaAnimation(0f, 1f))
            duration = 1000
        }

        val animation2 = AnimationSet(true).apply {
            addAnimation(TranslateAnimation(0f, 0f, -200f, 0f))
            addAnimation(AlphaAnimation(0f, 1f))
            duration = 1000
            startOffset = 2000 // Delay the start of the animation by 1 second
        }

        val animation3 = AnimationSet(true).apply {
            addAnimation(TranslateAnimation(0f, 0f, -200f, 0f))
            addAnimation(AlphaAnimation(0f, 1f))
            duration = 1000
            startOffset = 3000 // Delay the start of the animation by 2 seconds
        }

        // Apply the animations to the TextViews
        textView3.startAnimation(animation1)
        textView5.startAnimation(animation2)
        textView6.startAnimation(animation3)
    }
}

class ThirdFragment : Fragment() {
    private lateinit var getstarted: Button
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.third_stepper, container, false)
        getstarted = view.findViewById(R.id.getstarted)
        getstarted.setOnClickListener {

            val intent = Intent(activity, SignUp::class.java)
            startActivity(intent)
        }
        return view
    }
}

class StepperPagerAdapter(fragmentManager: FragmentManager) :
    FragmentPagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getCount(): Int {
        return 2 // Change this to the number of pages you want in the stepper.
    }

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> IntroFragment()
            1 -> ThirdFragment()
            else -> throw IllegalArgumentException("Invalid position: $position")
        }
    }
}

