package org.allaboard.project.ui.screens.createActivity

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.allaboard.project.ui.theme.FieldBackground
import org.allaboard.project.ui.theme.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.lifecycle.viewmodel.compose.viewModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import org.allaboard.project.domain.Activity
import org.allaboard.project.ui.components.CategoryDropdown
import org.allaboard.project.ui.components.ScreenTopBar
import org.allaboard.project.ui.components.ScreenTopBarDefaults
import org.allaboard.project.di.AppModule
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay

/**
 * Create Custom Activity screen. When [existingActivity] is non-null, the screen is in edit mode.
 */
class CreateCustomActivityScreen(
    private val tripId: String,
    private val existingActivity: Activity? = null
) : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        val viewModel: CreateCustomActivityViewModel = viewModel {
            CreateCustomActivityViewModel(
                model = AppModule.allAboardModel,
                tripId = tripId,
                existingActivity = existingActivity
            )
        }
        val uiState by viewModel.uiState.collectAsState()
        val isEditMode = existingActivity != null

        LaunchedEffect(uiState.isSuccess) {
            if (uiState.isSuccess) {
                delay(1000)
                navigator?.pop()
            }
        }

        if (uiState.isSuccess) {
            SuccessScreen(isEditMode = isEditMode)
        } else {
            CreateCustomActivityContent(
                uiState = uiState,
                isEditMode = isEditMode,
                onBack = { navigator?.pop() },
                onCreate = viewModel::onCreateOrUpdateActivity,
                onCategoryChange = viewModel::updateCategory,
                onNameChange = viewModel::updateName,
                onLocationChange = viewModel::updateLocation,
                onDescriptionChange = viewModel::updateDescription,
                onLinkChange = viewModel::updateLink
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateCustomActivityContent(
    uiState: CreateCustomActivityUiState,
    isEditMode: Boolean = false,
    onBack: () -> Unit = {},
    onCategoryChange: (Int) -> Unit = {},
    onNameChange: (String) -> Unit = {},
    onLocationChange: (String) -> Unit = {},
    onDescriptionChange: (String) -> Unit = {},
    onLinkChange: (String) -> Unit = {},
    onCreate: () -> Unit = {}
) {
    var showLinkDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Surface)
    ) {
        ScreenTopBar(
            title = if (isEditMode) "Edit Activity" else "Create Custom Activity",
            onBack = onBack
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(
                    start = ScreenTopBarDefaults.ContentPaddingHorizontal,
                    end = ScreenTopBarDefaults.ContentPaddingHorizontal,
                    top = 16.dp,
                    bottom = 80.dp
                )
        ) {
        Spacer(Modifier.height(20.dp))

        // Category
        Text("Category", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        Spacer(Modifier.height(8.dp))

        // Use shared CategoryDropdown component
        CategoryDropdown(
            categories = uiState.categories,
            selectedIndex = uiState.selectedCategoryIndex,
            onCategorySelected = onCategoryChange,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        // Activity Name
        Text("Activity Name", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        Spacer(Modifier.height(8.dp))
        BasicTextField(
            value = uiState.name,
            onValueChange = onNameChange,
            singleLine = true,
            textStyle = TextStyle(color = MaterialTheme.colorScheme.onBackground),
            modifier = Modifier.fillMaxWidth(),
            decorationBox = { inner ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .border(
                            BorderStroke(
                                width = if (uiState.error == "Name required") 2.dp else 1.dp,
                                color = if (uiState.error == "Name required") MaterialTheme.colorScheme.error else Color.Black
                            ),
                            RoundedCornerShape(25.dp)
                        )
                        .background(FieldBackground, RoundedCornerShape(25.dp))
                        .padding(start = 20.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (uiState.name.isEmpty()) {
                        Text("e.g. Sunset Picnic", style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                    }
                    inner()
                }
            }
        )
        if (uiState.error == "Name required") {
            Text(
                text = uiState.error,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(start = 20.dp, top = 4.dp)
            )
        }

        Spacer(Modifier.height(16.dp))

        // Location
        Text("Location", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        Spacer(Modifier.height(8.dp))
        BasicTextField(
            value = uiState.location,
            onValueChange = onLocationChange,
            singleLine = true,
            textStyle = TextStyle(color = MaterialTheme.colorScheme.onBackground),
            modifier = Modifier.fillMaxWidth(),
            decorationBox = { inner ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .border(BorderStroke(1.dp, Color.Black), RoundedCornerShape(25.dp))
                        .background(FieldBackground, RoundedCornerShape(25.dp))
                        .padding(start = 20.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (uiState.location.isEmpty()) {
                        Text("Add a location or address", style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                    }
                    inner()
                }
            }
        )

        Spacer(Modifier.height(16.dp))

        // Description
        Text("Description", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        Spacer(Modifier.height(8.dp))
        BasicTextField(
            value = uiState.description,
            onValueChange = onDescriptionChange,
            singleLine = false,
            textStyle = TextStyle(color = MaterialTheme.colorScheme.onBackground),
            modifier = Modifier.fillMaxWidth(),
            decorationBox = { inner ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .border(BorderStroke(1.dp, Color.Black), RoundedCornerShape(16.dp))
                        .background(FieldBackground, RoundedCornerShape(16.dp))
                        .padding(all = 16.dp),
                    contentAlignment = Alignment.TopStart
                ) {
                    if (uiState.description.isEmpty()) {
                        Text("Describe the activity ...", style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                    }
                    inner()
                }
            }
        )

        Spacer(Modifier.height(16.dp))

        Spacer(Modifier.height(20.dp))

        Text("Add Link", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        Spacer(Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .size(60.dp)
                .border(BorderStroke(1.dp, Color.Black), RoundedCornerShape(16.dp))
                .background(FieldBackground, RoundedCornerShape(16.dp))
                .clickable { showLinkDialog = true },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Link,
                contentDescription = "Add link",
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(24.dp)
            )
        }

        // Link entry dialog (reuses ViewModel's link state via onLinkChange)
        if (showLinkDialog) {
            AlertDialog(
                onDismissRequest = { showLinkDialog = false },
                title = { Text("Add a link") },
                text = {
                    Column {
                        Text("Paste a website URL or reservation link below:")
                        Spacer(Modifier.height(8.dp))
                        TextField(
                            value = uiState.link,
                            onValueChange = onLinkChange,
                            singleLine = true,
                            placeholder = { Text("https://example.com") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = { showLinkDialog = false },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color.Black)
                    ) {
                        Text("Done")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            onLinkChange("")
                            showLinkDialog = false
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color.Black)
                    ) {
                        Text("Clear")
                    }
                }
            )
        }

        Spacer(Modifier.height(24.dp))

        // Create / Save Button
        Button(
            onClick = onCreate,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(26.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Text(
                text = when {
                    uiState.isCreating && isEditMode -> "Saving..."
                    uiState.isCreating -> "Creating..."
                    isEditMode -> "Save changes"
                    else -> "Create Activity"
                },
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        // small bottom padding
        Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun SuccessScreen(isEditMode: Boolean = false) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = "Success",
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = if (isEditMode) "Activity updated!" else "Activity Created!",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

