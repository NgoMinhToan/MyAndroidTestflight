package com.mycompany.testflight.feature.applist.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mycompany.testflight.designsystem.tokens.FriendlyType
import com.mycompany.testflight.designsystem.tokens.PillShape
import com.mycompany.testflight.designsystem.tokens.TestflightColors
import com.mycompany.testflight.feature.applist.R
import com.mycompany.testflight.feature.applist.install.InstallStage
import com.mycompany.testflight.feature.applist.install.InstallState
import kotlin.math.roundToInt

private enum class PillPhase { Idle, NoBuild, Busy, Downloading, Verifying, Launching }

/**
 * Install pill used in list rows and version cards. The phase drives the animated colors and
 * the label swap; progress text inside the Downloading phase updates without a new transition.
 */
@Composable
fun InstallPill(
    idleLabel: String,
    state: InstallState?,
    installBusy: Boolean,
    hasBuild: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    idleContainerColor: Color = TestflightColors.CoralStrong,
    idleContentColor: Color = TestflightColors.White,
    unavailableLabel: String = "—",
    textStyle: TextStyle = FriendlyType.PillLabel,
    contentDescription: String? = null,
    unavailableContentDescription: String? = null,
) {
    val phase = when {
        state?.stage == InstallStage.Downloading -> PillPhase.Downloading
        state?.stage == InstallStage.Verifying -> PillPhase.Verifying
        state?.stage == InstallStage.Launching -> PillPhase.Launching
        !hasBuild -> PillPhase.NoBuild
        installBusy -> PillPhase.Busy
        else -> PillPhase.Idle
    }
    val containerTarget = when (phase) {
        PillPhase.Downloading, PillPhase.Verifying, PillPhase.Launching ->
            TestflightColors.Navy
        PillPhase.NoBuild -> TestflightColors.DisabledFill
        else -> idleContainerColor
    }
    val contentTarget = when (phase) {
        PillPhase.Downloading, PillPhase.Verifying, PillPhase.Launching ->
            TestflightColors.White
        PillPhase.NoBuild -> TestflightColors.DisabledText
        else -> idleContentColor
    }
    val container by animateColorAsState(
        targetValue = containerTarget,
        animationSpec = tween(180),
        label = "pillContainer",
    )
    val content by animateColorAsState(
        targetValue = contentTarget,
        animationSpec = tween(180),
        label = "pillContent",
    )
    val enabled = state == null && hasBuild && !installBusy
    val dim = installBusy && state == null && hasBuild
    val semanticsDescription = when {
        state != null -> null
        !hasBuild -> unavailableContentDescription ?: contentDescription
        enabled -> contentDescription
        else -> null
    }

    CompositionLocalProvider(LocalContentColor provides content) {
        Box(
            modifier = modifier
                .alpha(if (dim) 0.4f else 1f)
                .minimumInteractiveComponentSize()
                .clip(PillShape)
                .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
                .semantics {
                    if (semanticsDescription != null) {
                        this.contentDescription = semanticsDescription
                    }
                },
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .clip(PillShape)
                    .background(container)
                    .padding(horizontal = 14.dp, vertical = 9.dp),
                contentAlignment = Alignment.Center,
            ) {
                AnimatedContent(
                    targetState = phase,
                    transitionSpec = {
                        fadeIn(tween(120)) togetherWith fadeOut(tween(90))
                    },
                    label = "installPhase",
                ) { target ->
                    Text(
                        text = when (target) {
                            PillPhase.Downloading -> state?.progress?.let { progress ->
                                stringResource(
                                    R.string.installing_percent,
                                    (progress * 100).roundToInt(),
                                )
                            } ?: stringResource(R.string.installing)

                            PillPhase.Verifying -> stringResource(R.string.verifying)
                            PillPhase.Launching -> stringResource(R.string.opening_installer)
                            PillPhase.NoBuild -> unavailableLabel
                            PillPhase.Busy, PillPhase.Idle -> idleLabel
                        },
                        style = textStyle,
                        color = content,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}
