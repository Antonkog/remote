package com.kivi.remote.presentation.home.tutorial

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import com.kivi.remote.R
import com.kivi.remote.databinding.TutorialFragmentBinding
import com.kivi.remote.presentation.base.BaseFragment
import com.kivi.remote.presentation.base.BaseViewModelFactory
import com.kivi.remote.presentation.home.HomeActivity
import javax.inject.Inject


//
// Created by Antonio on 3/18/20.
// email: akogan777@gmail.com
//
class TutorialFragment : BaseFragment() {

    lateinit var viewModel: TutorialViewModel

    private lateinit var binding: TutorialFragmentBinding

    @Inject
    lateinit var viewModelFactory: BaseViewModelFactory

    private lateinit var demoCollectionAdapter: TutorialsAdapter

    override fun injectDependencies() = fragmentComponent.inject(this)

    fun init() {

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = TutorialFragmentBinding.inflate(inflater, container!!, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(TutorialViewModel::class.java)
        (activity as HomeActivity).changeFabVisibility(View.GONE)

        demoCollectionAdapter = TutorialsAdapter()
        binding.tutorialsParger.adapter = demoCollectionAdapter

        TabLayoutMediator(binding.tabsTutorial, binding.tutorialsParger)
        { tab, position -> }.attach()


        binding.tutorialsParger.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                when (position) {
                    4 -> {
                        binding.textSkipTutorial.visibility = View.GONE
                        binding.buttonStartContainer.visibility = View.VISIBLE
                        binding.tabsTutorial.visibility = View.GONE
                    }
                    else -> {
                        binding.textSkipTutorial.visibility = View.VISIBLE
                        binding.tabsTutorial.visibility = View.VISIBLE
                        binding.buttonStartContainer.visibility = View.GONE
                    }
                }
            }
        })


        binding.textSkipTutorial.setOnClickListener(View.OnClickListener {
            binding.tutorialsParger.currentItem = binding.tutorialsParger.currentItem + 1
        })


        binding.buttonStart.setOnClickListener {
            //            (activity as HomeActivity).navController
            viewModel.tutorialIsDone()
            (activity as HomeActivity).changeFabVisibility(View.VISIBLE)
            viewModel.navigate(R.id.action_tutorialFragment_to_deviceSearchFragment)
        }

    }

}

