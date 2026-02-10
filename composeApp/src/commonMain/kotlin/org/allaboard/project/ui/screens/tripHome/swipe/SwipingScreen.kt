package org.allaboard.project.ui.screens.tripHome.swipe

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import androidx.lifecycle.viewmodel.compose.viewModel
import org.allaboard.project.ui.theme.Background
import org.allaboard.project.ui.theme.FieldBackground
import org.allaboard.project.ui.theme.SwipeDislike
import org.allaboard.project.ui.theme.SwipeLike
import org.allaboard.project.ui.theme.TextPrimary
import org.allaboard.project.ui.theme.TextSecondary
import org.jetbrains.compose.resources.painterResource
import team_102_8.composeapp.generated.resources.Res
import team_102_8.composeapp.generated.resources.prettyplace
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SwipingScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        val viewModel = viewModel { SwipingViewModel() }
        val uiState by viewModel.uiState.collectAsState()

        SwipingScreenContent(
            uiState = uiState,
            onBack = { navigator?.pop() },
            onDislike = viewModel::onDislike,
            onSuperLike = viewModel::onSuperLike,
            onLike = viewModel::onLike,
            onCategorySelected = viewModel::onCategorySelected,
            onAllDone = { /*navigator?.push(SwipeResultsScreen()) */}
        )
    }
}

@Composable
fun SwipingScreenContent(
    uiState: SwipingUiState,
    onBack: () -> Unit,
    onDislike: () -> Unit,
    onSuperLike: () -> Unit,
    onLike: () -> Unit,
    onCategorySelected: (Int) -> Unit,
    onAllDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val flashAlpha = remember { Animatable(0f) }
    var flashColor by remember { mutableStateOf<Color?>(null) }
    var flashIcon by remember { mutableStateOf<ImageVector?>(null) }

    val triggerFlash: (Color, ImageVector) -> Unit = { color, icon ->
        scope.launch {
            flashColor = color
            flashIcon = icon
            flashAlpha.snapTo(0f)
            flashAlpha.animateTo(0.35f, animationSpec = tween(150))
            delay(250)
            flashAlpha.animateTo(0f, animationSpec = tween(300))
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Background)
    ) {
        LaunchedEffect(uiState.isAllDone) {
            if (uiState.isAllDone) {
                onAllDone()
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.fillMaxWidth()) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }
            }
            Text(
                text = "Swipe!",
                style = MaterialTheme.typography.headlineLarge,
                color = TextPrimary,
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-8).dp),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            CategoryDropdown(
                categories = uiState.categories,
                selectedIndex = uiState.selectedCategoryIndex,
                onCategorySelected = onCategorySelected
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            val card = uiState.currentCard
            AnimatedContent(
                targetState = card,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "SwipeCardFade"
            ) { targetCard ->
                if (targetCard != null) {
                    key(targetCard.id) {
                        SwipeCard(
                            name = targetCard.name,
                            location = targetCard.location,
                            category = targetCard.category,
                            imagePainter = painterResource(Res.drawable.prettyplace),
                            modifier = Modifier.fillMaxWidth(),
                            onDislike = {
                                triggerFlash(SwipeDislike, Icons.Filled.Close)
                                onDislike()
                            },
                            onSuperLike = onSuperLike,
                            onLike = {
                                triggerFlash(SwipeLike, Icons.Filled.Check)
                                onLike()
                            }
                        )
                    }
                } else if (uiState.isCategoryDone) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 24.dp),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        CategoryDoneMessage(category = uiState.selectedCategory)
                    }
                }
            }
        }
        }

        if (flashColor != null && flashAlpha.value > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(flashColor!!.copy(alpha = flashAlpha.value)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = flashIcon ?: Icons.Filled.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(96.dp)
                )
            }
        }
    }
}

@Composable
private fun CategoryDoneMessage(category: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(FieldBackground)
            .padding(horizontal = 24.dp, vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Filled.Check,
            contentDescription = null,
            tint = TextPrimary,
            modifier = Modifier.size(42.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "All set for $category",
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Pick another category to keep swiping.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryDropdown(
    categories: List<String>,
    selectedIndex: Int,
    onCategorySelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedText = categories.getOrNull(selectedIndex) ?: categories.firstOrNull().orEmpty()
    val interactionSource = remember { MutableInteractionSource() }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        BasicTextField(
            value = selectedText,
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            textStyle = MaterialTheme.typography.titleMedium.copy(
                color = TextPrimary,
                textAlign = TextAlign.Center
            ),
            interactionSource = interactionSource,
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(0.6f)
                .height(40.dp)
        ) { innerTextField ->
            TextFieldDefaults.DecorationBox(
                value = selectedText,
                innerTextField = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        innerTextField()
                    }
                },
                enabled = true,
                singleLine = true,
                visualTransformation = VisualTransformation.None,
                interactionSource = interactionSource,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = FieldBackground,
                    unfocusedContainerColor = FieldBackground,
                    disabledContainerColor = FieldBackground,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                contentPadding = TextFieldDefaults.contentPaddingWithoutLabel(
                    top = 4.dp,
                    bottom = 4.dp
                ),
                shape = RoundedCornerShape(999.dp)
            )
        }
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            categories.forEachIndexed { index, category ->
                DropdownMenuItem(
                    text = { Text(text = category) },
                    onClick = {
                        expanded = false
                        onCategorySelected(index)
                    }
                )
            }
        }
    }
}
