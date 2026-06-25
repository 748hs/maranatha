package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ui.MaranathaViewModel
import com.example.ui.theme.BotswanaBlue
import com.example.ui.theme.BotswanaBlueDark
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.GoldenAmber
import com.example.ui.theme.SuccessGreen

@Composable
fun PostAdScreen(
    viewModel: MaranathaViewModel,
    adIdToEdit: Long?, // Null means new ad, otherwise editing
    onSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUserFlow.collectAsState()

    // Form inputs state
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var priceStr by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Vehicles") }
    var selectedLocation by remember { mutableStateOf("Gaborone") }
    var contactNumber by remember { mutableStateOf("") }
    var attachedImages by remember { mutableStateOf<List<String>>(emptyList()) }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    // Dropdowns configuration
    val categories = listOf(
        "Vehicles", "Property", "Electronics", "Phones", "Computers", 
        "Furniture", "Fashion", "Jobs", "Services", "Agriculture", "Other"
    )

    val botswanaCities = listOf(
        "Gaborone", "Francistown", "Maun", "Phakalane", "Lobatse", 
        "Selibe Phikwe", "Kasane", "Jwaneng", "Serowe", "Palapye", 
        "Molepolole", "Kanye", "Mahalapye", "Mochudi", "Ghanzi", "Orapa"
    )

    // Prepopulate if in edit mode
    LaunchedEffect(adIdToEdit) {
        if (adIdToEdit != null) {
            val ads = viewModel.allAdsFlow.value
            val ad = ads.find { it.id == adIdToEdit }
            if (ad != null) {
                title = ad.title
                description = ad.description
                priceStr = ad.price.toInt().toString()
                selectedCategory = ad.category
                selectedLocation = ad.location
                contactNumber = ad.contactNumber
                attachedImages = ad.imageUrlsJson.split(";").filter { it.isNotBlank() }
            }
        } else {
            // Set initial contact number to user phone
            currentUser?.let {
                contactNumber = it.phoneNumber
            }
        }
    }

    // Photo picker configuration
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 5),
        onResult = { uris ->
            val uriStrings = uris.map { it.toString() }
            if (uriStrings.isNotEmpty()) {
                attachedImages = attachedImages + uriStrings
            }
        }
    )

    // Preset Catalog images matching currently selected category to guarantee awesome testing results!
    val presetImagesCatalog = when (selectedCategory) {
        "Vehicles" -> listOf(
            PresetImg("Toyota Hilux", "https://images.unsplash.com/photo-1533473359331-0135ef1b58bf?auto=format&fit=crop&q=80&w=600"),
            PresetImg("BMW M4", "https://images.unsplash.com/photo-1555215695-3004980ad54e?auto=format&fit=crop&q=80&w=600"),
            PresetImg("Golf GTI", "https://images.unsplash.com/photo-1541899481282-d53bffe3c35d?auto=format&fit=crop&q=80&w=600")
        )
        "Property" -> listOf(
            PresetImg("Phakalane Villa", "https://images.unsplash.com/photo-1564013799919-ab600027ffc6?auto=format&fit=crop&q=80&w=600"),
            PresetImg("Modern Kitchen", "https://images.unsplash.com/photo-1600585154340-be6161a56a0c?auto=format&fit=crop&q=80&w=600"),
            PresetImg("Cozy Flat", "https://images.unsplash.com/photo-1512917774080-9991f1c4c750?auto=format&fit=crop&q=80&w=600")
        )
        "Phones" -> listOf(
            PresetImg("iPhone 15", "https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?auto=format&fit=crop&q=80&w=600"),
            PresetImg("Android Phone", "https://images.unsplash.com/photo-1598327105666-5b89351aff97?auto=format&fit=crop&q=80&w=600")
        )
        "Furniture" -> listOf(
            PresetImg("Lounge Suite", "https://images.unsplash.com/photo-1555041469-a586c61ea9bc?auto=format&fit=crop&q=80&w=600"),
            PresetImg("Elegant Chair", "https://images.unsplash.com/photo-1567538096630-e0c55bd6374c?auto=format&fit=crop&q=80&w=600")
        )
        "Fashion" -> listOf(
            PresetImg("Wedding Dress", "https://images.unsplash.com/photo-1483985988355-763728e1935b?auto=format&fit=crop&q=80&w=600")
        )
        "Computers" -> listOf(
            PresetImg("EliteBook Laptop", "https://images.unsplash.com/photo-1496181130204-7552cc14ac41?auto=format&fit=crop&q=80&w=600")
        )
        "Agriculture" -> listOf(
            PresetImg("Farming Goat", "https://images.unsplash.com/photo-1524413840807-0c3cb6fa808d?auto=format&fit=crop&q=80&w=600")
        )
        "Services" -> listOf(
            PresetImg("Handyman tools", "https://images.unsplash.com/photo-1621905251189-08b45d6a269e?auto=format&fit=crop&q=80&w=600")
        )
        else -> listOf(
            PresetImg("General Marketplace", "https://images.unsplash.com/photo-1521737604893-d14cc237f11d?auto=format&fit=crop&q=80&w=600")
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            // Form title
            Text(
                text = if (adIdToEdit != null) "Edit Classified Ad" else "Post a Free Classified Ad",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Advertise your services or items completely free in Botswana.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp, bottom = 16.dp)
            )

            // User check warning if offline/unauth
            if (currentUser == null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Row(modifier = Modifier.padding(12.dp)) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Warning",
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "You are currently not logged in. You can browse ads, but you must register or log in before submitting advertisements.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            // Error Display Banner
            AnimatedVisibility(visible = errorMessage != null) {
                errorMessage?.let { error ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                            .background(
                                MaterialTheme.colorScheme.errorContainer,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Error",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Title Input
            OutlinedTextField(
                value = title,
                onValueChange = { 
                    title = it
                    errorMessage = null 
                },
                label = { Text("Advertisement Title *") },
                placeholder = { Text("e.g., Toyota Hilux GD-6 2020 for sale") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ad_title_input"),
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BotswanaBlue,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )
            Spacer(modifier = Modifier.height(14.dp))

            // Price & Category Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = priceStr,
                    onValueChange = { 
                        priceStr = it
                        errorMessage = null 
                    },
                    label = { Text("Price (Pula BWP) *") },
                    placeholder = { Text("e.g. 150") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    leadingIcon = { Text("P", fontWeight = FontWeight.Bold, color = BotswanaBlueDark) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("ad_price_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BotswanaBlue
                    )
                )

                // Category Selector Dropdown
                var categoryExpanded by remember { mutableStateOf(false) }
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = {},
                        label = { Text("Category *") },
                        readOnly = true,
                        trailingIcon = { 
                            IconButton(onClick = { categoryExpanded = true }) {
                                Icon(imageVector = Icons.Default.KeyboardArrowDown, contentDescription = null)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { categoryExpanded = true }
                            .testTag("ad_category_dropdown"),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BotswanaBlue
                        )
                    )
                    DropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.45f)
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    selectedCategory = cat
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))

            // Location & Contact Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Location Selector Dropdown
                var locationExpanded by remember { mutableStateOf(false) }
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = selectedLocation,
                        onValueChange = {},
                        label = { Text("Location/Town *") },
                        readOnly = true,
                        trailingIcon = { 
                            IconButton(onClick = { locationExpanded = true }) {
                                Icon(imageVector = Icons.Default.KeyboardArrowDown, contentDescription = null)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { locationExpanded = true }
                            .testTag("ad_location_dropdown"),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BotswanaBlue
                        )
                    )
                    DropdownMenu(
                        expanded = locationExpanded,
                        onDismissRequest = { locationExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.45f)
                    ) {
                        botswanaCities.forEach { city ->
                            DropdownMenuItem(
                                text = { Text(city) },
                                onClick = {
                                    selectedLocation = city
                                    locationExpanded = false
                                }
                            )
                        }
                    }
                }

                // Contact Phone Input
                OutlinedTextField(
                    value = contactNumber,
                    onValueChange = { 
                        contactNumber = it
                        errorMessage = null 
                    },
                    label = { Text("Contact Number *") },
                    placeholder = { Text("71554644") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    leadingIcon = { Icon(imageVector = Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("ad_contact_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BotswanaBlue
                    )
                )
            }
            Spacer(modifier = Modifier.height(14.dp))

            // Description input
            OutlinedTextField(
                value = description,
                onValueChange = { 
                    description = it
                    errorMessage = null 
                },
                label = { Text("Detailed Description *") },
                placeholder = { Text("Describe details, conditions, specs, delivery options, etc. in Botswana...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .testTag("ad_description_input"),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BotswanaBlue
                )
            )
            Spacer(modifier = Modifier.height(18.dp))

            // Attached Photo Previews Gallery
            Text(
                text = "Attached Photos (${attachedImages.size})",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(6.dp))

            if (attachedImages.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .border(1.dp, MaterialTheme.colorScheme.outline, shape = RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surface),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No photos attached yet. Pick from gallery or click presets below.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyRow(
                    contentPadding = PaddingValues(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(attachedImages) { imgUrl ->
                        Box(
                            modifier = Modifier
                                .size(70.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outline, shape = RoundedCornerShape(8.dp))
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(imgUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Attached photo",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            IconButton(
                                onClick = { attachedImages = attachedImages.filter { it != imgUrl } },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(20.dp)
                                    .background(Color.Black.copy(alpha = 0.6f), shape = RoundedCornerShape(bottomStart = 8.dp)),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove photo",
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Dynamic Action Panel: Pick Gallery Photo + Preset Catalog Photo Selection
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Add Pictures Easily",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Open Device Gallery
                        OutlinedButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1.2f)
                        ) {
                            Icon(imageVector = Icons.Default.AddAPhoto, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Device Gallery", fontSize = 11.sp)
                        }

                        // Direct Preset catalog text indicator
                        Text(
                            text = "OR select instant catalog photos:",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .weight(1f)
                                .align(Alignment.CenterVertically)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Horizontal Scrolling catalog presets matching category to attach instantly
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        items(presetImagesCatalog) { preset ->
                            val alreadyAttached = attachedImages.contains(preset.url)
                            Card(
                                modifier = Modifier
                                    .width(110.dp)
                                    .height(65.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable {
                                        if (alreadyAttached) {
                                            attachedImages = attachedImages.filter { it != preset.url }
                                        } else {
                                            attachedImages = attachedImages + preset.url
                                        }
                                    },
                                border = if (alreadyAttached) BorderStroke(2.dp, SuccessGreen) else null
                            ) {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    AsyncImage(
                                        model = preset.url,
                                        contentDescription = preset.name,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                Brush.verticalGradient(
                                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f))
                                                )
                                            )
                                    )
                                    Text(
                                        text = preset.name,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier
                                            .align(Alignment.BottomStart)
                                            .padding(4.dp)
                                    )

                                    if (alreadyAttached) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Selected",
                                            tint = SuccessGreen,
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(4.dp)
                                                .size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Bottom Submit Panel
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (adIdToEdit != null) {
                    // Delete Button
                    Button(
                        onClick = { showDeleteConfirm = true },
                        colors = ButtonDefaults.buttonColors(containerColor = ErrorRed, contentColor = Color.White),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .height(50.dp)
                            .testTag("delete_ad_button")
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Ad")
                    }
                }

                // Submit Form Button
                Button(
                    onClick = {
                        val parsedPrice = priceStr.toDoubleOrNull()
                        if (title.isBlank() || description.isBlank() || priceStr.isBlank() || contactNumber.isBlank()) {
                            errorMessage = "All marked (*) fields are required."
                            return@Button
                        }
                        if (parsedPrice == null || parsedPrice < 0) {
                            errorMessage = "Please enter a valid price."
                            return@Button
                        }
                        if (viewModel.formatAndValidateBotswanaPhone(contactNumber) == null) {
                            errorMessage = "Please enter a valid 8-digit Botswana contact mobile (e.g. 71554644)."
                            return@Button
                        }

                        isSubmitting = true
                        if (adIdToEdit == null) {
                            // Creating new ad
                            viewModel.postAd(
                                title = title,
                                description = description,
                                price = parsedPrice,
                                category = selectedCategory,
                                location = selectedLocation,
                                contactNumber = contactNumber,
                                images = attachedImages,
                                onSuccess = {
                                    isSubmitting = false
                                    onSuccess()
                                },
                                onError = {
                                    isSubmitting = false
                                    errorMessage = it
                                }
                            )
                        } else {
                            // Updating existing ad
                            viewModel.updateAd(
                                id = adIdToEdit,
                                title = title,
                                description = description,
                                price = parsedPrice,
                                category = selectedCategory,
                                location = selectedLocation,
                                contactNumber = contactNumber,
                                images = attachedImages,
                                onSuccess = {
                                    isSubmitting = false
                                    onSuccess()
                                },
                                onError = {
                                    isSubmitting = false
                                    errorMessage = it
                                }
                            )
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("submit_ad_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BotswanaBlue,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = if (isSubmitting) "Submitting..." else if (adIdToEdit != null) "Save Ad Changes" else "Post Ad Now (FREE)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    // Double-confirmation Dialog for listing deletion
    if (showDeleteConfirm && adIdToEdit != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Advertisement?") },
            text = { Text("Are you absolutely sure you want to remove this listing? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteAd(adIdToEdit)
                        showDeleteConfirm = false
                        onSuccess() // takes back
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                ) {
                    Text("Yes, Delete")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

data class PresetImg(val name: String, val url: String)
