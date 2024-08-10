package eu.ottop.yamlauncher

import android.app.AlertDialog
import android.content.Context
import android.content.pm.LauncherActivityInfo
import android.content.pm.LauncherApps
import android.os.Bundle
import android.os.UserHandle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class HiddenAppsFragment : Fragment(), HiddenAppsAdapter.OnItemClickListener {

    private lateinit var sharedPreferenceManager: SharedPreferenceManager
    private var adapter: HiddenAppsAdapter? = null
    private var stringUtils = StringUtils()
    private lateinit var uiUtils: UIUtils
    private lateinit var appUtils: AppUtils
    private lateinit var launcherApps: LauncherApps

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_hidden_apps, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        launcherApps = requireContext().getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
        uiUtils = UIUtils(requireContext())
        appUtils = AppUtils(requireContext(), launcherApps)
        sharedPreferenceManager = SharedPreferenceManager(requireContext())

        lifecycleScope.launch {

        val recyclerView = view.findViewById<RecyclerView>(R.id.hiddenAppRecycler)
        val appMenuEdgeFactory = AppMenuEdgeFactory(requireActivity())

        adapter = HiddenAppsAdapter(requireContext(), appUtils.getHiddenApps().toMutableList(), this@HiddenAppsFragment)


        recyclerView.edgeEffectFactory = appMenuEdgeFactory
        recyclerView.adapter = adapter

        recyclerView.scrollToPosition(0)

        val searchView = view.findViewById<TextInputEditText>(R.id.hiddenAppSearch)

        uiUtils.setMenuTitleAlignment(view.findViewById(R.id.hiddenMenuTitle))
        uiUtils.setSearchAlignment(searchView)
        uiUtils.setSearchSize(searchView)

        recyclerView.addOnLayoutChangeListener { _, _, top, _, bottom, _, oldTop, _, oldBottom ->

            if (bottom - top > oldBottom - oldTop) {
                searchView.clearFocus()
            }
        }

        searchView.addTextChangedListener(object :
            TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                lifecycleScope.launch {
                    filterItems(searchView.text.toString())
                }

            }
        })

        if (sharedPreferenceManager.isAutoKeyboardEnabled()) {
            val imm =
                requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            searchView.requestFocus()
            imm.showSoftInput(searchView, InputMethodManager.SHOW_IMPLICIT)
        }
    }
    }

    private suspend fun filterItems(query: String?) {

        val cleanQuery = stringUtils.cleanString(query)
        val newFilteredApps = mutableListOf<Pair<LauncherActivityInfo, Pair<UserHandle, Int>>>()
        val updatedApps = appUtils.getHiddenApps()

        getFilteredApps(cleanQuery, newFilteredApps, updatedApps)

        applySearch(newFilteredApps)

    }

    private fun getFilteredApps(cleanQuery: String?, newFilteredApps: MutableList<Pair<LauncherActivityInfo, Pair<UserHandle, Int>>>, updatedApps: List<Pair<LauncherActivityInfo, Pair<UserHandle, Int>>>) {
        if (cleanQuery.isNullOrEmpty()) {
            newFilteredApps.addAll(updatedApps)
        } else {
            updatedApps.forEach {
                val cleanItemText = stringUtils.cleanString(sharedPreferenceManager.getAppName(
                    it.first.applicationInfo.packageName,
                    it.second.second,
                    requireActivity().packageManager.getApplicationLabel(it.first.applicationInfo)
                ).toString())
                if (cleanItemText != null) {
                    if (cleanItemText.contains(cleanQuery, ignoreCase = true)) {
                        newFilteredApps.add(it)
                    }
                }
            }
        }
    }

    private fun applySearch(newFilteredApps: MutableList<Pair<LauncherActivityInfo, Pair<UserHandle, Int>>>) {
        adapter?.updateApps(newFilteredApps)
    }

    private fun showConfirmationDialog(appInfo: LauncherActivityInfo, appName: String, profile: Int) {
        AlertDialog.Builder(requireContext()).apply {
            setTitle("Confirmation")
            setMessage("Are you sure you want to unhide $appName?")
            setPositiveButton("Yes") { _, _ ->
                lifecycleScope.launch {
                    performConfirmedAction(appInfo, profile)
                }
            }

            setNegativeButton("Cancel") { _, _ ->
            }
        }.create().show()
    }

    private suspend fun performConfirmedAction(appInfo: LauncherActivityInfo, profile: Int) {
        sharedPreferenceManager.setAppVisible(appInfo.applicationInfo.packageName, profile)
        adapter?.updateApps(appUtils.getHiddenApps())
    }

    override fun onItemClick(appInfo: LauncherActivityInfo, profile: Int) {
        showConfirmationDialog(appInfo, sharedPreferenceManager.getAppName(
            appInfo.applicationInfo.packageName,
            profile,
            requireContext().packageManager.getApplicationLabel(appInfo.applicationInfo)
        ).toString(), profile)
    }

}