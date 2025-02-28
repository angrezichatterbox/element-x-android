/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.api.player

data class MediaPlayerControllerState(
    val isVisible: Boolean,
    val isPlaying: Boolean,
    val progressInMillis: Long,
    val durationInMillis: Long,
    val isMuted: Boolean,
)
