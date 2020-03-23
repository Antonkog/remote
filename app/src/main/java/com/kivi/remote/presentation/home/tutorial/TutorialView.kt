

package com.kivi.remote.presentation.home.tutorial

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.kivi.remote.R

class TutorialView(layoutInflater: LayoutInflater, container: ViewGroup?) {
    val view: View = layoutInflater.inflate(R.layout.tutorial_item, container, false)


    private val textDescription: TextView
    private val textTitle: TextView
    private val imgTutorial: ImageView

    init {
        textDescription = view.findViewById(R.id.text_description)
        textTitle = view.findViewById(R.id.text_title)
        imgTutorial = view.findViewById(R.id.tutorial_img)
    }

    /**
     * Updates the view to represent the passed in card
     */
    fun bind(tutorial: TutorialPage) {

        textTitle.text = textTitle.context.getString(tutorial.title)
        textDescription.text = textDescription.context.getString(tutorial.descrioption)
        imgTutorial.setImageResource(tutorial.imgId)
    }
}