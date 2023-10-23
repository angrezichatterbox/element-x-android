/*
 * Copyright (c) 2023 New Vector Ltd
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

package io.element.android.features.lockscreen.impl.setup

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.appconfig.LockScreenConfig
import io.element.android.features.lockscreen.impl.pin.model.assertEmpty
import io.element.android.features.lockscreen.impl.pin.model.assertText
import io.element.android.features.lockscreen.impl.setup.validation.PinValidator
import io.element.android.features.lockscreen.impl.setup.validation.SetupPinFailure
import io.element.android.libraries.matrix.test.core.aBuildMeta
import io.element.android.tests.testutils.awaitLastSequentialItem
import kotlinx.coroutines.test.runTest
import org.junit.Test

class SetupPinPresenterTest {

    private val blacklistedPin = LockScreenConfig.PIN_BLACKLIST
    private val halfCompletePin = "12"
    private val completePin = "1235"
    private val mismatchedPin = "1236"

    @Test
    fun `present - complete flow`() = runTest {

        val presenter = createSetupPinPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            awaitItem().also { state ->
                state.choosePinEntry.assertEmpty()
                state.confirmPinEntry.assertEmpty()
                assertThat(state.setupPinFailure).isNull()
                assertThat(state.isConfirmationStep).isFalse()
                state.eventSink(SetupPinEvents.OnPinEntryChanged(halfCompletePin))
            }
            awaitItem().also { state ->
                state.choosePinEntry.assertText(halfCompletePin)
                state.confirmPinEntry.assertEmpty()
                assertThat(state.setupPinFailure).isNull()
                assertThat(state.isConfirmationStep).isFalse()
                state.eventSink(SetupPinEvents.OnPinEntryChanged(blacklistedPin))
            }
            awaitLastSequentialItem().also { state ->
                state.choosePinEntry.assertText(blacklistedPin)
                assertThat(state.setupPinFailure).isEqualTo(SetupPinFailure.PinBlacklisted)
                state.eventSink(SetupPinEvents.ClearFailure)
            }
            awaitLastSequentialItem().also { state ->
                state.choosePinEntry.assertEmpty()
                assertThat(state.setupPinFailure).isNull()
                state.eventSink(SetupPinEvents.OnPinEntryChanged(completePin))
            }
            awaitLastSequentialItem().also { state ->
                state.choosePinEntry.assertText(completePin)
                state.confirmPinEntry.assertEmpty()
                assertThat(state.isConfirmationStep).isTrue()
                state.eventSink(SetupPinEvents.OnPinEntryChanged(mismatchedPin))
            }
            awaitLastSequentialItem().also { state ->
                state.choosePinEntry.assertText(completePin)
                state.confirmPinEntry.assertText(mismatchedPin)
                assertThat(state.setupPinFailure).isEqualTo(SetupPinFailure.PinsDontMatch)
                state.eventSink(SetupPinEvents.ClearFailure)
            }
            awaitLastSequentialItem().also { state ->
                state.choosePinEntry.assertEmpty()
                state.confirmPinEntry.assertEmpty()
                assertThat(state.isConfirmationStep).isFalse()
                assertThat(state.setupPinFailure).isNull()
                state.eventSink(SetupPinEvents.OnPinEntryChanged(completePin))
            }
            awaitLastSequentialItem().also { state ->
                state.choosePinEntry.assertText(completePin)
                state.confirmPinEntry.assertEmpty()
                assertThat(state.isConfirmationStep).isTrue()
                state.eventSink(SetupPinEvents.OnPinEntryChanged(completePin))
            }
            awaitItem().also { state ->
                state.choosePinEntry.assertText(completePin)
                state.confirmPinEntry.assertText(completePin)
            }
        }
    }

    private fun createSetupPinPresenter(): SetupPinPresenter {
        return SetupPinPresenter(PinValidator(), aBuildMeta())
    }
}
