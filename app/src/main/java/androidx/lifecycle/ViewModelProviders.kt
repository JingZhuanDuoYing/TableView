//package androidx.lifecycle
//
//import android.app.Application
//import androidx.annotation.MainThread
//import androidx.fragment.app.Fragment
//import androidx.fragment.app.FragmentActivity
//
//object ViewModelProviders {
//    /**
//     * Creates a [ViewModelProvider], which retains ViewModels while a scope of given
//     * `fragment` is alive. More detailed explanation is in [ViewModel].
//     *
//     *
//     * It uses the [default factory][Fragment.getDefaultViewModelProviderFactory]
//     * to instantiate new ViewModels.
//     *
//     * @param fragment a fragment, in whose scope ViewModels should be retained
//     * @return a ViewModelProvider instance
//     */
//    @MainThread
//    @Deprecated(
//        """Use the 'by viewModels()' Kotlin property delegate or
//      {@link ViewModelProvider#ViewModelProvider(ViewModelStoreOwner)},
//      passing in the fragment."""
//    )
//    fun of(fragment: Fragment): ViewModelProvider {
//        return ViewModelProvider(fragment)
//    }
//
//    /**
//     * Creates a [ViewModelProvider], which retains ViewModels while a scope of given Activity
//     * is alive. More detailed explanation is in [ViewModel].
//     *
//     *
//     * It uses the [default factory][FragmentActivity.getDefaultViewModelProviderFactory]
//     * to instantiate new ViewModels.
//     *
//     * @param activity an activity, in whose scope ViewModels should be retained
//     * @return a ViewModelProvider instance
//     */
//    @MainThread
//    @Deprecated(
//        """Use the 'by viewModels()' Kotlin property delegate or
//      {@link ViewModelProvider#ViewModelProvider(ViewModelStoreOwner)},
//      passing in the activity."""
//    )
//    fun of(activity: FragmentActivity): ViewModelProvider {
//        return ViewModelProvider(activity)
//    }
//
//    /**
//     * Creates a [ViewModelProvider], which retains ViewModels while a scope of given
//     * `fragment` is alive. More detailed explanation is in [ViewModel].
//     *
//     *
//     * It uses the given [Factory] to instantiate new ViewModels.
//     *
//     * @param fragment a fragment, in whose scope ViewModels should be retained
//     * @param factory  a `Factory` to instantiate new ViewModels
//     * @return a ViewModelProvider instance
//     */
//    @MainThread
//    @Deprecated(
//        """Use the 'by viewModels()' Kotlin property delegate or
//      {@link ViewModelProvider#ViewModelProvider(ViewModelStoreOwner, Factory)},
//      passing in the fragment and factory."""
//    )
//    fun of(
//        fragment: Fragment,
//        factory: ViewModelProvider.Factory?
//    ): ViewModelProvider {
//        var factory = factory
//        if (factory == null) {
//            factory = fragment.defaultViewModelProviderFactory
//        }
//        return ViewModelProvider(fragment.viewModelStore, factory)
//    }
//
//    /**
//     * Creates a [ViewModelProvider], which retains ViewModels while a scope of given Activity
//     * is alive. More detailed explanation is in [ViewModel].
//     *
//     *
//     * It uses the given [Factory] to instantiate new ViewModels.
//     *
//     * @param activity an activity, in whose scope ViewModels should be retained
//     * @param factory  a `Factory` to instantiate new ViewModels
//     * @return a ViewModelProvider instance
//     */
//    @MainThread
//    @Deprecated(
//        """Use the 'by viewModels()' Kotlin property delegate or
//      {@link ViewModelProvider#ViewModelProvider(ViewModelStoreOwner, Factory)},
//      passing in the activity and factory."""
//    )
//    fun of(
//        activity: FragmentActivity,
//        factory: ViewModelProvider.Factory?
//    ): ViewModelProvider {
//        var factory = factory
//        if (factory == null) {
//            factory = activity.defaultViewModelProviderFactory
//        }
//        return ViewModelProvider(activity.viewModelStore, factory)
//    }
//
//    /**
//     * [Factory] which may create [AndroidViewModel] and
//     * [ViewModel], which have an empty constructor.
//     *
//     */
//    @Deprecated("Use {@link ViewModelProvider.AndroidViewModelFactory}")
//    class DefaultFactory
//    /**
//     * Creates a `AndroidViewModelFactory`
//     *
//     * @param application an application to pass in [AndroidViewModel]
//     */
//    @Deprecated(
//        """Use {@link ViewModelProvider.AndroidViewModelFactory} or
//          {@link ViewModelProvider.AndroidViewModelFactory#getInstance(Application)}."""
//    ) constructor(
//        application: Application
//    ) :
//        ViewModelProvider.AndroidViewModelFactory(application)
//}
