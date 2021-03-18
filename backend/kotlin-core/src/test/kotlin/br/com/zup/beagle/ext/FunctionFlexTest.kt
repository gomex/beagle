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

package br.com.zup.beagle.ext

import br.com.zup.beagle.core.Layout
import br.com.zup.beagle.fake.FlexComponentFake
import br.com.zup.beagle.widget.core.Size
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

@DisplayName("Given a Flex")
internal class FunctionFlexTest {

    @DisplayName("When call this function")
    @Nested
    inner class FlexTest {

        @Test
        @DisplayName("Then should return the current widget with instance of style")
        fun testFunctionFlex() {
            // Given
            val grow = 1.0

            // When
            val styleComponent = Flex(FlexComponentFake(), {
                this.grow = grow
            })

            // Then
            val expected = FlexComponentFake(
                Layout(
                    flex = br.com.zup.beagle.widget.core.Flex(
                        grow = grow
                    ),
                    size = Size()
                )
            )

            assertEquals(expected, styleComponent)
        }
    }
}