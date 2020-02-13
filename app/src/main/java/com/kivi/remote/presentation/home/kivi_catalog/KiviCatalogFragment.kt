package com.kivi.remote.presentation.home.kivi_catalog

import android.Manifest
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.view.*
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.ImageViewCompat
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kivi.remote.App
import com.kivi.remote.R
import com.kivi.remote.databinding.KiviCatalogFragmentBinding
import com.kivi.remote.kivi_catalog.model.SortType
import com.kivi.remote.presentation.base.BaseFragment
import com.kivi.remote.presentation.base.BaseViewModelFactory
import com.kivi.remote.presentation.base.recycler.LazyAdapter
import com.kivi.remote.presentation.base.recycler.initWithGridLay
import com.kivi.remote.presentation.base.recycler.initWithLinLay
import com.kivi.remote.presentation.home.HomeActivity
import com.kivi.remote.presentation.home.kivi_catalog.adapters.*
import com.kivi.remote.presentation.home.kivi_catalog.adapters.pagination.UserPagination
import com.kivi.remote.presentation.home.media.MediaFragment.Companion.REQUEST_PERMISSION_CODE
import java.io.Serializable
import java.util.*
import javax.inject.Inject

class KiviCatalogFragment : BaseFragment(), Toolbar.OnMenuItemClickListener, LazyAdapter.OnItemClickListener<MovieData> {

    @Inject
    lateinit var viewModelFactory: BaseViewModelFactory
    private val viewModel: KiviCatalogViewModel by lazy { ViewModelProviders.of(this, viewModelFactory).get(KiviCatalogViewModel::class.java) }

    private lateinit var binding: KiviCatalogFragmentBinding

    //Dialog filters ui items
    private lateinit var dialogFilters: AlertDialog
    private lateinit var dialogFiltersTitle: TextView
    private lateinit var dialogFiltersRecycler: RecyclerView
    private lateinit var dialogFiltersBtnApply: TextView
    private lateinit var dialogFiltersBtnReset: TextView

    //Content
    private val moviesCatalogAdapter = CatalogMoviesAdapter(this)
    private val autocompleteMoviesCatalogAdapter = AutocompleteCatalogMoviesAdapter(this)

    //Filters data
    private val filtersAdapter = FilterCatalogAdapter()
    private var currentSelectedFiltersIndex = -1

    private lateinit var presenterFilterListYears: List<CatalogFilter>
    private lateinit var presenterFilterListCategories: List<CatalogFilter>
    private lateinit var presenterFilterListGenres: List<CatalogFilter>
    private lateinit var presenterFilterListCountries: List<CatalogFilter>

    override fun injectDependencies() = fragmentComponent.inject(this)

    private var speechRecognizerIntent: Intent? = null
    lateinit var speechListener: SpeechRecognizer
    lateinit var searchView: SearchView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = KiviCatalogFragmentBinding.inflate(inflater, container!!, false)
        initViews(inflater)
        return binding.root
    }

    private fun initViews(inflater: LayoutInflater) {
        //DialogView
        initDialogView(inflater)
        setSpeachRecognizer(SpeechRecognizer.createSpeechRecognizer(context))

        //Toolbar
        setHasOptionsMenu(true)
        (activity as HomeActivity).setToolbarTxt(getString(R.string.movies_toolbar_tittle))
        (activity as HomeActivity).toolbar.setOnMenuItemClickListener(this@KiviCatalogFragment)

        binding.rvCatalogMovies.initWithGridLay(2, moviesCatalogAdapter, listOf())
        binding.rvAutocompleteCatalogMovies.initWithLinLay(LinearLayoutManager.VERTICAL, autocompleteMoviesCatalogAdapter, listOf())

        binding.rvCatalogMovies.addOnScrollListener(object : UserPagination(binding.rvCatalogMovies.layoutManager) {
            override fun onLoadMore(currentPage: Int, totalItemCount: Int, view: View?) {
                if (!viewModel.catalogRequestOnWay) {
                    paginationFetchCatalogData(moviesCatalogAdapter.getLastVisibleItemId() + 1)
                }
            }
        })

        paginationFetchCatalogData(0)
    }

    private fun paginationFetchCatalogData(from: Int) {
        binding.progressBar.visibility = View.VISIBLE
        viewModel.paginationFetchCatalogData(context!!, from) { items ->
            if (moviesCatalogAdapter.data.isEmpty() && items.isNullOrEmpty()) {
                binding.llNotFoundPlaceholder.visibility = View.VISIBLE
                return@paginationFetchCatalogData
            } else {
                binding.llNotFoundPlaceholder.visibility = View.GONE
            }

            if (moviesCatalogAdapter.getLastVisibleItemId() == 0) {
                moviesCatalogAdapter.swapData(items)
            } else {
                if (moviesCatalogAdapter.data.firstOrNull()?.equals(items.firstOrNull()) != false) {
                    return@paginationFetchCatalogData
                }

                moviesCatalogAdapter.addData(items.filter { !moviesCatalogAdapter.data.contains(it) })
            }
            binding.progressBar.visibility = View.GONE
        }
    }

    private fun fetchAutocompleteData(query: String) {
        binding.progressBar.visibility = View.VISIBLE
        viewModel.fetchAutocompleteData(context!!, query) { items ->
            binding.llNotFoundPlaceholder.visibility = if (items.isNullOrEmpty()) View.VISIBLE else View.GONE
            autocompleteMoviesCatalogAdapter.swapData(items)
            binding.progressBar.visibility = View.GONE
        }
    }

    override fun onLazyItemClick(data: MovieData) {
        if (data.isSeries) {
            viewModel.navigateToCatalogSeries(data)
        } else {
            viewModel.showContentOnTv(data)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater?) {
        inflater!!.inflate(R.menu.menu_kivi_catalog, menu)

        when (viewModel.catalogSortType) {
            SortType.POP -> menu.findItem(R.id.item_sort_by_popularity).isChecked = true
            SortType.NEW -> menu.findItem(R.id.item_sort_by_appearance_date).isChecked = true
            SortType.IVI -> menu.findItem(R.id.item_sort_by_rating_ivi).isChecked = true
            SortType.KP -> menu.findItem(R.id.item_sort_by_rating_movie_search).isChecked = true
            SortType.IMDB -> menu.findItem(R.id.item_sort_by_rating_imdb).isChecked = true
            SortType.BUDGET -> menu.findItem(R.id.item_sort_by_budget).isChecked = true
            SortType.BOXOFFICE -> menu.findItem(R.id.item_sort_by_worldwide_fees).isChecked = true
            SortType.YEAR -> menu.findItem(R.id.item_sort_by_release_year).isChecked = true
        }

        val searchItem = menu.findItem(R.id.item_search)
        searchView = searchItem.actionView as SearchView
        initSearchView(searchView)
        initSearchItemExpandListener(menu)

        super.onCreateOptionsMenu(menu, inflater)
    }

    private fun initSearchView(searchView: SearchView) {
        searchView.queryHint = getString(R.string.search_movies)

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            private var movieQuery = ""
            private val TYPING_TIMEOUT = 500L
            private val timeoutHandler = Handler()
            private val typingTimeout = Runnable { fetchAutocompleteData(movieQuery) }

            override fun onQueryTextSubmit(query: String): Boolean {
                movieQuery = query
                timeoutHandler.removeCallbacks(typingTimeout)
                fetchAutocompleteData(movieQuery)
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                movieQuery = newText
                timeoutHandler.removeCallbacks(typingTimeout)
                timeoutHandler.postDelayed(typingTimeout, TYPING_TIMEOUT)
                return false
            }
        })

        try {
            val mDrawable = SearchView::class.java.getDeclaredField("mSearchHintIcon")
            mDrawable.isAccessible = true
            val drawable = mDrawable.get(searchView) as Drawable
            drawable.setBounds(0, 0, 0, 0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun initSearchItemExpandListener(menu: Menu) {
        val searchItem = menu.findItem(R.id.item_search)
        searchItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {

            override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                setItemsVisibility(menu, searchItem, false)
                menu.findItem(R.id.item_microphone).isVisible = true
                binding.rvCatalogMovies.visibility = View.GONE
                binding.rvAutocompleteCatalogMovies.visibility = View.VISIBLE
                binding.llNotFoundPlaceholder.visibility = View.GONE
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                setItemsVisibility(menu, searchItem, true)
                menu.findItem(R.id.item_microphone).isVisible = false
                binding.rvCatalogMovies.visibility = View.VISIBLE
                binding.rvAutocompleteCatalogMovies.visibility = View.GONE
                if (moviesCatalogAdapter.data.isEmpty()) {
                    binding.llNotFoundPlaceholder.visibility = View.VISIBLE
                } else {
                    binding.llNotFoundPlaceholder.visibility = View.GONE
                }
                return true
            }
        })

        val microphone = menu.findItem(R.id.item_microphone)

        microphone.actionView?.setOnTouchListener { view, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    if ((activity as HomeActivity).hasRecordAudioPermission()) {
                        startListenIntent()
                        speechRecognizerIntent?.let {
                            speechListener.startListening(it)
                            Toast.makeText(context, "listening ", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        val s = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                        requestPermissions(s, REQUEST_PERMISSION_CODE)
                    }
                }
                MotionEvent.ACTION_UP -> speechListener.stopListening()
            }

            false
        }
    }


    private fun startListenIntent() {
        if (speechRecognizerIntent == null) {
            speechRecognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)

            speechRecognizerIntent?.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            speechRecognizerIntent?.putExtra(RecognizerIntent.EXTRA_LANGUAGE,
                    Locale.getDefault())
        }
    }


    private fun initDialogView(inflater: LayoutInflater) {
        dialogFilters = AlertDialog.Builder(binding.root.context).create()

        val dialogView = inflater.inflate(R.layout.alert_dialog_catalog_filters, null)
        dialogFiltersTitle = dialogView.findViewById(R.id.tv_title)
        dialogFiltersRecycler = dialogView.findViewById(R.id.rv_filters)
        dialogFiltersBtnApply = dialogView.findViewById(R.id.btn_apply_filters)
        dialogFiltersBtnReset = dialogView.findViewById(R.id.btn_reset_filters)

        dialogView.apply {
            findViewById<CardView>(R.id.cv_root).setCardBackgroundColor(ResourcesCompat.getColor(resources, if (App.isDarkMode()) R.color.colorBlack else R.color.colorWhite, null))
            findViewById<RelativeLayout>(R.id.rl_header).setBackgroundColor(ResourcesCompat.getColor(resources, if (App.isDarkMode()) R.color.mine_shaft_4 else R.color.white_87, null))
            dialogFiltersTitle.setTextColor(ResourcesCompat.getColor(resources, if (App.isDarkMode()) R.color.white_87 else R.color.black_87, null))
            ImageViewCompat.setImageTintList(findViewById(R.id.iv_close), ColorStateList.valueOf(ContextCompat.getColor(context, if (App.isDarkMode()) R.color.white_87 else R.color.black_87)))
            dialogFiltersBtnApply.setBackgroundResource(R.drawable.shape_rectangle_blue_r3)
            dialogFiltersBtnReset.setBackgroundResource(if (App.isDarkMode()) R.drawable.shape_rectangle_grey_r3 else R.drawable.shape_rectangle_white_r3)
            dialogFiltersBtnApply.setTextColor(ResourcesCompat.getColor(resources, R.color.white_87, null))
            dialogFiltersBtnReset.setTextColor(ResourcesCompat.getColor(resources, if (App.isDarkMode()) R.color.white_87 else R.color.blue_ribbon, null))
        }

        dialogFiltersRecycler.initWithLinLay(LinearLayoutManager.VERTICAL, filtersAdapter, listOf())

        dialogView.findViewById<ImageView>(R.id.iv_close).setOnClickListener { dialogFilters.cancel() }
        dialogFiltersBtnApply.setOnClickListener { onDialogBtnApplyClick() }
        dialogFiltersBtnReset.setOnClickListener { onDialogBtnResetClick() }

        dialogFilters.setView(dialogView)
        dialogFilters.setCancelable(false)
    }

    private fun onDialogBtnApplyClick() {
        when (currentSelectedFiltersIndex) {
            0 -> viewModel.filterListYears = presenterFilterListYears
            1 -> viewModel.filterListCategories = presenterFilterListCategories
            2 -> viewModel.filterListGenres = presenterFilterListGenres
            3 -> viewModel.filterListCountries = presenterFilterListCountries
        }

        binding.rvCatalogMovies.scrollToPosition(0)
        moviesCatalogAdapter.clearData()
        paginationFetchCatalogData(0)
        dialogFilters.cancel()
    }

    private fun onDialogBtnResetClick() {
        when (currentSelectedFiltersIndex) {
            0 -> {
                presenterFilterListYears.forEach { it.isChecked = false }
                filtersAdapter.swapData(presenterFilterListYears)
            }
            1 -> {
                presenterFilterListCategories.forEach { it.isChecked = false }
                filtersAdapter.swapData(presenterFilterListCategories)
            }
            2 -> {
                presenterFilterListGenres.forEach { it.isChecked = false }
                filtersAdapter.swapData(presenterFilterListGenres)
            }
            3 -> {
                presenterFilterListCountries.forEach { it.isChecked = false }
                filtersAdapter.swapData(presenterFilterListCountries)
            }
        }
    }

    private fun setItemsVisibility(menu: Menu, exception: MenuItem, visible: Boolean) {
        for (i in 0 until menu.size()) {
            val item = menu.getItem(i)
            if (item !== exception)
                item.isVisible = visible
        }
    }

    private fun sortContentBy(itemId: Int) {
        viewModel.catalogSortType = when (itemId) {
            R.id.item_sort_by_popularity -> SortType.POP
            R.id.item_sort_by_appearance_date -> SortType.NEW
            R.id.item_sort_by_rating_ivi -> SortType.IVI
            R.id.item_sort_by_rating_movie_search -> SortType.KP
            R.id.item_sort_by_rating_imdb -> SortType.IMDB
            R.id.item_sort_by_budget -> SortType.BUDGET
            R.id.item_sort_by_worldwide_fees -> SortType.BOXOFFICE
            R.id.item_sort_by_release_year -> SortType.YEAR
            else -> SortType.POP
        }

        binding.rvCatalogMovies.scrollToPosition(0)
        moviesCatalogAdapter.clearData()
        paginationFetchCatalogData(0)
    }

    private fun prepareDialogFiltersList(itemId: Int, itemTitle: String) {
        dialogFiltersTitle.text = itemTitle

        when (itemId) {
            R.id.item_filter_by_year -> {
                currentSelectedFiltersIndex = 0
                presenterFilterListYears = viewModel.filterListYears.map { it.clone() }
                filtersAdapter.swapData(presenterFilterListYears)
            }

            R.id.item_filter_by_category -> {
                currentSelectedFiltersIndex = 1
                presenterFilterListCategories = viewModel.filterListCategories.map { it.clone() }
                filtersAdapter.swapData(presenterFilterListCategories)
            }

            R.id.item_filter_by_genre -> {
                currentSelectedFiltersIndex = 2
                presenterFilterListGenres = viewModel.filterListGenres.map { it.clone() }
                filtersAdapter.swapData(presenterFilterListGenres)
            }

            R.id.item_filter_by_country -> {
                currentSelectedFiltersIndex = 3
                presenterFilterListCountries = viewModel.filterListCountries.map { it.clone() }
                filtersAdapter.swapData(presenterFilterListCountries)
            }
        }
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        when (item.itemId) {
            //Sort
            R.id.item_sort_by_popularity, R.id.item_sort_by_appearance_date, R.id.item_sort_by_rating_ivi,
            R.id.item_sort_by_rating_movie_search, R.id.item_sort_by_rating_imdb, R.id.item_sort_by_budget,
            R.id.item_sort_by_worldwide_fees, R.id.item_sort_by_release_year -> {
                item.isChecked = true
                sortContentBy(item.itemId)
                return true
            }

            // Filter
            R.id.item_filter_by_year, R.id.item_filter_by_category, R.id.item_filter_by_genre, R.id.item_filter_by_country -> {
                prepareDialogFiltersList(item.itemId, item.title.toString())
                dialogFilters.show()
                return true
            }

            else -> return false
        }
    }


    fun setSpeachRecognizer(speechRecognizer: SpeechRecognizer) {
        speechListener = speechRecognizer


        speechListener.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle) {

            }

            override fun onBeginningOfSpeech() {

            }

            override fun onRmsChanged(rmsdB: Float) {

            }

            override fun onBufferReceived(buffer: ByteArray) {

            }

            override fun onEndOfSpeech() {

            }

            override fun onError(error: Int) {

            }

            override fun onResults(results: Bundle) {
                val matches = results
                        .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)

                //displaying the first match
                if (matches != null) {
                    searchView?.setQuery(matches[0], false)
                }
            }

            override fun onPartialResults(partialResults: Bundle) {

            }

            override fun onEvent(eventType: Int, params: Bundle) {

            }
        })
    }


    companion object {
        @JvmStatic
        fun newInstance(value: Serializable): KiviCatalogFragment {
            val fragment = KiviCatalogFragment()
            val args = Bundle()
            args.putSerializable("data", value)
            fragment.arguments = args
            return fragment
        }
    }
}