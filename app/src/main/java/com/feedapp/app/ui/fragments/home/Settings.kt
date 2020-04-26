/* * Copyright (c) 2020 Ruslan Potekhin */package com.feedapp.app.ui.fragments.homeimport android.annotation.SuppressLintimport android.app.Activityimport android.content.Intentimport android.os.Bundleimport android.view.LayoutInflaterimport android.view.Viewimport android.view.ViewGroupimport android.widget.CheckBoximport android.widget.EditTextimport android.widget.RadioGroupimport android.widget.RelativeLayoutimport androidx.appcompat.app.AlertDialogimport androidx.core.view.childrenimport androidx.lifecycle.Observerimport androidx.lifecycle.ViewModelProviderimport com.feedapp.app.Rimport com.feedapp.app.data.models.BasicNutrientTypeimport com.feedapp.app.data.models.DataResponseStatusimport com.feedapp.app.data.models.MeasureTypeimport com.feedapp.app.data.models.user.UserDeleteOperationimport com.feedapp.app.util.isConnectedimport com.feedapp.app.util.toastimport com.feedapp.app.util.toastLongimport com.feedapp.app.viewModels.SettingsViewModelimport com.firebase.ui.auth.AuthUIimport dagger.android.support.DaggerFragmentimport kotlinx.android.synthetic.main.fragment_settings.*import javax.inject.Injectclass Settings : DaggerFragment() {    private val RC_SIGN_IN = 103    @Inject    lateinit var modelFactory: ViewModelProvider.Factory    private lateinit var viewModel: SettingsViewModel    override fun onCreateView(        inflater: LayoutInflater, container: ViewGroup?,        savedInstanceState: Bundle?    ): View? {        // Inflate the layout for this fragment        return inflater.inflate(R.layout.fragment_settings, container, false)    }    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {        super.onViewCreated(view, savedInstanceState)        viewModel = activity?.run {            ViewModelProvider(this, modelFactory).get(SettingsViewModel::class.java)        } ?: throw Exception("Invalid Activity")        setViewListeners()        setObservers()    }    private fun setObservers() {        viewModel.toast.observe(viewLifecycleOwner, Observer { event ->            event.getContentIfNotHandled()                ?.let {                    when (it) {                        UserDeleteOperation.SUCCESS -> {                            activity?.toast(getString(R.string.toast_acc_deleted))                        }                        UserDeleteOperation.FAILED -> {                            activity?.toastLong(getString(R.string.error_dialog_deletion))                        }                        UserDeleteOperation.REAUTH -> {                            activity?.toast(getString(R.string.dialog_sign_in_again))                        }                    }                }        })        viewModel.reauth.observe(viewLifecycleOwner, Observer { event ->            // if requires re-authentication, open auth activity            if (event.peekContent()) {                val providers = arrayListOf(                    AuthUI.IdpConfig.GoogleBuilder().build(),                    AuthUI.IdpConfig.FacebookBuilder().build(),                    AuthUI.IdpConfig.EmailBuilder().build()                )                // Create and launch sign-in intent                startActivityForResult(                    AuthUI.getInstance()                        .createSignInIntentBuilder()                        .setIsSmartLockEnabled(false, false)                        .setAvailableProviders(providers)                        .setLogo(R.drawable.icon)                        .build(),                    RC_SIGN_IN                )            }        })        viewModel.status.observe(viewLifecycleOwner, Observer {            when (it) {                DataResponseStatus.SUCCESS -> {                    toastSuccess()                    viewModel.status.postValue(DataResponseStatus.NONE)                }                DataResponseStatus.FAILED -> {                    toastFail()                    viewModel.status.postValue(DataResponseStatus.NONE)                }                else -> {                }            }        })    }    private fun setViewListeners() {        calories.setOnClickListener { showAmountDialog(BasicNutrientType.CALORIES) }        proteins.setOnClickListener { showAmountDialog(BasicNutrientType.PROTEINS) }        carbs.setOnClickListener { showAmountDialog(BasicNutrientType.CARBS) }        fats.setOnClickListener { showAmountDialog(BasicNutrientType.FATS) }        measure.setOnClickListener { showMeasureDialog() }        intolerance.setOnClickListener { if (viewModel.shouldShowCautionDialog()) showCautionDialog() else showIntoleranceDialog() }        diet.setOnClickListener { if (viewModel.shouldShowCautionDialog()) showCautionDialog() else showDietDialog() }        delete.setOnClickListener { showDeleteDialog() }        viewModel.context = requireContext()    }    private fun showDeleteDialog() {        activity?.let {            AlertDialog.Builder(requireActivity())                .setTitle(getString(R.string.delete_all))                .setMessage(getString(R.string.dialog_delete_data))                .setPositiveButton(getString(R.string.delete)) { _, _ ->                    if (!requireContext().isConnected()) it.toast("Check Internet connection")                    else viewModel.deleteAllData()                }                .setNegativeButton(R.string.cancel, null)                .show()        }    }    private fun showCautionDialog() {        activity?.let {            AlertDialog.Builder(requireActivity())                .setTitle(getString(R.string.dialog_caution_diet_title))                .setMessage(getString(R.string.dialog_caution_diet_message))                .setPositiveButton(getString(R.string.ok))                { _, _ -> viewModel.saveShowedCautionDialog() }                .setNegativeButton(R.string.cancel, null)                .show()        }    }    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {        super.onActivityResult(requestCode, resultCode, data)        when (requestCode) {            RC_SIGN_IN -> {                if (resultCode == Activity.RESULT_OK) {                    viewModel.deleteAllData()                } else {                    toastFail()                }            }        }    }    private fun showIntoleranceDialog() {        try {            val intoleranceArray = resources.getStringArray(R.array.Intolerance)            viewModel.user.value?.let { user ->                val dialogView =                    View.inflate(requireContext(), R.layout.preference_intolerance_dialog, null)                val container =                    dialogView.findViewById<RelativeLayout>(R.id.pref_intolerance_container)                // display initial values                val userIntolerance = user.intolerance ?: listOf()                container.children.forEach { view ->                    if (view is CheckBox && userIntolerance.find { it == view.text }                            ?.isNotEmpty() == true)                        view.isChecked = true                }                activity?.let {                    AlertDialog.Builder(requireActivity())                        .setTitle(getString(R.string.intolerance))                        .setPositiveButton(getString(R.string.ok)) { _, _ ->                            val intolerance = arrayListOf<String>()                            // get checked checkboxes and put it's correlating                            // intolerance value to array                            container.children.forEachIndexed { index, view ->                                if (view is CheckBox && view.isChecked) intolerance.add(                                    intoleranceArray[index]                                )                            }                            // update new intolerance                            viewModel.saveIntolerance(intolerance)                        }                        .setNegativeButton(R.string.cancel, null)                        .setView(dialogView)                        .show()                }            }        } catch (e: java.lang.Exception) {            e.printStackTrace()            toastFail()        }    }    private fun showDietDialog() {        try {            val dietArray = resources.getStringArray(R.array.Diet)            viewModel.user.value?.let { user ->                val dialogView =                    View.inflate(requireContext(), R.layout.preference_diet_dialog, null)                val container =                    dialogView.findViewById<RelativeLayout>(R.id.pref_diet_container)                // display initial values                val userDiet = user.diet ?: listOf()                container.children.forEach { view ->                    if (view is CheckBox && userDiet.find { it == view.text }                            ?.isNotEmpty() == true)                        view.isChecked = true                }                activity?.let {                    AlertDialog.Builder(requireActivity())                        .setTitle(getString(R.string.intolerance))                        .setPositiveButton(getString(R.string.ok)) { _, _ ->                            val intolerance = arrayListOf<String>()                            // get checked checkboxes and put it's correlating                            // diet value to array                            container.children.forEachIndexed { index, view ->                                if (view is CheckBox && view.isChecked) intolerance.add(                                    dietArray[index]                                )                            }                            // update new intolerance                            viewModel.saveDiet(intolerance)                        }                        .setNegativeButton(R.string.cancel, null)                        .setView(dialogView)                        .show()                }            }        } catch (e: java.lang.Exception) {            e.printStackTrace()            toastFail()        }    }    private fun showMeasureDialog() {        viewModel.user.value?.let {            val dialogView =                View.inflate(requireContext(), R.layout.preference_measure_dialog, null)            val rGroup = dialogView.findViewById<RadioGroup>(R.id.pref_radio_group)            // display relevant measure value            if (it.measureType == MeasureType.METRIC)                rGroup.check(R.id.pref_radio_metric)            else rGroup.check(R.id.pref_radio_us)            activity?.let {                AlertDialog.Builder(requireActivity())                    .setTitle(getString(R.string.measure_system))                    .setPositiveButton(getString(R.string.ok)) { _, _ ->                        // save new value                        viewModel.saveMeasure(rGroup.checkedRadioButtonId == R.id.pref_radio_metric)                    }                    .setNegativeButton(R.string.cancel, null)                    .setView(dialogView)                    .show()            }        }    }    @SuppressLint("DefaultLocale")    private fun showAmountDialog(nutrient: BasicNutrientType) {        val dialogView = View.inflate(requireContext(), R.layout.preference_amount_dialog, null)        val edtText = dialogView.findViewById<EditText>(R.id.edt_value)        edtText.setText(viewModel.getNutrientValue(nutrient))        activity?.let {            AlertDialog.Builder(requireActivity())                .setTitle(nutrient.toString().toLowerCase().capitalize())                .setPositiveButton(getString(R.string.ok)) { _, _ ->                    viewModel.saveNewValue(edtText.text.toString(), nutrient)                }                .setNegativeButton(R.string.cancel, null)                .setView(dialogView)                .show()        }    }    private fun toastSuccess() {        activity?.toast("Saved successfully!")    }    private fun toastFail() {        activity?.toast("Failed to save")    }}