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

package br.com.zup.beagle.android.components.utils

import android.app.Activity
import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.TypedValue
import android.view.View
import android.view.inputmethod.InputMethodManager
import br.com.zup.beagle.android.utils.StyleManager
import br.com.zup.beagle.android.utils.toAndroidColor
import br.com.zup.beagle.android.view.ViewFactory
import br.com.zup.beagle.core.ServerDrivenComponent
import br.com.zup.beagle.core.StyleComponent

internal var viewExtensionsViewFactory = ViewFactory()
internal var styleManagerFactory = StyleManager()
const val FLOAT_ZERO = 0.0f

internal fun View.hideKeyboard() {
    val activity = context as AppCompatActivity
    val view = activity.currentFocus ?: viewExtensionsViewFactory.makeView(activity)
    val imm = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(view.windowToken, 0)
}

internal fun View.applyStyle(component: ServerDrivenComponent) {
    (component as? StyleComponent)?.let {
        if (it.style?.backgroundColor != null) {
            this.background = GradientDrawable()
            applyBackgroundColor(it)
            applyCornerRadius(it)
        } else {
            styleManagerFactory.applyStyleComponent(
                context = context,
                component = it,
                view = this
            )
        }
    }
}

internal fun View.applyViewBackgroundAndCorner(backgroundColor: Int?, component: StyleComponent) {
    if (backgroundColor != null) {
        this.background = GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(backgroundColor, backgroundColor)
        )

        this.applyCornerRadius(component)
    }
}

internal fun View.applyBackgroundColor(styleWidget: StyleComponent) {
    styleWidget.style?.backgroundColor?.let {
        (this.background as? GradientDrawable)?.setColor(it.toAndroidColor())
    }
}

internal fun View.applyCornerRadius(styleWidget: StyleComponent) {
    styleWidget.style?.cornerRadius?.let { cornerRadius ->
        if (cornerRadius.radius > FLOAT_ZERO) {
            (this.background as? GradientDrawable)?.cornerRadius = cornerRadius.radius.toFloat()
        }
    }
}

internal fun View.applyBackgroundFromWindowBackgroundTheme(context: Context) {
    val typedValue = styleManagerFactory
        .getTypedValueByResId(android.R.attr.windowBackground, context)
    if (typedValue.type >= TypedValue.TYPE_FIRST_COLOR_INT &&
        typedValue.type <= TypedValue.TYPE_LAST_COLOR_INT
    ) {
        setBackgroundColor(typedValue.data)
    } else {
        background = ContextCompat.getDrawable(context, typedValue.resourceId)
    }
}