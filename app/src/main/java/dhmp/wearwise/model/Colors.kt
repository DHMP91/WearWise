package dhmp.wearwise.model


class ColorName(
    val name: String,
    val color: Int
)

val nearestColorMatchList = listOf(
    ColorName("Black", 0xFF000000.toInt()),
    ColorName("Gray", 0xFF808080.toInt()),
    ColorName("White", 0xFFFFFFFF.toInt()),
    ColorName("Yellow", 0xFFFFFF00.toInt()),
    ColorName("Blue", 0xFF0000FF.toInt()),
    ColorName("Green", 0xFF00FF00 .toInt()),
    ColorName("Red", 0xFFFF0000.toInt()),
    ColorName("Brown", 0xFFA52A2A.toInt()),

)

val GarmentColorNames = listOf(
    ColorName("Black", 0xFF1A1A1A.toInt()),
    ColorName("White", 0xFFF5F5F5.toInt()),
    ColorName("Yellow", 0xFFF9D71C.toInt()),
    ColorName("Blue", 0xFF3A86FF.toInt()),
    ColorName("Brown", 0xFF8B4513.toInt()),
    ColorName("Green", 0xFF006400.toInt()),
    ColorName("Gray", 0xFFA9A9A9.toInt()),
    ColorName("Orange", 0xFFFF6F00.toInt()),
    ColorName("Pink", 0xFFFF99AC.toInt()),
    ColorName("Purple", 0xFF6A0DAD.toInt()),
    ColorName("Red", 0xFFE63946.toInt()),
    ColorName("Navy", 0xFF001F54.toInt()),
    ColorName("Olive", 0xFF556B2F.toInt()),
    // Less Common
    ColorName("Beige", 0xFFF5F5DC.toInt()),
    ColorName("Charcoal", 0xFF2E3B4E.toInt()),
    ColorName("Coral", 0xFFFF6F61.toInt()),
    ColorName("Crimson", 0xFFB22234.toInt()),
    ColorName("Cyan", 0xFF00B4D8.toInt()),
    ColorName("Gold", 0xFFE1C16E.toInt()),
    ColorName("Indigo", 0xFF3F51B5.toInt()),
    ColorName("Lavender", 0xFFCBC3E3.toInt()),
    ColorName("Lime", 0xFFA1C935.toInt()),
    ColorName("Magenta", 0xFFD70070.toInt()),
    ColorName("Maroon", 0xFF800020.toInt()),
    ColorName("Mint", 0xFF99EDC3.toInt()),
    ColorName("Salmon", 0xFFFA8072.toInt()),
    ColorName("Silver", 0xFFB0C4DE.toInt()),
    ColorName("Teal", 0xFF00796B.toInt()),
    ColorName("Turquoise", 0xFF30D5C8.toInt()),
    ColorName("Violet", 0xFF8F00FF.toInt())
)

val GarmentColorNameTable: HashMap<String, ColorName> = hashMapOf<String, ColorName>().apply {
    putAll(GarmentColorNames.associateBy { it.name })
}

val GarmentColorColorTable: HashMap<Int, ColorName> = hashMapOf<Int, ColorName>().apply {
    putAll(GarmentColorNames.associateBy { it.color })
}

data class ColorCount(
    val color: String?,
    val count: Int
)
