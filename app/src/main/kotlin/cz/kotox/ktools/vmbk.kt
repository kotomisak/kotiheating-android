package cz.kotox.ktools

import android.app.Activity
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import cz.kotox.kotiheating.BR
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

// ViewModelBinding extension functions for Fragment and FragmentActivity
// Note: these functions are meant to be used as delegates
// Example: `private val vmb by vmb<MainViewModel, ActivityMainBinding>(R.layout.activity_main)`
// Example with ViewModel constructor: private val vmb by vmb<MainViewModel, ActivityMainBinding>(R.layout.activity_main) { MainViewModel(xxx) }

inline fun <reified VM : ViewModel, B : ViewDataBinding> androidx.fragment.app.FragmentActivity.vmb(@LayoutRes layoutResId: Int, viewModelProvider: ViewModelProvider? = null) = object : ReadOnlyProperty<androidx.fragment.app.FragmentActivity, ViewModelBinding<VM, B>> {
	var instance = ViewModelBinding<VM, B>(this@vmb, VM::class.java, layoutResId, viewModelProvider, null)
	override fun getValue(thisRef: androidx.fragment.app.FragmentActivity, property: KProperty<*>) = instance
}

inline fun <reified VM : ViewModel, B : ViewDataBinding> androidx.fragment.app.FragmentActivity.vmb(@LayoutRes layoutResId: Int, noinline viewModelFactory: () -> VM) = object : ReadOnlyProperty<androidx.fragment.app.FragmentActivity, ViewModelBinding<VM, B>> {
	var instance = ViewModelBinding<VM, B>(this@vmb, VM::class.java, layoutResId, null, viewModelFactory)
	override fun getValue(thisRef: androidx.fragment.app.FragmentActivity, property: KProperty<*>) = instance
}

inline fun <reified VM : ViewModel, B : ViewDataBinding> androidx.fragment.app.Fragment.vmb(@LayoutRes layoutResId: Int, viewModelProvider: ViewModelProvider? = null) = object : ReadOnlyProperty<androidx.fragment.app.Fragment, ViewModelBinding<VM, B>> {
	var instance = ViewModelBinding<VM, B>(this@vmb, VM::class.java, layoutResId, viewModelProvider, null)
	override fun getValue(thisRef: androidx.fragment.app.Fragment, property: KProperty<*>) = instance
}

inline fun <reified VM : ViewModel, B : ViewDataBinding> androidx.fragment.app.Fragment.vmb(@LayoutRes layoutResId: Int, noinline viewModelFactory: () -> VM) = object : ReadOnlyProperty<androidx.fragment.app.Fragment, ViewModelBinding<VM, B>> {
	var instance = ViewModelBinding<VM, B>(this@vmb, VM::class.java, layoutResId, null, viewModelFactory)
	override fun getValue(thisRef: androidx.fragment.app.Fragment, property: KProperty<*>) = instance
}

// -- internal --

/**
 * Main VMB class connecting View (Activity/Fragment) to a Android Architecture ViewModel and Data Binding
 *
 * Note: Do not use this constructor directly. Use extension functions above instead.
 */
class ViewModelBinding<out VM : ViewModel, out B : ViewDataBinding> constructor(
	private val lifecycleOwner: LifecycleOwner,
	private val viewModelClass: Class<VM>,
	@LayoutRes private val layoutResId: Int,
	private var viewModelProvider: ViewModelProvider?,
	val viewModelFactory: (() -> VM)?
) {
	init {
		if (!(lifecycleOwner is androidx.fragment.app.FragmentActivity || lifecycleOwner is androidx.fragment.app.Fragment))
			throw IllegalArgumentException("Provided LifecycleOwner must be one of FragmentActivity or Fragment")
	}

	val binding: B by lazy {
		initializeVmb()
		DataBindingUtil.inflate<B>(activity.layoutInflater, layoutResId, null, false)!!
	}
	val rootView by lazy { binding.root }
	val viewModel: VM by lazy {
		initializeVmb()
		viewModelProvider!!.get(viewModelClass)
	}
	val fragment: androidx.fragment.app.Fragment? = lifecycleOwner as? androidx.fragment.app.Fragment
	val activity: androidx.fragment.app.FragmentActivity by lazy {
		lifecycleOwner as? androidx.fragment.app.FragmentActivity
			?: (lifecycleOwner as androidx.fragment.app.Fragment).activity!!
	}

	private var initialized = false

	init {
		lifecycleOwner.lifecycle.addObserver(object : LifecycleObserver {
			@OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
			fun onCreate() {
				// Note: This line will not work for Android Gradle plugin older than 3.1.0-alpha06 - comment it out if using those
				binding.setLifecycleOwner(lifecycleOwner)
				// setupCached binding variables
				// Note: BR.viewModel, BR.view will be auto-generated if you have those variables somewhere in your layout files
				// If you're not using both of them you will have to comment out one of the lines

				binding.setVariable(BR.viewModel, viewModel)
				binding.setVariable(BR.view, fragment ?: activity)

				if (lifecycleOwner is Activity)
					activity.setContentView(binding.root)
			}
		})
	}

	private fun initializeVmb() {
		if (initialized) return
		if (viewModelFactory != null) {
			val factory = object : ViewModelProvider.Factory {
				@Suppress("UNCHECKED_CAST")
				override fun <T : ViewModel?> create(modelClass: Class<T>) = viewModelFactory.invoke() as T
			}
			if (viewModelProvider == null)
				viewModelProvider = if (fragment != null) ViewModelProviders.of(fragment, factory) else ViewModelProviders.of(activity, factory)
		} else {
			if (viewModelProvider == null)
				viewModelProvider = if (fragment != null) ViewModelProviders.of(fragment) else ViewModelProviders.of(activity)
		}
		initialized = true
	}
}