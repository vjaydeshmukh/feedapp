/* * Copyright (c) 2020 Ruslan Potekhin */package com.feedapp.app.ui.viewclassesimport android.view.LayoutInflaterimport android.view.Viewimport android.view.ViewGroupimport android.widget.TextViewimport androidx.recyclerview.widget.RecyclerViewimport com.feedapp.app.Rimport com.feedapp.app.ui.viewholders.SearchBarViewHolderimport com.mancj.materialsearchbar.adapter.SuggestionsAdapterinterface SearchByQuery {    fun searchByQuery(q: String)}class SearchSuggestionAdapter(    layoutInflater: LayoutInflater,    private val searchByQuery: SearchByQuery,    val viewHeight: Int) :    SuggestionsAdapter<String, RecyclerView.ViewHolder>(layoutInflater) {    override fun onCreateViewHolder(        parent: ViewGroup,        viewType: Int    ): RecyclerView.ViewHolder {        val holder = LayoutInflater.from(parent.context)            .inflate(R.layout.vh_search_bar, parent, false)        return SearchBarViewHolder(holder)    }    override fun getSingleViewHeight(): Int {        return viewHeight    }    override fun onBindSuggestionHolder(        p0: String?,        p1: RecyclerView.ViewHolder?,        p2: Int    ) {        p1?.itemView?.findViewById<TextView>(R.id.vh_search_txt)?.text = p0        p1?.itemView?.findViewById<View>(R.id.vh_search_container)?.setOnClickListener {            p0?.let { searchByQuery.searchByQuery(it) }        }    }}