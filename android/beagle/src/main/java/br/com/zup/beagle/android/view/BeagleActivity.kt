/*
 * Copyright 2020 ZUP IT SERVICOS EM TECNOLOGIA E INOVACAO SA
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package br.com.zup.beagle.android.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.view.Window
import android.view.WindowManager
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import br.com.zup.beagle.R
import br.com.zup.beagle.android.components.layout.Screen
import br.com.zup.beagle.android.data.serializer.BeagleSerializer
import br.com.zup.beagle.android.networking.RequestData
import br.com.zup.beagle.android.setup.BeagleEnvironment
import br.com.zup.beagle.android.utils.BeagleRetry
import br.com.zup.beagle.core.ServerDrivenComponent
import br.com.zup.beagle.android.utils.toComponent
import br.com.zup.beagle.android.view.mapper.toRequestData
import br.com.zup.beagle.android.view.viewmodel.BeagleScreenViewModel
import br.com.zup.beagle.android.view.viewmodel.ViewState
import kotlinx.android.parcel.Parcelize
import java.net.URI

sealed class ServerDrivenState {

    class FormError(throwable: Throwable, retry: BeagleRetry) : Error(throwable, retry)
    class WebViewError(throwable: Throwable, retry: BeagleRetry) : Error(throwable, retry)

    @Deprecated("It was deprecated in version 1.2.0 and will be removed in a future version." +
        " Use Started and Finished instead.")
    data class Loading(val loading: Boolean) : ServerDrivenState()

    /**
     * indicates that a server-driven component fetch has begun
     */
    object Started : ServerDrivenState()

    /**
     * indicates that a server-driven component fetch has finished
     */
    object Finished : ServerDrivenState()

    /**
     * indicates a success state while fetching a server-driven component
     */
    object Success : ServerDrivenState()

    /**
     * indicates that a server-driven component fetch has cancel
     */
    object Canceled : ServerDrivenState()

    /**
     * indicates an error state while fetching a server-driven component
     *
     * @param throwable error occurred. See {@link br.com.zup.beagle.android.exception.BeagleApiException},
     * See {@link br.com.zup.beagle.android.exception.BeagleException}
     * @param retry action to be performed when an error occurs
     */
    open class Error(val throwable: Throwable, val retry: BeagleRetry) : ServerDrivenState()
}

/**
 * ScreenRequest is used to do requests.
 *
 * @param url Server URL.
 * @param method HTTP method.
 * @param headers Header items for the request.
 * @param body Content that will be delivered with the request.
 */
@Parcelize
@Deprecated(
    message = "It was deprecated in version 1.7.0 and will be removed in a future version. " +
        "Use class RequestData.", replaceWith = ReplaceWith("RequestData()")
)
data class ScreenRequest(
    val url: String,
    val method: ScreenMethod = ScreenMethod.GET,
    val headers: Map<String, String> = mapOf(),
    val body: String? = null,
) : Parcelable

/**
 * Screen method to indicate the desired action to be performed for a given resource.
 *
 */

@Deprecated(
    message = "It was deprecated in version 1.7.0 and will be removed in a future version. " +
        "Use field HttpMethod.")
enum class ScreenMethod {
    /**
     * The GET method requests a representation of the specified resource. Requests using GET should only retrieve
     * data.
     */
    GET,

    /**
     * The POST method is used to submit an entity to the specified resource, often causing
     * a change in state or side effects on the server.
     */
    POST,

    /**
     * The PUT method replaces all current representations of the target resource with the request payload.
     */
    PUT,

    /**
     * The DELETE method deletes the specified resource.
     */
    DELETE,

    /**
     * The HEAD method asks for a response identical to that of a GET request, but without the response body.
     */
    HEAD,

    /**
     * The PATCH method is used to apply partial modifications to a resource.
     */
    PATCH
}

private val beagleSerializer: BeagleSerializer = BeagleSerializer()
private const val FIRST_SCREEN_REQUEST_KEY = "FIRST_SCREEN_REQUEST_KEY"
private const val FIRST_SCREEN_KEY = "FIRST_SCREEN_KEY"

abstract class BeagleActivity : AppCompatActivity() {

    private val screenViewModel by lazy { ViewModelProvider(this).get(BeagleScreenViewModel::class.java) }
    private val screenRequest by lazy { intent.extras?.getParcelable<RequestData>(FIRST_SCREEN_REQUEST_KEY) }
    private val screen by lazy { intent.extras?.getString(FIRST_SCREEN_KEY) }

    companion object {
        @Deprecated(
            message = "It was deprecated in version 1.2.0 and will be removed in a future version." +
                " To create a intent of your sub-class of BeagleActivity use Context.newServerDrivenIntent instead.",
            replaceWith = ReplaceWith(
                "context.newServerDrivenIntent<YourBeagleActivity>(screenJson)",
                imports = ["br.com.zup.beagle.android.utils.newServerDrivenIntent"]
            )
        )
        fun newIntent(context: Context, screenJson: String): Intent {
            return newIntent(context).apply {
                putExtra(FIRST_SCREEN_KEY, screenJson)
            }
        }

        @Deprecated(
            message = "It was deprecated in version 1.2.0 and will be removed in a future version." +
                " To create a intent of your sub-class of BeagleActivity use Context.newServerDrivenIntent instead.",
            replaceWith = ReplaceWith(
                "context.newServerDrivenIntent<YourBeagleActivity>(screen)",
                imports = ["br.com.zup.beagle.android.utils.newServerDrivenIntent"]
            )
        )
        fun newIntent(context: Context, screen: Screen): Intent {
            return newIntent(context, null, screen)
        }

        @Deprecated(
            message = "It was deprecated in version 1.2.0 and will be removed in a future version." +
                " To create a intent of your sub-class of BeagleActivity use Context.newServerDrivenIntent instead.",
            replaceWith = ReplaceWith(
                "context.newServerDrivenIntent<YourBeagleActivity>(screenRequest)",
                imports = ["br.com.zup.beagle.android.utils.newServerDrivenIntent"]
            )
        )
        fun newIntent(context: Context, screenRequest: ScreenRequest): Intent {
            return newIntent(context, screenRequest.toRequestData(), null)
        }

        internal fun newIntent(
            context: Context,
            screenRequest: RequestData? = null,
            screen: Screen? = null,
        ): Intent {
            return newIntent(context).apply {
                screenRequest?.let {
                    putExtra(FIRST_SCREEN_REQUEST_KEY, screenRequest)
                }
                screen?.let {
                    putExtra(FIRST_SCREEN_KEY, beagleSerializer.serializeComponent(screen.toComponent()))
                }
            }
        }

        private fun newIntent(context: Context): Intent {
            val activityClass = BeagleEnvironment.beagleSdk.serverDrivenActivity
            return Intent(context, activityClass)
        }

        @Deprecated(
            message = "It was deprecated in version 1.7.0 and will be removed in a future version." +
                " To create a intent of your sub-class of BeagleActivity use Context.newServerDrivenIntent instead.",
            replaceWith = ReplaceWith(
                "bundleOf(requestData)"
            )
        )
        fun bundleOf(screenRequest: ScreenRequest): Bundle {
            return Bundle(1).apply {
                putParcelable(FIRST_SCREEN_REQUEST_KEY, screenRequest.toRequestData())
            }
        }

        fun bundleOf(requestData: RequestData): Bundle {
            return Bundle(1).apply {
                putParcelable(FIRST_SCREEN_REQUEST_KEY, requestData)
            }
        }

        @Deprecated(
            message = "It was deprecated in version 1.7.0 and will be removed in a future version." +
                " To create a intent of your sub-class of BeagleActivity use Context.newServerDrivenIntent instead.",
            replaceWith = ReplaceWith(
                "bundleOf(requestData, fallbackScreen)"
            )
        )
        fun bundleOf(screenRequest: ScreenRequest, fallbackScreen: Screen): Bundle {
            return Bundle(2).apply {
                putParcelable(FIRST_SCREEN_REQUEST_KEY, screenRequest.toRequestData())
                putAll(bundleOf(fallbackScreen))
            }
        }

        fun bundleOf(requestData: RequestData, fallbackScreen: Screen): Bundle {
            return Bundle(2).apply {
                putParcelable(FIRST_SCREEN_REQUEST_KEY, requestData)
                putAll(bundleOf(fallbackScreen))
            }
        }

        fun bundleOf(screen: Screen): Bundle {
            return Bundle(1).apply {
                putString(FIRST_SCREEN_KEY, beagleSerializer.serializeComponent(screen.toComponent()))
            }
        }

        fun bundleOf(screenJson: String): Bundle {
            return Bundle(1).apply {
                putString(FIRST_SCREEN_KEY, screenJson)
            }
        }
    }

    abstract fun getToolbar(): Toolbar

    @IdRes
    abstract fun getServerDrivenContainerId(): Int

    abstract fun onServerDrivenContainerStateChanged(state: ServerDrivenState)

    open fun getFragmentTransitionAnimation() = FragmentTransitionAnimation(
        R.anim.slide_from_right,
        R.anim.none_animation,
        R.anim.none_animation,
        R.anim.slide_to_right
    )

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.KITKAT) {
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        }

        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        observeScreenLoadFinish()
    }

    private fun observeScreenLoadFinish() {
        screenViewModel.screenLoadFinished.observe(
            this,
            Observer {
                onServerDrivenContainerStateChanged(ServerDrivenState.Success)
                onServerDrivenContainerStateChanged(ServerDrivenState.Finished)
            }
        )
    }

    override fun onResume() {
        super.onResume()

        if (supportFragmentManager.fragments.size == 0) {
            screen?.let { screen ->
                fetch(
                    RequestData(uri = URI.create("")),
                    beagleSerializer.deserializeComponent(screen)
                )
            } ?: run {
                screenRequest?.let { request -> fetch(request) }
            }
        }
    }

    override fun onBackPressed() {
        if (screenViewModel.isFetchComponent()) {
            if (supportFragmentManager.backStackEntryCount == 0) {
                finish()
            }
        } else if (supportFragmentManager.backStackEntryCount == 1) {
            finish()
        } else {
            super.onBackPressed()
        }
    }

    fun hasServerDrivenScreen(): Boolean = supportFragmentManager.backStackEntryCount > 0

    internal fun navigateTo(requestData: RequestData, screen: Screen?) {
        fetch(requestData, screen?.toComponent())
    }

    private fun fetch(requestData: RequestData, screenComponent: ServerDrivenComponent? = null) {
        val liveData = screenViewModel.fetchComponent(requestData, screenComponent)
        handleLiveData(liveData)
    }

    private fun handleLiveData(state: LiveData<ViewState>) {
        state.observe(this, {
            when (it) {
                is ViewState.Error -> {
                    onServerDrivenContainerStateChanged(ServerDrivenState.Error(it.throwable, it.retry))
                    onServerDrivenContainerStateChanged(ServerDrivenState.Finished)
                }

                is ViewState.Loading -> {
                    onServerDrivenContainerStateChanged(ServerDrivenState.Loading(it.value))

                    if (it.value) {
                        onServerDrivenContainerStateChanged(ServerDrivenState.Started)
                    }
                }

                is ViewState.DoCancel -> {
                    onServerDrivenContainerStateChanged(ServerDrivenState.Canceled)
                }

                is ViewState.DoRender -> {
                    showScreen(it.screenId, it.component)
                }
            }
        })
    }

    private fun showScreen(screenName: String?, component: ServerDrivenComponent) {
        val transition = getFragmentTransitionAnimation()
        supportFragmentManager
            .beginTransaction()
            .setCustomAnimations(
                transition.enter,
                transition.exit,
                transition.popEnter,
                transition.popExit
            )
            .replace(getServerDrivenContainerId(), BeagleFragment.newInstance(component, screenName))
            .addToBackStack(screenName)
            .commit()
    }
}