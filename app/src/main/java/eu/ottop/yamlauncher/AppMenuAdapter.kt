package eu.ottop.yamlauncher

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.LauncherActivityInfo
import android.content.pm.LauncherApps
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.os.UserHandle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText


class AppMenuAdapter(

    private val context: Context,
    private var apps: MutableList<Pair<LauncherActivityInfo, Pair<UserHandle, Int>>>,
    private val itemClickListener: OnItemClickListener,
    private val shortcutListener: OnShortcutListener,
    private val itemLongClickListener: OnItemLongClickListener,
    launcherApps: LauncherApps
) :
    RecyclerView.Adapter<AppMenuAdapter.AppViewHolder>() {

        var shortcutTextView: TextView? = null

        private val sharedPreferenceManager = SharedPreferenceManager(context)
        private val uiUtils = UIUtils(context)
        private val appUtils = AppUtils(context, launcherApps)

    interface OnItemClickListener {
        fun onItemClick(appInfo: LauncherActivityInfo, userHandle: UserHandle)
    }

    interface OnShortcutListener {
        fun onShortcut(appInfo: LauncherActivityInfo, userHandle: UserHandle, textView: TextView, userProfile: Int, shortcutView: TextView)
    }

    interface OnItemLongClickListener {
        fun onItemLongClick(
            appInfo: LauncherActivityInfo,
            userHandle: UserHandle,
            userProfile: Int,
            textView: TextView,
            actionMenuLayout: LinearLayout,
            editView: LinearLayout,
            position: Int,
            shortcutTextView: TextView?
        )
    }

    inner class AppViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val listItem: FrameLayout = itemView.findViewById(R.id.listItem)
        val textView: TextView = listItem.findViewById(R.id.appName)
        val actionMenuLayout: LinearLayout = listItem.findViewById(R.id.actionMenu)
        private val editView: LinearLayout = listItem.findViewById(R.id.renameView)
        val editText: TextInputEditText = editView.findViewById(R.id.appNameEdit)

        init {
            actionMenuLayout.visibility = View.INVISIBLE
            editView.visibility = View.INVISIBLE

            textView.setOnClickListener {
                    val position = bindingAdapterPosition
                    val app = apps[position].first
                    if (shortcutTextView != null) {
                        shortcutListener.onShortcut(app, apps[position].second.first, textView, apps[position].second.second, shortcutTextView!!)
                    }
                    else {
                        itemClickListener.onItemClick(app, apps[position].second.first)
                    }
            }


            textView.setOnLongClickListener {
                val position = bindingAdapterPosition

                val app = apps[position].first
                itemLongClickListener.onItemLongClick(
                    app,
                    apps[position].second.first,
                    apps[position].second.second,
                    textView,
                    actionMenuLayout,
                    editView,
                    position,
                    shortcutTextView
                )
                return@setOnLongClickListener true
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.app_item_layout, parent, false)
        return AppViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        val app = apps[position]

        if (app.second.second != 0) {
            holder.textView.setCompoundDrawablesWithIntrinsicBounds(ResourcesCompat.getDrawable(context.resources, R.drawable.ic_work_app, null),null, ResourcesCompat.getDrawable(context.resources, R.drawable.ic_empty, null),null)
            holder.textView.compoundDrawables[0].colorFilter =
                BlendModeColorFilter(sharedPreferenceManager.getTextColor(), BlendMode.SRC_ATOP)
        }
        else {
            holder.textView.setCompoundDrawablesWithIntrinsicBounds(ResourcesCompat.getDrawable(context.resources, R.drawable.ic_empty, null),null,ResourcesCompat.getDrawable(context.resources, R.drawable.ic_empty, null),null)
        }

        uiUtils.setAppAlignment(holder.textView, holder.editText)

        uiUtils.setAppSize(holder.textView, holder.editText)

        val appInfo = appUtils.getAppInfo(
            app.first.applicationInfo.packageName,
            app.second.second
        )

        holder.textView.setTextColor(sharedPreferenceManager.getTextColor())
        val appLabel: CharSequence = appInfo?.loadLabel(context.packageManager) ?: "Removing..."

        if (appInfo != null) {
            holder.textView.text = sharedPreferenceManager.getAppName(
                appInfo.packageName,
                app.second.second,
                appLabel
            )

            holder.editText.setText(holder.textView.text)

            if (appInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0) {
                holder.actionMenuLayout.findViewById<TextView>(R.id.uninstall).visibility =
                    View.GONE
            } else {
                holder.actionMenuLayout.findViewById<TextView>(R.id.uninstall).visibility =
                    View.VISIBLE
            }
        }
        else {holder.textView.text = appLabel}

        holder.textView.visibility = View.VISIBLE
    }

    override fun getItemCount(): Int {
        return apps.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateApps(newApps: List<Pair<LauncherActivityInfo, Pair<UserHandle, Int>>>) {
        apps = newApps.toMutableList()
        notifyDataSetChanged()
    }
}