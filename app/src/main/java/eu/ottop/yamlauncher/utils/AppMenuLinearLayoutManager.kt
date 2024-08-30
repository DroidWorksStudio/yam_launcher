package eu.ottop.yamlauncher.utils

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import eu.ottop.yamlauncher.MainActivity

class AppMenuLinearLayoutManager(private val activity: MainActivity) : LinearLayoutManager(activity) {

    private var firstVisibleItemPosition = 0
    private var scrollStarted = false
    private var isScrollEnabled: Boolean = true

    fun setScrollEnabled(enabled: Boolean) {
        isScrollEnabled = enabled
    }

    override fun canScrollVertically(): Boolean {
        // Control vertical scrolling based on the flag
        return isScrollEnabled && super.canScrollVertically()
    }

    fun setScrollInfo() {
        firstVisibleItemPosition = findFirstCompletelyVisibleItemPosition()
        scrollStarted = true
    }

    override fun scrollVerticallyBy(dy: Int, recycler: RecyclerView.Recycler?, state: RecyclerView.State?): Int {
        val scrollRange = super.scrollVerticallyBy(dy, recycler, state)
        val overscroll: Int = dy - scrollRange

        // If the user scrolls up when already on top, go back to home. Only if the keyboard isn't open, though
        if (overscroll < 0 && (firstVisibleItemPosition == 0 || firstVisibleItemPosition < 0) && scrollStarted) {
            activity.backToHome()
        }

        if (scrollStarted) {
            scrollStarted = false
        }

        return scrollRange
    }

}