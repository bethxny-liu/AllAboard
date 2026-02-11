package org.allaboard.project.ui.screens.createActivity

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.lifecycle.viewmodel.compose.viewModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator

/**
 * Create Custom Activity screen: allows users to create a new activity that others can swipe on.
 * Modeled after Create Trip screens with similar design and user flow.
 */
class CreateCustomActivityScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        val viewModel: CreateCustomActivityViewModel = viewModel()
        val uiState by viewModel.uiState.collectAsState()

        CreateCustomActivityContent(
            uiState = uiState,
            onBack = { navigator?.pop() },
            onCreate = { viewModel.onCreateActivity() },
            onCategoryChange = viewModel::updateCategory,
            onNameChange = viewModel::updateName,
            onLocationChange = viewModel::updateLocation,
            onDescriptionChange = viewModel::updateDescription
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateCustomActivityContent(
    uiState: CreateCustomActivityUiState,
    onBack: () -> Unit,
    onCreate: () -> Unit,
    onCategoryChange: (String) -> Unit = {},
    onNameChange: (String) -> Unit = {},
    onLocationChange: (String) -> Unit = {},
    onDescriptionChange: (String) -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 96.dp)
    ) {
        // Back arrow and Title on same row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }

            Spacer(Modifier.width(8.dp))

            Text(
                text = "Create Custom Activity",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(20.dp))

        // Category
        Text("Category", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        Spacer(Modifier.height(8.dp))

        // Use the project's CategoryDropdown pattern (index-based) and map to string API
        val categories = listOf("Landmark", "Restaurant", "Activity")
        val selectedIndex = categories.indexOf(uiState.category).coerceAtLeast(0)

        CategoryDropdown(
            categories = categories,
            selectedIndex = selectedIndex,
            onCategorySelected = { idx -> onCategoryChange(categories.getOrNull(idx) ?: categories.first()) },
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
                        .border(BorderStroke(1.dp, Color.Black), RoundedCornerShape(25.dp))
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

        Spacer(Modifier.height(20.dp))

        Text("Add Photo or Link", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Upload button
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .border(BorderStroke(1.dp, Color.Black), RoundedCornerShape(16.dp))
                    .background(FieldBackground, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = Icons.Filled.CloudUpload, contentDescription = "Upload", tint = MaterialTheme.colorScheme.onBackground, modifier = Modifier.size(24.dp))
            }

            // Link button
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .border(BorderStroke(1.dp, Color.Black), RoundedCornerShape(16.dp))
                    .background(FieldBackground, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = Icons.Filled.Link, contentDescription = "Add link", tint = MaterialTheme.colorScheme.onBackground, modifier = Modifier.size(24.dp))
            }
        }

        // push remaining content (including the button) to the bottom
        Spacer(modifier = Modifier.weight(1f))

        // Create Button pinned near the bottom (above footer)
        Button(
            onClick = onCreate,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(26.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Text(text = if (uiState.isCreating) "Creating..." else "Create Activity", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
        }

        // small bottom padding
        Spacer(Modifier.height(12.dp))
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
        // Wrap the text field in a bordered box so it visually matches the other inputs
        Box(
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
                .height(50.dp)
                .border(BorderStroke(1.dp, Color.Black), RoundedCornerShape(25.dp))
                .background(FieldBackground, RoundedCornerShape(25.dp))
        ) {
            BasicTextField(
                value = selectedText,
                onValueChange = {},
                readOnly = true,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onBackground),
                interactionSource = interactionSource,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 20.dp, end = 44.dp), // small inset for the icon
            ) { innerTextField ->
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.CenterStart) {
                    innerTextField()
                }
            }

            // trailing icon aligned to the end inside the bordered box (reliable right placement)
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 8.dp)
                    .width(36.dp),
                contentAlignment = Alignment.Center
            ) {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            }
        }

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth() // make menu match the width of the anchor
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
