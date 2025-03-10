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

package br.com.zup.beagle.android.networking

import android.os.Parcelable
import br.com.zup.beagle.core.BeagleJson
import kotlinx.android.parcel.Parcelize

/**
 * HttpAdditionalData is used to do requests.
 * @param method HTTP method.
 * @param headers Header items for the request.
 * @param body Content that will be delivered with the request.
 */
@BeagleJson
@Parcelize
data class HttpAdditionalData(
    val method: HttpMethod? = HttpMethod.GET,
    val headers: Map<String, String>? = hashMapOf(),
    val body: String? = null,
) : Parcelable