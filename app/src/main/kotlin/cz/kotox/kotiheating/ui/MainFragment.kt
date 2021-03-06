package cz.kotox.kotiheating.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.databinding.ObservableInt
import androidx.lifecycle.ViewModelProviders
import cz.kotox.kotiheating.R
import cz.kotox.kotiheating.databinding.FragmentMainBinding
import cz.kotox.ktools.vmb

class MainFragment : androidx.fragment.app.Fragment(), MainFragmentView {

	companion object {
		fun newInstance(day: Int) = MainFragment().apply { arguments = Bundle().apply { putInt("day", day) } }
	}

	private val vmb by vmb<MainViewModel, FragmentMainBinding>(R.layout.fragment_main) { ViewModelProviders.of(activity!!).get(MainViewModel::class.java) }

	val fragmentDay = ObservableInt(-1)

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		arguments?.getInt("day")?.let {
			fragmentDay.set(it)
		}
		if (fragmentDay.get() < 0) throw IllegalStateException("MainFragment cannot be initialized without day!")

		vmb.binding.circleProgress.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
			override fun onGlobalLayout() {
				if (vmb.binding.circleProgress.showLayout()) {
					vmb.binding.circleProgress.viewTreeObserver.removeOnGlobalLayoutListener(this)
				}
			}
		})
	}

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) = vmb.rootView


	override fun onResume() {
		super.onResume()
		onReloadStatusView()
	}

	override fun onReloadStatusView() {
		vmb.viewModel.refreshDataFromServer()
		vmb.binding.circleProgress.showLayout()
	}

}

interface MainFragmentView {
	fun onReloadStatusView()
}