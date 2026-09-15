package com.example.data.model

data class ParkZone(
    val id: String,
    val name: String,
    val rangeName: String,
    val gateLocation: String,
    val description: String,
    val landscape: String,
    val bestFor: String,
    val elephantSafariAllowed: Boolean,
    val jeepSafariAllowed: Boolean,
    val safariRouteLengthKm: Int,
    val keySightings: List<String>,
    val highlights: String,
    val coordinates: String
)

data class WildlifeSpecies(
    val id: String,
    val commonName: String,
    val scientificName: String,
    val localAssameseName: String,
    val category: WildlifeCategory,
    val isBigFive: Boolean,
    val iucnStatus: String,
    val populationEstimate: String,
    val description: String,
    val habitat: String,
    val bestZones: List<String>,
    val peakSpottingTime: String,
    val spottingLikelihood: SpottingLikelihood,
    val spottingTips: String,
    val fascinatingFact: String
)

enum class WildlifeCategory(val displayName: String) {
    ALL("All Wildlife"),
    BIG_FIVE("The Big Five"),
    MAMMALS("Mammals"),
    BIRDS("Birds"),
    REPTILES_AMPHIBIANS("Reptiles & Aquatic")
}

enum class SpottingLikelihood(val label: String) {
    VERY_HIGH("Very High (90%+)"),
    HIGH("High (70-85%)"),
    MODERATE("Moderate (40-60%)"),
    RARE("Rare / Elusive (10-25%)")
}

data class FloraSpecies(
    val id: String,
    val commonName: String,
    val scientificName: String,
    val localName: String,
    val type: String,
    val roleInEcosystem: String,
    val description: String,
    val bestObservedAt: String
)

data class SpottingTip(
    val id: String,
    val title: String,
    val category: String,
    val description: String,
    val practicalAdvice: String,
    val iconName: String
)

data class MapLocation(
    val id: String,
    val name: String,
    val zoneId: String,
    val type: MapLocationType,
    val normX: Float, // Normalized 0..1 for interactive canvas
    val normY: Float, // Normalized 0..1
    val description: String,
    val distanceKmFromKohora: Double,
    val keyAttraction: String
)

enum class MapLocationType(val label: String) {
    ENTRY_GATE("Entry Gate"),
    WATCH_TOWER("Watch Tower"),
    BEEL_WATERBODY("Beel & Wetlands"),
    RHINO_HOTSPOT("Rhino Hotspot"),
    SAFARI_TRACK("Safari Point"),
    FOREST_BEAT("Forest Office & Post")
}

data class Accommodation(
    val id: String,
    val name: String,
    val category: AccommodationCategory,
    val rating: Double,
    val pricePerNightInr: Int,
    val locationDescription: String,
    val distanceToGate: String,
    val nearestGate: String,
    val overview: String,
    val amenities: List<String>,
    val contactPhone: String,
    val roomTypes: List<RoomTypeOption>
)

data class RoomTypeOption(
    val name: String,
    val description: String,
    val priceInr: Int,
    val maxGuests: Int
)

enum class AccommodationCategory(val label: String) {
    ALL("All Stays"),
    LUXURY_ECO("Luxury Eco-Resort"),
    MID_RANGE("Heritage Lodge"),
    BUDGET_HOMESTAY("Homestay & Cottage"),
    GOVT_LODGE("Forest Dept. Lodge")
}

data class TravelSection(
    val id: String,
    val title: String,
    val subtitle: String,
    val content: String,
    val bulletPoints: List<String>
)
