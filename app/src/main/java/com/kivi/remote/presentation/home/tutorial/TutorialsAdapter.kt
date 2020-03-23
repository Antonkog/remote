package com.kivi.remote.presentation.home.tutorial

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kivi.remote.R

class TutorialsAdapter : RecyclerView.Adapter<TutorialHolder>() {

    val tutorial  = listOf(
            TutorialPage(R.string.tutorial_title_I, R.string.tutorial_desc_I, R.drawable.tutorial_first ),
            TutorialPage(R.string.tutorial_title_II, R.string.tutorial_desc_II, R.drawable.tutorial_second ),
            TutorialPage(R.string.tutorial_title_III, R.string.tutorial_desc_III, R.drawable.tutorial_lll ),
            TutorialPage(R.string.tutorial_title_IV, R.string.tutorial_desc_IV, R.drawable.tutorial_iv ),
            TutorialPage(R.string.tutorial_title_V, R.string.tutorial_desc_V, R.drawable.tutotial_v )

    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TutorialHolder {
        return TutorialHolder(TutorialView(LayoutInflater.from(parent.context), parent))
    }

    override fun onBindViewHolder(holder: TutorialHolder, position: Int) {
        holder.bind(tutorial[position])
    }

    override fun getItemCount(): Int {
        return tutorial.size
    }
}

class TutorialHolder internal constructor(private val tutorialView: TutorialView) :
    RecyclerView.ViewHolder(tutorialView.view) {
    internal fun bind(card: TutorialPage) {
        tutorialView.bind(card)
    }
}