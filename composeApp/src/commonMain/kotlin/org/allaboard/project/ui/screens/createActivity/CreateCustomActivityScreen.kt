package org.allaboard.project.ui.screens.createActivity

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import org.allaboard.project.ui.theme.TextPrimary
import org.allaboard.project.ui.theme.FieldBackground
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator

/**
 * Create Custom Activity screen modeled after Create Trip pages.
 * Users can input details (location, description, category etc.) and make a new activity that others can swipe on.
 */
class CreateCustomActivityScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        val vm: CreateCustomActivityViewModel = viewModel()

        CreateCustomActivityContent(
            uiState = vm.uiState,
            onBack = { navigator?.pop() },
            onCreate = { vm.onCreateActivity() },
            onCategoryChange = vm::updateCategory,
            onNameChange = vm::updateName,
            onLocationChange = vm::updateLocation,
            onDescriptionChange = vm::updateDescription
        )
    }
}

// UI State
data class CreateCustomActivityUiState(
    val category: String = "Landmark",
    val name: String = "",
    val location: String = "",
    val description: String = "",
    val isCreating: Boolean = false
)

class CreateCustomActivityViewModel : androidx.lifecycle.ViewModel() {
    var uiState by mutableStateOf(CreateCustomActivityUiState())
        private set

    fun updateCategory(c: String) {
        uiState = uiState.copy(category = c)
    }

    fun updateName(v: String) { uiState = uiState.copy(name = v) }
    fun updateLocation(v: String) { uiState = uiState.copy(location = v) }
    fun updateDescription(v: String) { uiState = uiState.copy(description = v) }

    fun onCreateActivity() {
        uiState = uiState.copy(isCreating = true)
        // TODO: perform create action and reset isCreating when done
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCustomActivityContent(
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
        // Back arrow and title
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("◀", modifier = Modifier.clickable { onBack() }, fontSize = 20.sp)
            Spacer(Modifier.width(12.dp))
            Text(
                text = "Create Custom Activity",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(Modifier.height(20.dp))

        // Category
        Text("Category", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
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
        Text("Activity Name", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
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
                        .background(FieldBackground, RoundedCornerShape(25.dp))
                        .padding(start = 20.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (uiState.name.isEmpty()) {
                        Text("e.g. Sunset Picnic", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    inner()
                }
            }
        )

        Spacer(Modifier.height(16.dp))

        // Location
        Text("Location", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
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
                        .background(FieldBackground, RoundedCornerShape(25.dp))
                        .padding(start = 20.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (uiState.location.isEmpty()) {
                        Text("Add a location or address", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    inner()
                }
            }
        )

        Spacer(Modifier.height(16.dp))

        // Description
        Text("Description", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
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
                        .background(FieldBackground, RoundedCornerShape(16.dp))
                        .padding(all = 16.dp),
                    contentAlignment = Alignment.TopStart
                ) {
                    if (uiState.description.isEmpty()) {
                        Text("Describe the activity ...", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    inner()
                }
            }
        )

        Spacer(Modifier.height(20.dp))

        Text("Add Photo or Link", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
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
                    .background(Color(0xFFF5F5F5), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("⤴", fontSize = 20.sp)
            }

            // Link button
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(Color(0xFFF5F5F5), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("🔗", fontSize = 20.sp)
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
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD8F0FB))
        ) {
            Text(text = if (uiState.isCreating) "Creating..." else "Create Activity", color = Color.Black)
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
        BasicTextField(
            value = selectedText,
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            textStyle = TextStyle(color = MaterialTheme.colorScheme.onBackground, fontSize = 14.sp),
            interactionSource = interactionSource,
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(0.6f)
                .height(50.dp)
        ) { innerTextField ->
            TextFieldDefaults.DecorationBox(
                value = selectedText,
                innerTextField = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 20.dp),
                        contentAlignment = Alignment.CenterStart
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

@Composable
@Preview(showBackground = true)
private fun PreviewCreateCustomActivity() {
    val vm = CreateCustomActivityViewModel()
    CreateCustomActivityContent(uiState = vm.uiState, onBack = {}, onCreate = {})
}
