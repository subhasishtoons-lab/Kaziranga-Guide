package com.example.data.repository

import com.example.data.database.AccommodationBookingDao
import com.example.data.database.AccommodationBookingEntity
import com.example.data.database.SafariBookingDao
import com.example.data.database.SafariBookingEntity
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class KazirangaRepository(
    private val safariDao: SafariBookingDao,
    private val accommodationDao: AccommodationBookingDao
) {
    val safariBookings: Flow<List<SafariBookingEntity>> = safariDao.getAllSafariBookings()
    val accommodationBookings: Flow<List<AccommodationBookingEntity>> = accommodationDao.getAllAccommodationBookings()

    suspend fun bookSafari(
        safariType: String,
        zoneName: String,
        date: String,
        timeSlot: String,
        numAdults: Int,
        numChildren: Int,
        isForeignNational: Boolean,
        includeCameraPermit: Boolean,
        leadName: String,
        leadPhone: String,
        idProofType: String,
        idProofNumber: String
    ): Long {
        val baseFeePerPerson = if (isForeignNational) 2000 else 400
        val vehicleGuideFee = if (safariType == "ELEPHANT") 1250 else 2400
        val cameraFee = if (includeCameraPermit) 200 else 0
        val totalAmount = (numAdults * baseFeePerPerson) + (numChildren * (baseFeePerPerson / 2)) + vehicleGuideFee + cameraFee

        val refCode = "KZ-SAF-${(10000..99999).random()}"
        val entity = SafariBookingEntity(
            bookingReference = refCode,
            safariType = safariType,
            zoneName = zoneName,
            date = date,
            timeSlot = timeSlot,
            numAdults = numAdults,
            numChildren = numChildren,
            isForeignNational = isForeignNational,
            includeCameraPermit = includeCameraPermit,
            leadGuestName = leadName,
            leadGuestPhone = leadPhone,
            idProofType = idProofType,
            idProofNumber = idProofNumber,
            totalAmountInr = totalAmount,
            status = "CONFIRMED"
        )
        return safariDao.insertSafariBooking(entity)
    }

    suspend fun cancelSafariBooking(id: Long) {
        safariDao.cancelSafariBooking(id)
    }

    suspend fun bookAccommodation(
        propertyId: String,
        propertyName: String,
        roomTypeName: String,
        checkInDate: String,
        checkOutDate: String,
        numNights: Int,
        numGuests: Int,
        pricePerNight: Int,
        guestName: String,
        guestEmail: String,
        guestPhone: String,
        specialRequests: String
    ): Long {
        val totalAmount = pricePerNight * numNights
        val refCode = "KZ-HTL-${(10000..99999).random()}"
        val entity = AccommodationBookingEntity(
            bookingReference = refCode,
            propertyId = propertyId,
            propertyName = propertyName,
            roomTypeName = roomTypeName,
            checkInDate = checkInDate,
            checkOutDate = checkOutDate,
            numNights = numNights,
            numGuests = numGuests,
            guestName = guestName,
            guestEmail = guestEmail,
            guestPhone = guestPhone,
            totalAmountInr = totalAmount,
            specialRequests = specialRequests,
            status = "CONFIRMED"
        )
        return accommodationDao.insertAccommodationBooking(entity)
    }

    suspend fun cancelAccommodationBooking(id: Long) {
        accommodationDao.cancelAccommodationBooking(id)
    }

    // Static Park Zones Knowledge
    fun getParkZones(): List<ParkZone> = listOf(
        ParkZone(
            id = "central_kohora",
            name = "Central Zone (Kohora)",
            rangeName = "Central Range",
            gateLocation = "Mihimukh Gate, Kohora (NH 715)",
            description = "The most visited and accessible range of Kaziranga. Features sprawling wetlands, lush water bodies (beels), and riverine floodplains teeming with wildlife.",
            landscape = "Mihimukh grassland, Donga beel, marshy swamps, tropical semi-evergreen pockets",
            bestFor = "Highest likelihood of Elephant Safaris, Rhino photography at close distance, and scenic watchtowers.",
            elephantSafariAllowed = true,
            jeepSafariAllowed = true,
            safariRouteLengthKm = 28,
            keySightings = listOf("One-Horned Rhinoceros", "Swamp Deer (Barasingha)", "Wild Water Buffalo", "Asian Elephant", "Otters"),
            highlights = "Mihimukh entry tower, Donga view point, Kathpara watch tower",
            coordinates = "26°35'24\"N 93°21'36\"E"
        ),
        ParkZone(
            id = "western_bagori",
            name = "Western Zone (Bagori)",
            rangeName = "Western Range",
            gateLocation = "Bagori Gate, 13 km west of Kohora (NH 715)",
            description = "Famed for having the highest concentration of Greater One-Horned Rhinos in the world. Ideal for travelers with limited time seeking guaranteed sightings.",
            landscape = "Open short elephant grass, Donga beel, sandy river channels, mudflats",
            bestFor = "Magnificent Rhino groups, Wild Water Buffaloes, and early morning Elephant rides.",
            elephantSafariAllowed = true,
            jeepSafariAllowed = true,
            safariRouteLengthKm = 24,
            keySightings = listOf("One-Horned Rhinoceros (Dense)", "Wild Water Buffalo", "Hog Deer", "Migratory Geese", "Bengal Florican"),
            highlights = "Sohola beel vantage, Donga wetland trail, Elephant boarding tower",
            coordinates = "26°34'12\"N 93°14'48\"E"
        ),
        ParkZone(
            id = "eastern_agoratoli",
            name = "Eastern Zone (Agoratoli)",
            rangeName = "Eastern Range",
            gateLocation = "Agoratoli Gate, 22 km east of Kohora",
            description = "The wild, untouched birdwatcher's paradise along the Brahmaputra River. Thick woodlands transition into colossal water bodies hosting thousands of migratory fowl.",
            landscape = "Deep alluvial woodland, Sohola Beel, Brahmaputra riverbanks, dense bamboo groves",
            bestFor = "Ornithologists, Royal Bengal Tiger tracking, Asian Elephant herds, and solitude away from crowds.",
            elephantSafariAllowed = false,
            jeepSafariAllowed = true,
            safariRouteLengthKm = 36,
            keySightings = listOf("Spot-billed Pelican", "Greater Adjutant", "Pallas's Fish Eagle", "Royal Bengal Tiger", "Asian Elephant"),
            highlights = "Sohola Beel bird sanctuary, Brahmaputra river bank lookout point",
            coordinates = "26°39'45\"N 93°27'10\"E"
        ),
        ParkZone(
            id = "burapahar_ghorakati",
            name = "Burapahar Zone (Ghorakati)",
            rangeName = "Burapahar Range",
            gateLocation = "Ghorakati Gate, 33 km west of Kohora",
            description = "Characterized by rolling hills meeting the plains, adjacent to the lush Karbi Anglong forest corridor. The only zone offering guided nature trekking trails alongside jeep safaris.",
            landscape = "Undulating hilly woodlands, Panbari reserve edge, rocky streams, dense canopy",
            bestFor = "Primates like the Hoolock Gibbon and Capped Langur, guided walking treks, and birding.",
            elephantSafariAllowed = false,
            jeepSafariAllowed = true,
            safariRouteLengthKm = 20,
            keySightings = listOf("Hoolock Gibbon", "Capped Langur", "Tiger tracks", "Great Hornbill", "Barking Deer"),
            highlights = "Hills backdrop, Rhimghat viewpoint, approved walking trek trail",
            coordinates = "26°32'15\"N 93°05'20\"E"
        )
    )

    // Comprehensive Fauna & Flora Catalog
    fun getWildlifeSpecies(): List<WildlifeSpecies> = listOf(
        WildlifeSpecies(
            id = "rhino",
            commonName = "Great Indian One-Horned Rhinoceros",
            scientificName = "Rhinoceros unicornis",
            localAssameseName = "Gorh (গঁড়)",
            category = WildlifeCategory.BIG_FIVE,
            isBigFive = true,
            iucnStatus = "Vulnerable (Conservation Success)",
            populationEstimate = "2,613+ individuals (over 70% of world total)",
            description = "The iconic prehistoric titan of Assam. Identified by its single black horn (20-60 cm) and armor-plated grey skin folds resembling riveted armor plates.",
            habitat = "Alluvial elephant grasslands, marshy riverine depressions, and wallowing beels.",
            bestZones = listOf("Western (Bagori)", "Central (Kohora)"),
            peakSpottingTime = "Early morning (06:00 - 08:30 AM) and late afternoon (03:30 - 05:00 PM)",
            spottingLikelihood = SpottingLikelihood.VERY_HIGH,
            spottingTips = "Look around mud wallows and shallow beels during warmer midday hours, and open grassland patches during dawn.",
            fascinatingFact = "Kaziranga's rhino population recovered from a mere 12 individuals in 1905 to over 2,600 today through fierce anti-poaching vigilance!"
        ),
        WildlifeSpecies(
            id = "tiger",
            commonName = "Royal Bengal Tiger",
            scientificName = "Panthera tigris tigris",
            localAssameseName = "Dhekia-potia Bagh (ঢেকীয়াপটীয়া বাঘ)",
            category = WildlifeCategory.BIG_FIVE,
            isBigFive = true,
            iucnStatus = "Endangered",
            populationEstimate = "121+ individuals (One of the highest densities globally)",
            description = "The apex predator of the Brahmaputra floodplain. Kaziranga hosts one of the highest tiger densities of any protected area on earth (1 tiger per 5 sq km).",
            habitat = "Dense elephant grass corridors, riverine woodlands, and sandy river islands (chars).",
            bestZones = listOf("Eastern (Agoratoli)", "Central (Kohora)"),
            peakSpottingTime = "Dawn (05:45 - 07:00 AM) & Dusk (04:30 - 05:30 PM)",
            spottingLikelihood = SpottingLikelihood.MODERATE,
            spottingTips = "Listen for alarm calls of Hog Deer and Sambar. Check dusty jeep safari tracks for fresh pugmarks at dawn.",
            fascinatingFact = "Because of extremely tall grass (up to 5 meters high), Kaziranga tigers are masters of camouflage despite their vibrant stripes."
        ),
        WildlifeSpecies(
            id = "elephant",
            commonName = "Asian Elephant",
            scientificName = "Elephas maximus",
            localAssameseName = "Haati (হাতী)",
            category = WildlifeCategory.BIG_FIVE,
            isBigFive = true,
            iucnStatus = "Endangered",
            populationEstimate = "1,100+ individuals",
            description = "Majestic megaherbivores moving in intimate matriarchal family herds. They migrate between Kaziranga's lowlands and the elevated Karbi Anglong hills during floods.",
            habitat = "Grasslands, bamboo forests, riverbanks, and agricultural corridor edges.",
            bestZones = listOf("Central (Kohora)", "Burapahar (Ghorakati)", "Eastern (Agoratoli)"),
            peakSpottingTime = "Late afternoon bath times in beels (03:00 - 05:00 PM)",
            spottingLikelihood = SpottingLikelihood.VERY_HIGH,
            spottingTips = "Watch shallow river bends where family herds congregate to bathe, spray mud, and feed on succulent elephant grasses.",
            fascinatingFact = "Kaziranga elephants maintain vital animal corridors under NH 715 to escape annual Brahmaputra monsoon inundations."
        ),
        WildlifeSpecies(
            id = "water_buffalo",
            commonName = "Wild Water Buffalo",
            scientificName = "Bubalus arnee",
            localAssameseName = "Bonoria Moh (বনৰীয়া ম'হ)",
            category = WildlifeCategory.BIG_FIVE,
            isBigFive = true,
            iucnStatus = "Endangered",
            populationEstimate = "1,600+ individuals (Largest wild population)",
            description = "A colossal bovine with massive crescent-shaped horns spanning up to 2 meters. Considerably larger and more aggressive than domestic water buffaloes.",
            habitat = "Deep marshes, muddy floodplains, and reed-choked wetlands.",
            bestZones = listOf("Western (Bagori)", "Central (Kohora)"),
            peakSpottingTime = "Morning (07:00 - 10:00 AM) wallowing in marshes",
            spottingLikelihood = SpottingLikelihood.VERY_HIGH,
            spottingTips = "Often seen grazing submerged shoulder-deep in water hyacinth and lotus beds in Sohola and Donga beels.",
            fascinatingFact = "Kaziranga holds the purest remaining genetic stock of Wild Water Buffalo in Asia."
        ),
        WildlifeSpecies(
            id = "swamp_deer",
            commonName = "Eastern Swamp Deer (Barasingha)",
            scientificName = "Rucervus duvaucelii ranjitsinhi",
            localAssameseName = "Dolhorina (দলহৰিণা)",
            category = WildlifeCategory.BIG_FIVE,
            isBigFive = true,
            iucnStatus = "Vulnerable / Subspecies Critically Rare",
            populationEstimate = "800+ individuals (Endemic subspecies)",
            description = "A stunning deer species known for stags possessing 10 to 14 antler tines (barasingha means '12-tined'). The Eastern subspecies is uniquely adapted to marsh life.",
            habitat = "Waterlogged grasslands, floating mats of vegetation (phumdis), and swamps.",
            bestZones = listOf("Central (Kohora)", "Western (Bagori)"),
            peakSpottingTime = "Early morning mist and sunrise (06:00 - 08:00 AM)",
            spottingLikelihood = SpottingLikelihood.HIGH,
            spottingTips = "Scan open swamp expanses from Mihimukh and Donga watchtowers with binoculars.",
            fascinatingFact = "Kaziranga is the final stronghold of the sub-species 'ranjitsinhi', virtually extinct outside Assam!"
        ),
        WildlifeSpecies(
            id = "hoolock_gibbon",
            commonName = "Western Hoolock Gibbon",
            scientificName = "Hoolock hoolock",
            localAssameseName = "Holou Bandor (হলৌ বান্দৰ)",
            category = WildlifeCategory.MAMMALS,
            isBigFive = false,
            iucnStatus = "Endangered",
            populationEstimate = "Scattered canopy groups in Panbari & Burapahar",
            description = "India's only ape species! Monogamous, tailless, and celebrated for their melodious territorial duet calls echoing through the misty jungle canopy.",
            habitat = "Dense contiguous evergreen and broadleaf forest canopy.",
            bestZones = listOf("Burapahar (Ghorakati)", "Panbari Reserve"),
            peakSpottingTime = "Morning vocalization hours (06:30 - 09:00 AM)",
            spottingLikelihood = SpottingLikelihood.MODERATE,
            spottingTips = "Locate them by their distinctive loud, whooping calls carrying up to 1 km in Burapahar trekking trails.",
            fascinatingFact = "They swing between tree branches via brachiation at speeds exceeding 50 km/h!"
        ),
        WildlifeSpecies(
            id = "great_hornbill",
            commonName = "Great Indian Hornbill",
            scientificName = "Buceros bicornis",
            localAssameseName = "Dhelesh (ধনেশ)",
            category = WildlifeCategory.BIRDS,
            isBigFive = false,
            iucnStatus = "Vulnerable",
            populationEstimate = "Healthy breeding forest populations",
            description = "A magnificent forest bird with an enormous yellow casque atop its curved bill. Its heavy wings generate a distinctive roaring whoosh audible hundreds of meters away.",
            habitat = "Tall fig trees, tropical moist deciduous forest, fruiting canopy.",
            bestZones = listOf("Burapahar", "Agoratoli", "Panbari Forest"),
            peakSpottingTime = "Morning (07:00 - 10:00 AM) feeding in fruiting fig trees",
            spottingLikelihood = SpottingLikelihood.HIGH,
            spottingTips = "Watch tall Ficus (fig) trees laden with ripe fruit near forest fringes.",
            fascinatingFact = "Females seal themselves inside tree hollows with mud and droppings for months while incubating eggs, fed entirely by the male."
        ),
        WildlifeSpecies(
            id = "bengal_florican",
            commonName = "Bengal Florican",
            scientificName = "Houbaropsis bengalensis",
            localAssameseName = "Ulu Mora (উলুম'ৰা)",
            category = WildlifeCategory.BIRDS,
            isBigFive = false,
            iucnStatus = "Critically Endangered",
            populationEstimate = "Fewer than 400 left worldwide; Kaziranga is key sanctuary",
            description = "One of the world's rarest bustard birds. Famed for the male's spectacular courtship aerial leap display over tall grassland.",
            habitat = "Pristine lowland alluvial grasslands with scattered short patches.",
            bestZones = listOf("Bagori (Western)", "Kohora (Central)"),
            peakSpottingTime = "Early spring breeding season mornings (March - April)",
            spottingLikelihood = SpottingLikelihood.RARE,
            spottingTips = "Requires high-magnification binoculars or spotting scope over grassland plains.",
            fascinatingFact = "Males leap up to 2 meters into the air, fluttering wings like a helicopter to attract mates!"
        ),
        WildlifeSpecies(
            id = "river_dolphin",
            commonName = "Ganges River Dolphin",
            scientificName = "Platanista gangetica",
            localAssameseName = "Xihu (শিহু)",
            category = WildlifeCategory.REPTILES_AMPHIBIANS,
            isBigFive = false,
            iucnStatus = "Endangered",
            populationEstimate = "Resident pod along Brahmaputra boundary",
            description = "A freshwater dolphin that navigates exclusively by echolocation because its eyes lack a crystalline lens, making it functionally blind.",
            habitat = "Deep pools and eddies of the mighty Brahmaputra river along northern Kaziranga.",
            bestZones = listOf("Agoratoli (Eastern - Brahmaputra viewpoint)", "Bhomoraguri Ghat"),
            peakSpottingTime = "Calm morning and sunset waters (07:00 AM or 04:30 PM)",
            spottingLikelihood = SpottingLikelihood.MODERATE,
            spottingTips = "Watch river bends where tributaries meet the Brahmaputra; look for quick surfacing blows lasting 1-2 seconds.",
            fascinatingFact = "India's official National Aquatic Animal and the apex bio-indicator of river water health."
        )
    )

    fun getFloraSpecies(): List<FloraSpecies> = listOf(
        FloraSpecies(
            id = "elephant_grass",
            commonName = "Tall Elephant Grass",
            scientificName = "Saccharum elephantinum / Arundo donax",
            localName = "Ikora / Khagori / Nal (ইকৰা / খাগৰি)",
            type = "Tall Perennial Grass",
            roleInEcosystem = "Primary food source for Rhinos and Elephants; crucial shelter for Tigers and Floricans.",
            description = "Towering grass species growing between 3 and 5 meters in height. Forms the dense grassland sea characteristic of the floodplains.",
            bestObservedAt = "Mihimukh and Bagori grassland tracks"
        ),
        FloraSpecies(
            id = "kumbhi",
            commonName = "Wild Guava / Kumbhi",
            scientificName = "Careya arborea",
            localName = "Kumbhi (কুম্ভী)",
            type = "Deciduous Broadleaf Tree",
            roleInEcosystem = "Bark and fruit consumed by wildlife; leaves used traditionally for healing.",
            description = "Medium-sized tree with thick corky fire-resistant bark that survives annual grassland burns and monsoon inundations.",
            bestObservedAt = "Central Kohora savanna boundary"
        ),
        FloraSpecies(
            id = "semul",
            commonName = "Silk Cotton Tree / Red Kapok",
            scientificName = "Bombax ceiba",
            localName = "Ximolu (শিমলু)",
            type = "Canopy Tree with Buttress Roots",
            roleInEcosystem = "Vibrant crimson blossoms in Feb-March feed bees, hornbills, parakeets, and monkeys.",
            description = "Immense trees with spiny trunks and sprawling horizontal branches, frequently used as nesting perches by Pallas's Fish Eagles and Vultures.",
            bestObservedAt = "Agoratoli riverine forest and Ghorakati foothills"
        ),
        FloraSpecies(
            id = "water_hyacinth",
            commonName = "Water Hyacinth",
            scientificName = "Eichhornia crassipes",
            localName = "Pani Meteka (পানী মেটেকা)",
            type = "Aquatic Plant",
            roleInEcosystem = "Floating mats support swamp deer, wild buffalo grazing, and roosting water birds.",
            description = "Prolific aquatic plant with violet blossoms covering the tranquil surfaces of beels and oxbow lakes.",
            bestObservedAt = "Donga Beel and Sohola Beel"
        ),
        FloraSpecies(
            id = "elephant_apple",
            commonName = "Elephant Apple",
            scientificName = "Dillenia indica",
            localName = "Ou Tenga (ঔ টেঙা)",
            type = "Evergreen Sub-canopy Tree",
            roleInEcosystem = "Heavy greenish-yellow aromatic fruits are a cherished delicacy of wild elephants.",
            description = "Beautiful tree with serrated leaves, large white flowers, and hard round fruits adapted for dispersal by megafauna.",
            bestObservedAt = "Burapahar woodland fringes"
        )
    )

    fun getSpottingTips(): List<SpottingTip> = listOf(
        SpottingTip(
            id = "timing",
            title = "Golden Dawn vs Late Afternoon",
            category = "Safari Timing",
            description = "Rhinos and tigers are crepuscular—most active when ambient temperature is low. Dawn safaris (06:00 - 08:30 AM) offer atmospheric mist and active grazing.",
            practicalAdvice = "Book the 1st shift elephant ride (05:30 AM) in Bagori or Kohora for mist-shrouded rhinos just meters away.",
            iconName = "wb_sunny"
        ),
        SpottingTip(
            id = "zone_strategy",
            title = "Pairing the Right Zones",
            category = "Zone Strategy",
            description = "Don't visit only one zone! Combine Bagori (highest rhino density) with Agoratoli (serene birding & tiger corridor) and Kohora (mixed wetlands).",
            practicalAdvice = "Plan at least 2 full days: morning Bagori elephant safari, afternoon Kohora jeep safari, next day Agoratoli birding jeep safari.",
            iconName = "explore"
        ),
        SpottingTip(
            id = "clothing",
            title = "Jungle Camouflage & Dust Gear",
            category = "Dress Code",
            description = "Wild animals possess sharp eyesight and can detect bright, neon, or stark white apparel from hundreds of meters away.",
            practicalAdvice = "Wear neutral earth tones (khaki, olive green, taupe, brown). Bring a dust scarf/buff and sunglasses as jeep tracks get dusty.",
            iconName = "checkroom"
        ),
        SpottingTip(
            id = "photography",
            title = "Optics & Lens Selection",
            category = "Photography",
            description = "Due to safety regulations, jeeps remain on designated forest tracks. Animals can range from 10 meters to 300 meters away.",
            practicalAdvice = "A 100-400mm or 150-600mm telephoto lens is ideal. Carry 8x42 or 10x42 binoculars for scanning distant water beels.",
            iconName = "photo_camera"
        ),
        SpottingTip(
            id = "silence",
            title = "Silence & Engine Off Protocol",
            category = "Park Etiquette",
            description = "Wildlife senses engine noise and loud human chatter long before you spot them, causing them to retreat into tall grass.",
            practicalAdvice = "Politely request your safari driver to cut the engine when near a sighting. Speak in quiet whispers.",
            iconName = "volume_off"
        )
    )

    // Interactive Map POIs
    fun getMapLocations(): List<MapLocation> = listOf(
        MapLocation("poi_mihimukh", "Mihimukh Gate & Office", "central_kohora", MapLocationType.ENTRY_GATE, 0.48f, 0.72f, "Main tourist entrance, ticket counter, and souvenir hub in Kohora.", 0.0, "Entry Gateway & Elephant Stand"),
        MapLocation("poi_donga_tower", "Donga Watch Tower", "central_kohora", MapLocationType.WATCH_TOWER, 0.44f, 0.52f, "Overlooks vast marshy wetland with resident rhino family and water buffaloes.", 4.5, "Rhinos bathing, raptor perches"),
        MapLocation("poi_kathpara", "Kathpara View Point", "central_kohora", MapLocationType.WATCH_TOWER, 0.53f, 0.42f, "Elevated wooden observatory above swamp deer grazing meadows.", 7.2, "Barasingha herds and fishing eagles"),
        MapLocation("poi_bagori_gate", "Bagori Entry Gate", "western_bagori", MapLocationType.ENTRY_GATE, 0.28f, 0.76f, "Western gate offering quick access to high-density rhino zones.", 13.0, "Elephant boarding ramp"),
        MapLocation("poi_bagori_beel", "Donga & Sohola Beel", "western_bagori", MapLocationType.BEEL_WATERBODY, 0.22f, 0.48f, "Massive waterbody filled with wild water buffalo, otters, and whistling ducks.", 16.5, "Massive buffalo horns & rhino pairs"),
        MapLocation("poi_agoratoli_gate", "Agoratoli Gate", "eastern_agoratoli", MapLocationType.ENTRY_GATE, 0.76f, 0.68f, "Gateway to tranquil eastern forests and birding sanctuaries.", 21.0, "Forest guide checkpoint"),
        MapLocation("poi_sohola_birding", "Sohola Birding Hotspot", "eastern_agoratoli", MapLocationType.BEEL_WATERBODY, 0.82f, 0.44f, "Pelican breeding colony, storks, and fish eagles.", 27.5, "Spot-billed pelican nests"),
        MapLocation("poi_brahmaputra_view", "Brahmaputra River Ghat", "eastern_agoratoli", MapLocationType.RHINO_HOTSPOT, 0.88f, 0.25f, "Where the forest meets the mighty river; Ganges river dolphin viewing spot.", 32.0, "Freshwater river dolphin pods"),
        MapLocation("poi_ghorakati_gate", "Ghorakati Gate (Burapahar)", "burapahar_ghorakati", MapLocationType.ENTRY_GATE, 0.12f, 0.82f, "Entry for Burapahar hilly range and Panbari trekking corridor.", 33.0, "Trekking permit office"),
        MapLocation("poi_rhimghat", "Rhimghat Forest Post", "burapahar_ghorakati", MapLocationType.FOREST_BEAT, 0.08f, 0.58f, "Remote forest post near Karbi Anglong animal migration corridor.", 38.0, "Hoolock gibbon canopy calls")
    )

    // Accommodations
    fun getAccommodations(): List<Accommodation> = listOf(
        Accommodation(
            id = "diphlu_river_lodge",
            name = "Diphlu River Lodge",
            category = AccommodationCategory.LUXURY_ECO,
            rating = 4.9,
            pricePerNightInr = 18500,
            locationDescription = "Set right along the banks of the Diphlu River, bordering the national park.",
            distanceToGate = "3.5 km to Bagori Gate / 9 km to Kohora Gate",
            nearestGate = "Bagori Gate",
            overview = "Famed luxury eco-resort that hosted British Royalty (Prince William and Kate Middleton). Elevated thatched cottages built on bamboo stilts overlooking paddy fields and park forest.",
            amenities = listOf("In-house Naturalists", "Gourmet Assamese Dining", "Riverbank Sundowners", "Free Wi-Fi", "Safari Jeep Service", "Verandah River View"),
            contactPhone = "+91 94350 86005",
            roomTypes = listOf(
                RoomTypeOption("River-Facing Stilt Cottage", "Spacious natural wood cottage directly gazing onto Diphlu river wildlife.", 18500, 2),
                RoomTypeOption("Paddy View Cottage", "Peaceful cottage framing rural paddy fields and birding perches.", 16000, 2)
            )
        ),
        Accommodation(
            id = "iora_the_retreat",
            name = "IORA - The Retreat",
            category = AccommodationCategory.LUXURY_ECO,
            rating = 4.7,
            pricePerNightInr = 9500,
            locationDescription = "Sprawled across 20 acres of lush tea gardens in Kohora.",
            distanceToGate = "1.5 km to Mihimukh / Kohora Gate",
            nearestGate = "Kohora Gate",
            overview = "A premier 4-star luxury wildlife retreat surrounded by verdant tea bushes. Offers world-class spa treatments, swimming pool, and authentic Assamese thali dining.",
            amenities = listOf("Swimming Pool", "Ayurvedic Spa", "Tea Garden Walks", "Boutique Craft Shop", "Multi-Cuisine Restaurant", "Conference Hall"),
            contactPhone = "+91 3776 262423",
            roomTypes = listOf(
                RoomTypeOption("Luxury Tea View Room", "Balcony overlooking manicured tea estate.", 9500, 2),
                RoomTypeOption("Supreme Suite", "Separate living area with king master bedroom and luxury bath.", 14500, 3)
            )
        ),
        Accommodation(
            id = "wild_grass_lodge",
            name = "Wild Grass Lodge",
            category = AccommodationCategory.MID_RANGE,
            rating = 4.6,
            pricePerNightInr = 5200,
            locationDescription = "Kaziranga village woodland, 3 km from Kohora NH 715 junction.",
            distanceToGate = "4 km to Kohora Gate",
            nearestGate = "Kohora Gate",
            overview = "One of the earliest and most respected pioneer eco-lodges in Kaziranga. Built in vernacular Assamese architectural style with red brick and wooden verandas, cherished by avid wildlife naturalists.",
            amenities = listOf("Naturalist Library", "Bonfire Evenings", "Traditional Kitchen", "Birding Trail in Grounds", "Travel Desk"),
            contactPhone = "+91 94350 49904",
            roomTypes = listOf(
                RoomTypeOption("Standard Jungle Room", "Rustic brick room with twin beds and verandah seating.", 5200, 2),
                RoomTypeOption("Deluxe Cottage", "Independent cottage surrounded by bamboo groves.", 6800, 3)
            )
        ),
        Accommodation(
            id = "bonani_tourist_lodge",
            name = "Prashanti & Bonani Tourist Complex (ATDC)",
            category = AccommodationCategory.GOVT_LODGE,
            rating = 4.2,
            pricePerNightInr = 2400,
            locationDescription = "Right at Kohora central crossing, Government of Assam Tourism complex.",
            distanceToGate = "1.0 km to Mihimukh Gate",
            nearestGate = "Kohora Gate",
            overview = "Official government-managed tourist complex with spacious heritage rooms, peaceful manicured lawns, and direct walking distance to forest safari booking offices.",
            amenities = listOf("Direct Forest Booking Proximity", "On-site Restaurant", "Parking", "24hr Front Desk", "Lawn Seating"),
            contactPhone = "+91 3776 262429",
            roomTypes = listOf(
                RoomTypeOption("Standard Double AC", "Clean, comfortable government lodge room.", 2400, 2),
                RoomTypeOption("Deluxe AC Room", "Spacious room with modern bath.", 3200, 2)
            )
        ),
        Accommodation(
            id = "mishing_homestay",
            name = "Kaziranga Mishing Eco-Homestay",
            category = AccommodationCategory.BUDGET_HOMESTAY,
            rating = 4.8,
            pricePerNightInr = 1800,
            locationDescription = "Traditional indigenous Mishing tribal village near Kohora.",
            distanceToGate = "2.8 km to Kohora Gate",
            nearestGate = "Kohora Gate",
            overview = "Experience authentic indigenous Assamese tribal warmth! Stay in traditional Chang Ghar (bamboo house on stilts) and relish organic smoke-cooked ethnic dishes with the host family.",
            amenities = listOf("Tribal Stilt Living", "Home-cooked Ethnic Food", "Village Cultural Walk", "Bicycle Rental", "Campfire"),
            contactPhone = "+91 98540 12345",
            roomTypes = listOf(
                RoomTypeOption("Bamboo Stilt Room", "Eco-friendly bamboo construction with private balcony.", 1800, 2),
                RoomTypeOption("Family Chang Ghar", "Traditional stilt home suitable for families.", 2800, 4)
            )
        )
    )

    fun getTravelGuideSections(): List<TravelSection> = listOf(
        TravelSection(
            id = "how_to_reach",
            title = "How to Reach Kaziranga",
            subtitle = "Airports, Railways, and Highway routes",
            content = "Kaziranga National Park is located in Golaghat and Nagaon districts of Assam along National Highway 715 (NH 715 / old NH 37). Kohora is the primary tourist hub.",
            bulletPoints = listOf(
                "By Air: Jorhat Airport (JRH) is 97 km away (~2 hrs drive). Guwahati Lokpriya Gopinath Bordoloi Airport (GAU) is 225 km away (~4.5 hrs drive via 4-lane highway). Tezpur Airport is 60 km away.",
                "By Rail: Furkating Junction (FKG) is 75 km away. Guwahati Railway Station is 210 km away with express trains from all major Indian cities. Jakhalabandha is 45 km away.",
                "By Road: Frequent luxury ASTC government buses and private taxis ply NH 715 between Guwahati, Tezpur, Jorhat, and Kaziranga (Kohora bus stop)."
            )
        ),
        TravelSection(
            id = "best_season",
            title = "Best Time to Visit & Climate",
            subtitle = "Park opening cycle and seasonal wildlife behavior",
            content = "The park follows a strict seasonal cycle dictated by the Brahmaputra monsoon flood dynamics.",
            bulletPoints = listOf(
                "November to April (OPEN): The official tourist safari season. November has cool lush greenery; January has chilly mist; March-April brings dry weather with cropped grass offering the highest wildlife visibility.",
                "May to October (CLOSED): Monsoon season. Torrential rains cause the Brahmaputra to overflow, inundating 70-80% of the park. Safaris remain completely shut for wildlife safety and breeding."
            )
        ),
        TravelSection(
            id = "safari_types_permits",
            title = "Safari Rides & Official Permits",
            subtitle = "Jeep Safaris vs Elephant Safaris",
            content = "Every safari requires an entry permit issued by the Assam Forest Department. Booking in advance is strongly recommended, especially for peak December-February holidays.",
            bulletPoints = listOf(
                "Elephant Safari (Morning Only): 05:30 - 06:30 AM & 06:30 - 07:30 AM in Kohora and Bagori ranges. Provides an intimate, slow vantage point up close with rhinos through tall grasses where vehicles cannot enter.",
                "Jeep Safari (Morning & Afternoon): 07:30 - 09:30 AM and 01:30 - 03:30 PM across all 4 zones (Kohora, Bagori, Agoratoli, Burapahar). Covers large territory and remote watch towers.",
                "Permit Requirements: Valid government photo ID (Aadhaar, Passport, Voter ID) is mandatory at the gate entry checkpost. Foreign nationals require passport and visa details."
            )
        ),
        TravelSection(
            id = "park_rules",
            title = "Essential Park Rules & Conservation Etiquette",
            subtitle = "Strict Assam Forest Department guidelines",
            content = "Kaziranga is a protected UNESCO World Heritage Site with zero tolerance for poaching or disruptive tourism.",
            bulletPoints = listOf(
                "Zero Single-Use Plastic: Disposing plastic bottles or snack wrappers inside the park carries heavy fines.",
                "Strict Stay Inside Vehicle: Visitors are never permitted to step down from safari jeeps except at designated watch towers.",
                "No Flash Photography or Drones: Flying drones is strictly illegal and subject to confiscation. Camera flashes distress wild animals.",
                "Official Forest Armed Guard: Every safari jeep is accompanied by a registered department guide/guard for visitor safety.",
                "Speed Limit 40 km/h: Animal corridors along NH 715 are monitored by sensor cameras to prevent collisions."
            )
        )
    )
}
