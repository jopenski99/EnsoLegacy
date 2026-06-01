package com.ensolegacy.mobile.ui.onboarding

import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ensolegacy.mobile.R

/** Past this much horizontal drag (px), a swipe advances/rewinds a slide. */
private const val SWIPE_THRESHOLD = 100f

/** Duration of the morph between slides. */
private const val MORPH_MS = 450

/**
 * First-run onboarding: a three-slide arc — Legacy → Forever → Documenting — in
 * the calm paper/olive design language. Slides do **not** slide horizontally;
 * each illustration morphs in place — the outgoing art grows and fades while the
 * incoming art zooms in ([AnimatedContent] + scale/fade), so it reads as one
 * tree transforming. Advance by tapping Next or swiping; Skip / "Start My
 * Legacy" call [onFinish].
 */
@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    val pages = OnboardingPage.entries
    var currentPage by remember { mutableIntStateOf(0) }
    val isLastPage = currentPage == pages.lastIndex
    val isFirstPage = currentPage == 0

    fun goNext() = if (isLastPage) onFinish() else currentPage++
    fun goBack() { if (!isFirstPage) currentPage-- }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding(),
    ) {
        // Brand wordmark + Skip. Skip is hidden on the final slide, where the
        // primary CTA takes over.
        // Fixed height so the wordmark stays put whether or not Skip is shown
        // (Skip's touch target would otherwise change the row height per slide).
        Row(
            modifier = Modifier.fillMaxWidth().height(56.dp).padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Wordmark()
            Spacer(Modifier.weight(1f))
            if (!isLastPage) {
                TextButton(onClick = onFinish) {
                    Text(
                        text = "Skip",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        // Morphing content area. A horizontal drag past the threshold advances
        // or rewinds, but the transition is a zoom/fade in place — never a slide.
        var dragAccum by remember { mutableFloatStateOf(0f) }
        AnimatedContent(
            targetState = currentPage,
            transitionSpec = {
                val forward = targetState > initialState
                val enterScale = if (forward) 0.82f else 1.12f
                val exitScale = if (forward) 1.12f else 0.82f
                (fadeIn(tween(MORPH_MS)) + scaleIn(initialScale = enterScale, animationSpec = tween(MORPH_MS)))
                    .togetherWith(
                        fadeOut(tween(MORPH_MS)) + scaleOut(targetScale = exitScale, animationSpec = tween(MORPH_MS)),
                    )
            },
            contentAlignment = Alignment.Center,
            label = "onboardingMorph",
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            if (dragAccum <= -SWIPE_THRESHOLD) goNext()
                            else if (dragAccum >= SWIPE_THRESHOLD) goBack()
                            dragAccum = 0f
                        },
                        onDragCancel = { dragAccum = 0f },
                    ) { _, dragAmount -> dragAccum += dragAmount }
                },
        ) { page ->
            OnboardingSlide(pages[page])
        }

        PageIndicator(
            pageCount = pages.size,
            currentPage = currentPage,
            modifier = Modifier.padding(vertical = 20.dp),
        )

        // Primary action — Next while paging, "Start My Legacy" on the last slide.
        Button(
            onClick = { goNext() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
        ) {
            Text(if (isLastPage) "Start My Legacy" else "Next")
            if (isLastPage) {
                Spacer(Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
            }
        }

        // Back — present from the second slide onward; a fixed-height slot keeps
        // the layout from shifting on the first slide.
        Box(
            modifier = Modifier.fillMaxWidth().height(48.dp).padding(top = 4.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (!isFirstPage) {
                TextButton(onClick = { goBack() }) {
                    Text("Back", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun Wordmark() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        // Emblem placeholder — swap for the leaf/ensō brand mark.
        Text(text = "❖", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(8.dp))
        Text(
            text = "Ensō Legacy",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun OnboardingSlide(page: OnboardingPage) {
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // Transparent art, no circle backdrop — so the tall scroll on the final
        // slide isn't clipped. The morph (scale/fade) is driven by AnimatedContent.
        Image(
            painter = painterResource(page.illustration),
            contentDescription = page.title,
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(260.dp),
        )
        Spacer(Modifier.height(40.dp))
        Text(
            text = page.title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = page.body,
            style = MaterialTheme.typography.bodyLarge,
            fontStyle = FontStyle.Italic,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun PageIndicator(pageCount: Int, currentPage: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
    ) {
        repeat(pageCount) { index ->
            val selected = index == currentPage
            val width by animateDpAsState(if (selected) 24.dp else 8.dp, label = "dotWidth")
            val color by animateColorAsState(
                if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.outline,
                label = "dotColor",
            )
            Box(
                modifier = Modifier
                    .padding(horizontal = 4.dp)
                    .height(8.dp)
                    .width(width)
                    .clip(RoundedCornerShape(50))
                    .background(color),
            )
        }
    }
}

/**
 * The onboarding slides, in order. Copy is calm and reverent — no growth-hacking
 * or social proof (per the product tone rules).
 */
private enum class OnboardingPage(
    @DrawableRes val illustration: Int,
    val title: String,
    val body: String,
) {
    LEGACY(
        illustration = R.drawable.onboarding_step1,
        title = "Legacy",
        body = "Every bonsai has a beginning.",
    ),
    FOREVER(
        illustration = R.drawable.onboarding_step2,
        title = "Forever",
        body = "A legacy that lives through time.",
    ),
    DOCUMENTING(
        illustration = R.drawable.onboarding_step3,
        title = "Documenting Milestones",
        body = "Preserve every milestone.",
    ),
}
