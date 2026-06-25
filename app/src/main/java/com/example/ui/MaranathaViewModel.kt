package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.MaranathaDatabase
import com.example.data.database.entities.AdEntity
import com.example.data.database.entities.UserEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MaranathaViewModel(application: Application) : AndroidViewModel(application) {

    private val db = MaranathaDatabase.getDatabase(application)
    private val userDao = db.userDao()
    private val adDao = db.adDao()

    // Authentication States
    val currentUserFlow: StateFlow<UserEntity?> = userDao.getCurrentUserFlow()
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    val allUsersFlow: StateFlow<List<UserEntity>> = userDao.getAllUsersFlow()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // All Listings Flow
    val allAdsFlow: StateFlow<List<AdEntity>> = adDao.getAllAdsFlow()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Active User's Own Listings
    val userAdsFlow: StateFlow<List<AdEntity>> = currentUserFlow.flatMapLatest { user ->
        if (user != null) {
            adDao.getAdsBySellerPhoneFlow(user.phoneNumber)
        } else {
            flowOf(emptyList())
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Search and Filter States
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory = _selectedCategory.asStateFlow()

    private val _selectedLocation = MutableStateFlow("All")
    val selectedLocation = _selectedLocation.asStateFlow()

    private val _minPrice = MutableStateFlow<Double?>(null)
    val minPrice = _minPrice.asStateFlow()

    private val _maxPrice = MutableStateFlow<Double?>(null)
    val maxPrice = _maxPrice.asStateFlow()

    private val _sortByNewest = MutableStateFlow(true) // true: newest first, false: lowest price first
    val sortByNewest = _sortByNewest.asStateFlow()

    // Admin Mode State
    private val _isAdminMode = MutableStateFlow(false)
    val isAdminMode = _isAdminMode.asStateFlow()

    // Admin Session Credentials Security - Passcode verification
    fun verifyAdminPasscode(pin: String): Boolean {
        // Securely matches "1968", "2026" or "1234" to enter admin dashboard
        val isVerified = pin == "1968" || pin == "2026" || pin == "1234"
        if (isVerified) {
            _isAdminMode.value = true
        }
        return isVerified
    }

    fun exitAdminMode() {
        _isAdminMode.value = false
    }

    // Filtered Ads StateFlow
    val filteredAdsFlow: StateFlow<List<AdEntity>> = combine(
        allAdsFlow,
        combine(_searchQuery, _selectedCategory, _selectedLocation) { q, c, l -> Triple(q, c, l) },
        combine(_minPrice, _maxPrice, _sortByNewest) { minP, maxP, s -> Triple(minP, maxP, s) }
    ) { ads, textFilters, numberFilters ->
        val (query, category, location) = textFilters
        val (minP, maxP, newest) = numberFilters
        var list = ads

        // Search Query (title, product name, keywords in description, category, location)
        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            list = list.filter { ad ->
                ad.title.lowercase().contains(q) ||
                ad.description.lowercase().contains(q) ||
                ad.category.lowercase().contains(q) ||
                ad.location.lowercase().contains(q)
            }
        }

        // Category Filter
        if (category != "All") {
            list = list.filter { it.category.equals(category, ignoreCase = true) }
        }

        // Location Filter
        if (location != "All") {
            list = list.filter { it.location.equals(location, ignoreCase = true) }
        }

        // Price Filters
        if (minP != null) {
            list = list.filter { it.price >= minP }
        }
        if (maxP != null) {
            list = list.filter { it.price <= maxP }
        }

        // Sorting
        list = if (newest) {
            list.sortedByDescending { it.createdAt }
        } else {
            list.sortedBy { it.price }
        }

        list
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        prepopulateSampleDataIfNeeded()
    }

    private fun prepopulateSampleDataIfNeeded() {
        viewModelScope.launch {
            val existingAds = adDao.getAllAds()
            if (existingAds.isEmpty()) {
                // Populate elegant Botswana cities/towns and preloaded users
                val sampleUsers = listOf(
                    UserEntity("71554644", "Amogelang Manyatse", isBlocked = false, isLoggedIn = false),
                    UserEntity("72883441", "Kabo Segokgo", isBlocked = false, isLoggedIn = false),
                    UserEntity("74561122", "Lesego Mokgosi", isBlocked = false, isLoggedIn = false),
                    UserEntity("75322998", "Thabo Phiri", isBlocked = false, isLoggedIn = false)
                )
                sampleUsers.forEach {
                    try {
                        userDao.insertUser(it)
                    } catch (e: Exception) {
                        // User might already exist
                    }
                }

                val sampleAds = listOf(
                    AdEntity(
                        title = "Toyota Hilux 2.8 GD-6 Raider Double Cab",
                        description = "Excellent condition off-road bakkie. Low mileage (78,000 km), full service history with Toyota. Leather seats, canopy included, custom steel bumper. Price is negotiable. Selling due to relocation.",
                        price = 285000.0,
                        category = "Vehicles",
                        location = "Gaborone",
                        contactNumber = "71554644",
                        sellerUsername = "Amogelang Manyatse",
                        imageUrlsJson = "https://images.unsplash.com/photo-1533473359331-0135ef1b58bf?auto=format&fit=crop&q=80&w=600;https://images.unsplash.com/photo-1549399542-7e3f8b79c341?auto=format&fit=crop&q=80&w=600"
                    ),
                    AdEntity(
                        title = "Modern 3 Bedroom House in Phakalane",
                        description = "Spacious secure family home located in a gated community. Master bedroom en-suite, modern open-plan kitchen, lounge with fireplace, single garage, private swimming pool, and motorized gate. Close to Phakalane Golf Estate.",
                        price = 1200000.0,
                        category = "Property",
                        location = "Phakalane",
                        contactNumber = "72883441",
                        sellerUsername = "Kabo Segokgo",
                        imageUrlsJson = "https://images.unsplash.com/photo-1564013799919-ab600027ffc6?auto=format&fit=crop&q=80&w=600;https://images.unsplash.com/photo-1600585154340-be6161a56a0c?auto=format&fit=crop&q=80&w=600"
                    ),
                    AdEntity(
                        title = "Samsung Galaxy S24 Ultra 512GB",
                        description = "Titanium Gray color, brand new sealed in box. 12GB RAM, 512GB storage. Includes 12 months manufacturer warranty. Directly imported. Serious buyers only. Free delivery within Gaborone.",
                        price = 145000.0 / 10, // Let's keep it reasonable: P14,500
                        category = "Phones",
                        location = "Gaborone",
                        contactNumber = "74561122",
                        sellerUsername = "Lesego Mokgosi",
                        imageUrlsJson = "https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?auto=format&fit=crop&q=80&w=600"
                    ),
                    AdEntity(
                        title = "HP EliteBook 840 G8 Core i7",
                        description = "High-performance business laptop. Intel Core i7 11th Gen, 16GB DDR4 RAM, 512GB NVMe SSD, 14\" Full HD IPS anti-glare display, Backlit Keyboard, Fingerprint scanner. Perfect for coding, work, or university studies.",
                        price = 5800.0,
                        category = "Computers",
                        location = "Francistown",
                        contactNumber = "75322998",
                        sellerUsername = "Thabo Phiri", // Thabo Phiri
                        imageUrlsJson = "https://images.unsplash.com/photo-1496181130204-7552cc14ac41?auto=format&fit=crop&q=80&w=600"
                    ),
                    AdEntity(
                        title = "Elegant 4-Piece Leather Corner Sofa",
                        description = "Luxury leather modular couch set. Thick comfortable cushioning, solid wood framing. Available in chocolate brown. 100% genuine local leather. Handcrafted in Botswana.",
                        price = 8990.0,
                        category = "Furniture",
                        location = "Maun",
                        contactNumber = "71554644",
                        sellerUsername = "Amogelang Manyatse",
                        imageUrlsJson = "https://images.unsplash.com/photo-1555041469-a586c61ea9bc?auto=format&fit=crop&q=80&w=600"
                    ),
                    AdEntity(
                        title = "Custom Botswana Traditional Attire (Leteitshi)",
                        description = "Beautifully crafted custom traditional dresses, skirts, and matching shirts for weddings and traditional ceremonies. Made to measure. Premium fabric options are available.",
                        price = 950.0,
                        category = "Fashion",
                        location = "Serowe",
                        contactNumber = "74561122",
                        sellerUsername = "Lesego Mokgosi",
                        imageUrlsJson = "https://images.unsplash.com/photo-1483985988355-763728e1935b?auto=format&fit=crop&q=80&w=600"
                    ),
                    AdEntity(
                        title = "Professional Residential Plumbing & Leak Detection",
                        description = "Certified plumbing services in Botswana. Leak detection, bathroom remodeling, high-pressure geyser installation, blocked drains clearing, and general home repair services. 24/7 emergency response.",
                        price = 250.0,
                        category = "Services",
                        location = "Molepolole",
                        contactNumber = "75322998",
                        sellerUsername = "Thabo Phiri",
                        imageUrlsJson = "https://images.unsplash.com/photo-1621905251189-08b45d6a269e?auto=format&fit=crop&q=80&w=600"
                    ),
                    AdEntity(
                        title = "Pure Boer Goat Rams for Breeding",
                        description = "High-quality Boer Goat breeding rams. Vaccinated, dewormed, healthy, and direct from a certified ranch in Kanye. Age range 14-18 months. Great genetics for improving herd size and meat quality.",
                        price = 2800.0,
                        category = "Agriculture",
                        location = "Kanye",
                        contactNumber = "72883441",
                        sellerUsername = "Kabo Segokgo",
                        imageUrlsJson = "https://images.unsplash.com/photo-1524413840807-0c3cb6fa808d?auto=format&fit=crop&q=80&w=600"
                    )
                )
                sampleAds.forEach { adDao.insertAd(it) }
            }
        }
    }

    // Setters for filters
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    fun selectLocation(location: String) {
        _selectedLocation.value = location
    }

    fun setPriceRange(min: Double?, max: Double?) {
        _minPrice.value = min
        _maxPrice.value = max
    }

    fun toggleSortOrder() {
        _sortByNewest.value = !_sortByNewest.value
    }

    fun clearAllFilters() {
        _searchQuery.value = ""
        _selectedCategory.value = "All"
        _selectedLocation.value = "All"
        _minPrice.value = null
        _maxPrice.value = null
        _sortByNewest.value = true
    }

    // AUTH ACTIONS
    fun registerUser(username: String, phone: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val trimmedPhone = formatAndValidateBotswanaPhone(phone)
            if (trimmedPhone == null) {
                onError("Invalid mobile number. Must be an 8-digit Botswana number starting with 7 (e.g., 71554644).")
                return@launch
            }
            if (username.trim().length < 3) {
                onError("Username must be at least 3 characters.")
                return@launch
            }

            val existing = userDao.getUserByPhone(trimmedPhone)
            if (existing != null) {
                onError("An account with this phone number already exists. Please log in.")
                return@launch
            }

            // Unlog current users first
            userDao.logoutAllUsers()

            val newUser = UserEntity(
                phoneNumber = trimmedPhone,
                username = username.trim(),
                isBlocked = false,
                isLoggedIn = true
            )
            userDao.insertUser(newUser)
            onSuccess()
        }
    }

    fun loginUser(phone: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val trimmedPhone = formatAndValidateBotswanaPhone(phone)
            if (trimmedPhone == null) {
                onError("Invalid mobile number. Please enter an 8-digit Botswana number.")
                return@launch
            }

            val user = userDao.getUserByPhone(trimmedPhone)
            if (user == null) {
                onError("No account found with this phone number. Please register first.")
                return@launch
            }

            if (user.isBlocked) {
                onError("This account has been blocked by the administrator.")
                return@launch
            }

            // Log in user
            userDao.logoutAllUsers()
            userDao.updateUser(user.copy(isLoggedIn = true))
            onSuccess()
        }
    }

    fun logout() {
        viewModelScope.launch {
            userDao.logoutAllUsers()
        }
    }

    // CLASSIFIED ADS ACTIONS (FREE & INSTANT)
    fun postAd(
        title: String,
        description: String,
        price: Double,
        category: String,
        location: String,
        contactNumber: String,
        images: List<String>,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val user = currentUserFlow.value
            if (user == null) {
                onError("You must be logged in to post advertisements.")
                return@launch
            }
            if (user.isBlocked) {
                onError("Your account is blocked and cannot post advertisements.")
                return@launch
            }
            if (title.trim().isBlank() || description.trim().isBlank()) {
                onError("Title and description cannot be empty.")
                return@launch
            }
            if (price < 0) {
                onError("Price must be a valid positive number.")
                return@launch
            }

            val contactVal = formatAndValidateBotswanaPhone(contactNumber) ?: user.phoneNumber

            val ad = AdEntity(
                title = title.trim(),
                description = description.trim(),
                price = price,
                category = category,
                location = location,
                contactNumber = contactVal,
                sellerUsername = user.username,
                imageUrlsJson = images.joinToString(";")
            )
            adDao.insertAd(ad)
            onSuccess()
        }
    }

    fun updateAd(
        id: Long,
        title: String,
        description: String,
        price: Double,
        category: String,
        location: String,
        contactNumber: String,
        images: List<String>,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val ad = adDao.getAdById(id)
            if (ad == null) {
                onError("Ad not found.")
                return@launch
            }
            if (title.trim().isBlank() || description.trim().isBlank()) {
                onError("Title and description cannot be empty.")
                return@launch
            }

            val contactVal = formatAndValidateBotswanaPhone(contactNumber) ?: ad.contactNumber

            val updated = ad.copy(
                title = title.trim(),
                description = description.trim(),
                price = price,
                category = category,
                location = location,
                contactNumber = contactVal,
                imageUrlsJson = images.joinToString(";")
            )
            adDao.updateAd(updated)
            onSuccess()
        }
    }

    fun deleteAd(id: Long) {
        viewModelScope.launch {
            adDao.deleteAdById(id)
        }
    }

    // ADMIN PRIVILEGED ACTIONS
    fun toggleBlockUser(phone: String, isBlocked: Boolean) {
        viewModelScope.launch {
            val user = userDao.getUserByPhone(phone)
            if (user != null) {
                userDao.updateUser(user.copy(isBlocked = isBlocked, isLoggedIn = if (isBlocked) false else user.isLoggedIn))
            }
        }
    }

    fun deleteAdAdmin(id: Long) {
        viewModelScope.launch {
            adDao.deleteAdById(id)
        }
    }

    // Utilities
    fun formatAndValidateBotswanaPhone(phone: String): String? {
        val digits = phone.filter { it.isDigit() }
        if (digits.length == 8 && digits.startsWith("7")) {
            return digits
        }
        if (digits.length == 11 && digits.startsWith("2677")) {
            return digits.substring(3)
        }
        if (digits.length == 12 && digits.startsWith("2677")) {
            return digits.substring(4) // supports +2677...
        }
        // If just 8 digits and starts with 7, that is perfect for Botswana
        return if (digits.length == 8) digits else null
    }
}
