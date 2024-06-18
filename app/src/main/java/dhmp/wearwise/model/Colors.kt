package dhmp.wearwise.model



class ColorName(
    val name: String,
    val color: Int
)

val GarmentColorNames = listOf(
    ColorName("Black", 0xFF000000.toInt()),
    ColorName("LightBlack", 0xFF505050.toInt()),
    ColorName("DarkBlack", 0xFF000000.toInt()),  // Black is already dark

    ColorName("White", 0xFFFFFFFF.toInt()),
    ColorName("LightWhite", 0xFFFFFFFF.toInt()),  // White is already light
    ColorName("DarkWhite", 0xFFAAAAAA.toInt()),

    ColorName("Red", 0xFFFF0000.toInt()),
    ColorName("LightRed", 0xFFFFA07A.toInt()),
    ColorName("DarkRed", 0xFF8B0000.toInt()),

    ColorName("Green", 0xFF00FF00.toInt()),
    ColorName("LightGreen", 0xFF90EE90.toInt()),
    ColorName("DarkGreen", 0xFF006400.toInt()),

    ColorName("Blue", 0xFF0000FF.toInt()),
    ColorName("LightBlue", 0xFFADD8E6.toInt()),
    ColorName("DarkBlue", 0xFF00008B.toInt()),

    ColorName("Yellow", 0xFFFFFF00.toInt()),
    ColorName("LightYellow", 0xFFFFFFE0.toInt()),
    ColorName("DarkYellow", 0xFF9B870C.toInt()),

    ColorName("Cyan", 0xFF00FFFF.toInt()),
    ColorName("LightCyan", 0xFFE0FFFF.toInt()),
    ColorName("DarkCyan", 0xFF008B8B.toInt()),

    ColorName("Magenta", 0xFFFF00FF.toInt()),
    ColorName("LightMagenta", 0xFFFF77FF.toInt()),
    ColorName("DarkMagenta", 0xFF8B008B.toInt()),

    ColorName("Silver", 0xFFC0C0C0.toInt()),
    ColorName("LightSilver", 0xFFD3D3D3.toInt()),
    ColorName("DarkSilver", 0xFF708090.toInt()),

    ColorName("Gray", 0xFF808080.toInt()),
    ColorName("LightGray", 0xFFD3D3D3.toInt()),
    ColorName("DarkGray", 0xFFA9A9A9.toInt()),

    ColorName("Maroon", 0xFF800000.toInt()),
    ColorName("LightMaroon", 0xFFB03060.toInt()),
    ColorName("DarkMaroon", 0xFF800000.toInt()),

    ColorName("Purple", 0xFF800080.toInt()),
    ColorName("LightPurple", 0xFFE6E6FA.toInt()),
    ColorName("DarkPurple", 0xFF4B0082.toInt()),

    ColorName("Orange", 0xFFFFA500.toInt()),
    ColorName("LightOrange", 0xFFFFE4B2.toInt()),
    ColorName("DarkOrange", 0xFFFF8C00.toInt()),

    ColorName("Pink", 0xFFFFC0CB.toInt()),
    ColorName("LightPink", 0xFFFFB6C1.toInt()),
    ColorName("DarkPink", 0xFFC71585.toInt()),

    ColorName("Brown", 0xFFA52A2A.toInt()),
    ColorName("LightBrown", 0xFFCD853F.toInt()),
    ColorName("DarkBrown", 0xFF5C4033.toInt()),

    ColorName("Violet", 0xFFEE82EE.toInt()),
    ColorName("LightViolet", 0xFFD8BFD8.toInt()),
    ColorName("DarkViolet", 0xFF9400D3.toInt()),

    ColorName("Gold", 0xFFFFD700.toInt()),
    ColorName("LightGold", 0xFFFFFACD.toInt()),
    ColorName("DarkGold", 0xFFB8860B.toInt()),

    ColorName("Salmon", 0xFFFA8072.toInt()),
    ColorName("LightSalmon", 0xFFFFA07A.toInt()),
    ColorName("DarkSalmon", 0xFFE9967A.toInt()),

    ColorName("Beige", 0xFFF5F5DC.toInt()),
    ColorName("LightBeige", 0xFFFFF8DC.toInt()),
    ColorName("DarkBeige", 0xFFD2B48C.toInt()),

    ColorName("Coral", 0xFFFF7F50.toInt()),
    ColorName("LightCoral", 0xFFF08080.toInt()),
    ColorName("DarkCoral", 0xFFCD5B45.toInt()),


    ColorName("Crimson", 0xFFDC143C.toInt()),
    ColorName("LightCrimson", 0xFFFB607F.toInt()),
    ColorName("DarkCrimson", 0xFF8B0000.toInt()),

    ColorName("Teal", 0xFF008080.toInt()),
    ColorName("LightTeal", 0xFF20B2AA.toInt()),
    ColorName("DarkTeal", 0xFF006666.toInt()),

    ColorName("Navy", 0xFF000080.toInt()),
    ColorName("LightNavy", 0xFF5F9EA0.toInt()),
    ColorName("DarkNavy", 0xFF000040.toInt()),

    ColorName("Lime", 0xFF00FF00.toInt()),
    ColorName("LightLime", 0xFF66FF66.toInt()),
    ColorName("DarkLime", 0xFF32CD32.toInt()),

    ColorName("Olive", 0xFF808000.toInt()),
    ColorName("LightOlive", 0xFF999900.toInt()),
    ColorName("DarkOlive", 0xFF556B2F.toInt()),

    ColorName("Turquoise", 0xFF40E0D0.toInt()),
    ColorName("LightTurquoise", 0xFFAFEEEE.toInt()),
    ColorName("DarkTurquoise", 0xFF00CED1.toInt()),

    ColorName("Indigo", 0xFF4B0082.toInt()),
    ColorName("LightIndigo", 0xFF9FA8DA.toInt()),
    ColorName("DarkIndigo", 0xFF3F51B5.toInt()),

    ColorName("Lavender", 0xFFE6E6FA.toInt()),
    ColorName("LightLavender", 0xFFF3E5F5.toInt()),
    ColorName("DarkLavender", 0xFFB39DDB.toInt()),

    ColorName("Mint", 0xFF98FF98.toInt()),
    ColorName("LightMint", 0xFFBDFCC9.toInt()),
    ColorName("DarkMint", 0xFF32CD32.toInt())
)

