package com.example.qr.engine

import android.content.Context
import android.graphics.Color
import org.json.JSONArray
import org.json.JSONObject

object QrPresets {

    private val _presets = mutableListOf<QrPreset>()
    val list: List<QrPreset>
        get() = if (_presets.isNotEmpty()) _presets else fallbackList

    val categories: List<String> = listOf(
        "All", "Templates", "Styles", "Art", "Fresh", "Neon", "Pastel",
        "Luxury", "Retro", "Playful", "Minimal", "Classic",
        "Nature", "Ember", "Studio", "Pulse", "Photo", "Scenes"
    )

    fun initialize(context: Context) {
        if (_presets.isNotEmpty()) return
        try {
            val jsonString = context.assets.open("qrwho/presets.json").bufferedReader().use { it.readText() }
            val array = JSONArray(jsonString)
            val parsed = mutableListOf<QrPreset>()

            // Prepend custom templates to ensure they appear first
            parsed.addAll(fallbackList.filter { it.category == "Templates" })

            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val id = obj.optString("id", "preset_$i")
                val name = obj.optString("name", "Preset $i")
                val category = obj.optString("category", "Styles")
                val blurb = obj.optString("blurb", "")
                val featured = obj.optBoolean("featured", false)

                val styleObj = obj.optJSONObject("style")
                val style = if (styleObj != null) parseStyle(styleObj) else QrStyle()

                parsed.add(QrPreset(id, name, category, blurb, style, featured))
            }

            if (parsed.isNotEmpty()) {
                _presets.clear()
                _presets.addAll(parsed)
            }
        } catch (_: Exception) {
            // Fallback is used automatically
        }
    }

    private fun parseStyle(o: JSONObject): QrStyle {
        val moduleShapeStr = o.optString("moduleShape", "rounded")
        val eyeShapeStr = o.optString("eyeShape", "rounded")
        val ballShapeStr = o.optString("ballShape", "circle")
        val fgStr = o.optString("fg", "#0f172a")
        val bgStr = o.optString("bg", "#ffffff")
        val eyeColorStr = o.optString("eyeColor", fgStr)
        val ballColorStr = o.optString("ballColor", fgStr)
        val gradTypeStr = o.optString("gradientType", "none")
        val gradToStr = o.optString("gradientTo", fgStr)

        val frameStyleStr = o.optString("frameStyle", "none")
        val frameCaptionStr = o.optString("frameCaption", "SCAN ME")

        return QrStyle(
            moduleShape = ModuleShape.fromString(moduleShapeStr),
            eyeShape = EyeShape.fromString(eyeShapeStr),
            ballShape = EyeShape.fromString(ballShapeStr),
            fgColor = safeColor(fgStr, 0xFF0F172A.toInt()),
            bgColor = safeColor(bgStr, 0xFFFFFFFF.toInt()),
            eyeColor = safeColor(eyeColorStr, 0xFF0F172A.toInt()),
            ballColor = safeColor(ballColorStr, 0xFF0F172A.toInt()),
            gradientType = GradientType.fromString(gradTypeStr),
            gradientTo = safeColor(gradToStr, 0xFF7A5AF8.toInt()),
            quietZone = o.optInt("quietZone", 3),
            moduleGap = o.optDouble("moduleGap", 0.04).toFloat(),
            imageMode = ImageMode.fromString(o.optString("imageMode", "none")),
            imageOpacity = o.optDouble("imageOpacity", 0.86).toFloat(),
            photoZoom = o.optDouble("photoZoom", 1.0).toFloat(),
            dotScale = o.optDouble("dotScale", 0.88).toFloat(),
            contrast = o.optDouble("contrast", 1.0).toFloat(),
            logoScale = o.optDouble("logoScale", 0.22).toFloat(),
            ecc = o.optString("ecc", "H"),
            artisticStrength = o.optDouble("artisticStrength", 0.5).toFloat(),
            artDirection = o.optString("artDirection", null),
            frameStyle = FrameStyle.fromString(frameStyleStr),
            frameCaption = frameCaptionStr
        )
    }

    private fun safeColor(hex: String?, defaultColor: Int): Int {
        if (hex.isNullOrBlank()) return defaultColor
        return try {
            Color.parseColor(hex.trim())
        } catch (_: Exception) {
            defaultColor
        }
    }

    fun findById(id: String): QrPreset? = list.find { it.id == id }

    fun filter(query: String, category: String): List<QrPreset> {
        val q = query.trim().lowercase()
        return list.filter { preset ->
            val matchesCategory = category == "All" || preset.category.equals(category, ignoreCase = true)
            val matchesQuery = q.isEmpty() ||
                    preset.name.lowercase().contains(q) ||
                    preset.description.lowercase().contains(q) ||
                    preset.category.lowercase().contains(q)
            matchesCategory && matchesQuery
        }
    }

    // Curated fallback including the 9 flagship QRWho art showcase styles
    val fallbackList = listOf(
        QrPreset(
            id = "art-ukiyo",
            name = "Ukiyo Wave",
            category = "Art",
            description = "Ocean indigo on handmade washi texture with leaf finders",
            style = QrStyle(
                moduleShape = ModuleShape.Fluid,
                eyeShape = EyeShape.Leaf,
                ballShape = EyeShape.Leaf,
                fgColor = 0xFF164E63.toInt(),
                bgColor = 0xFFF4EFE6.toInt(),
                eyeColor = 0xFF0E3A4A.toInt(),
                ballColor = 0xFF0E3A4A.toInt(),
                gradientType = GradientType.Diagonal,
                gradientTo = 0xFF1D7A8C.toInt(),
                moduleGap = 0.02f,
                dotScale = 0.88f,
                ecc = "H",
                artDirection = "ukiyo-e"
            ),
            featured = true
        ),
        QrPreset(
            id = "art-cyberpunk",
            name = "Cyberpunk 2099",
            category = "Neon",
            description = "Electric magenta to cyan neon data stream on dark onyx",
            style = QrStyle(
                moduleShape = ModuleShape.Dash,
                eyeShape = EyeShape.Ticks,
                ballShape = EyeShape.Square,
                fgColor = 0xFFFF2A85.toInt(),
                bgColor = 0xFF0A0A14.toInt(),
                eyeColor = 0xFF00F0FF.toInt(),
                ballColor = 0xFF00F0FF.toInt(),
                gradientType = GradientType.Diagonal,
                gradientTo = 0xFF7928CA.toInt(),
                moduleGap = 0.04f,
                dotScale = 0.88f,
                ecc = "H",
                artDirection = "cyberpunk-pink"
            ),
            featured = true
        ),
        QrPreset(
            id = "art-vaporwave",
            name = "Vaporwave 1995",
            category = "Retro",
            description = "Pastel lilac, mint green and electric blue dreamscape",
            style = QrStyle(
                moduleShape = ModuleShape.Squircle,
                eyeShape = EyeShape.Square,
                ballShape = EyeShape.Square,
                fgColor = 0xFFFF8AD4.toInt(),
                bgColor = 0xFF120A24.toInt(),
                eyeColor = 0xFF5FF0E6.toInt(),
                ballColor = 0xFFF4E9FF.toInt(),
                gradientType = GradientType.Diagonal,
                gradientTo = 0xFF8D72FF.toInt(),
                moduleGap = 0.05f,
                dotScale = 0.90f,
                ecc = "Q",
                artDirection = "vaporwave"
            ),
            featured = true
        ),
        QrPreset(
            id = "art-royal",
            name = "Royal Gold",
            category = "Luxury",
            description = "Obsidian black with polished gold gradient & classy finders",
            style = QrStyle(
                moduleShape = ModuleShape.Classy,
                eyeShape = EyeShape.Classy,
                ballShape = EyeShape.Rounded,
                fgColor = 0xFFC6A25A.toInt(),
                bgColor = 0xFF16140F.toInt(),
                eyeColor = 0xFFE8D09A.toInt(),
                ballColor = 0xFFF0E4C0.toInt(),
                gradientType = GradientType.Linear,
                gradientTo = 0xFFF0E4C0.toInt(),
                moduleGap = 0.02f,
                dotScale = 0.88f,
                ecc = "H"
            ),
            featured = true
        ),
        QrPreset(
            id = "art-sakura",
            name = "Sakura Bloom",
            category = "Pastel",
            description = "Two-lobe heart modules with emerald leaf eyes on blush cream",
            style = QrStyle(
                moduleShape = ModuleShape.Heart,
                eyeShape = EyeShape.Leaf,
                ballShape = EyeShape.Circle,
                fgColor = 0xFFD4457F.toInt(),
                bgColor = 0xFFFDF4F1.toInt(),
                eyeColor = 0xFFB8477C.toInt(),
                ballColor = 0xFFB8477C.toInt(),
                gradientType = GradientType.Linear,
                gradientTo = 0xFFBE4A82.toInt(),
                moduleGap = 0.04f,
                dotScale = 0.90f,
                ecc = "H",
                artDirection = "sakura"
            ),
            featured = true
        ),
        QrPreset(
            id = "art-matcha",
            name = "Matcha Latte",
            category = "Nature",
            description = "Earthy matcha green leaf modules on steamed cream",
            style = QrStyle(
                moduleShape = ModuleShape.Leaf,
                eyeShape = EyeShape.Leaf,
                ballShape = EyeShape.Leaf,
                fgColor = 0xFF41644A.toInt(),
                bgColor = 0xFFF7F9F4.toInt(),
                eyeColor = 0xFF263E2D.toInt(),
                ballColor = 0xFF263E2D.toInt(),
                gradientType = GradientType.None,
                moduleGap = 0.04f,
                dotScale = 0.88f,
                ecc = "H"
            ),
            featured = true
        ),
        QrPreset(
            id = "art-solarpunk",
            name = "Solarpunk Dawn",
            category = "Nature",
            description = "Radiant lime & golden solar amber on forest night",
            style = QrStyle(
                moduleShape = ModuleShape.Fluid,
                eyeShape = EyeShape.Leaf,
                ballShape = EyeShape.Rounded,
                fgColor = 0xFF84CC16.toInt(),
                bgColor = 0xFF0C1708.toInt(),
                eyeColor = 0xFFFACC15.toInt(),
                ballColor = 0xFF65A30D.toInt(),
                gradientType = GradientType.Diagonal,
                gradientTo = 0xFFEAB308.toInt(),
                moduleGap = 0.02f,
                dotScale = 0.88f,
                ecc = "H"
            ),
            featured = true
        ),
        QrPreset(
            id = "art-neon-fungi",
            name = "Neon Fungi",
            category = "Neon",
            description = "Bioluminescent emerald bubbles on pitch obsidian",
            style = QrStyle(
                moduleShape = ModuleShape.Bubbles,
                eyeShape = EyeShape.Circle,
                ballShape = EyeShape.Circle,
                fgColor = 0xFF3DBA8B.toInt(),
                bgColor = 0xFF0B1210.toInt(),
                eyeColor = 0xFFD8F5EA.toInt(),
                ballColor = 0xFF22C3D6.toInt(),
                gradientType = GradientType.Diagonal,
                gradientTo = 0xFF22C3D6.toInt(),
                moduleGap = 0.02f,
                dotScale = 0.88f,
                ecc = "H"
            ),
            featured = true
        ),
        QrPreset(
            id = "art-mono",
            name = "Mono Luxe",
            category = "Minimal",
            description = "Ultra-crisp geometric precision for architecture & design",
            style = QrStyle(
                moduleShape = ModuleShape.Classy,
                eyeShape = EyeShape.Square,
                ballShape = EyeShape.Square,
                fgColor = 0xFF0E0E0E.toInt(),
                bgColor = 0xFFF7F7F5.toInt(),
                eyeColor = 0xFF0E0E0E.toInt(),
                ballColor = 0xFF0E0E0E.toInt(),
                gradientType = GradientType.None,
                moduleGap = 0.02f,
                dotScale = 0.88f,
                ecc = "H"
            ),
            featured = true
        ),
        // --- Custom Templates requested in UI Screenshots ---
        QrPreset(
            id = "tpl-classic",
            name = "Classic",
            category = "Templates",
            description = "Crisp high-contrast standard QR code style",
            style = QrStyle(
                moduleShape = ModuleShape.Square,
                eyeShape = EyeShape.Square,
                ballShape = EyeShape.Square,
                fgColor = 0xFF000000.toInt(),
                bgColor = 0xFFFFFFFF.toInt(),
                eyeColor = 0xFF000000.toInt(),
                ballColor = 0xFF000000.toInt(),
                gradientType = GradientType.None
            ),
            featured = true
        ),
        QrPreset(
            id = "tpl-globe",
            name = "Globe",
            category = "Templates",
            description = "Circular world globe frame with ocean blue tones",
            style = QrStyle(
                moduleShape = ModuleShape.Dots,
                eyeShape = EyeShape.Circle,
                ballShape = EyeShape.Circle,
                fgColor = 0xFF1976D2.toInt(),
                bgColor = 0xFFE3F2FD.toInt(),
                eyeColor = 0xFF1565C0.toInt(),
                ballColor = 0xFF1565C0.toInt(),
                gradientType = GradientType.None,
                frameStyle = FrameStyle.Globe,
                artDirection = "globe"
            ),
            featured = true
        ),
        QrPreset(
            id = "tpl-ocean-blue",
            name = "Ocean Blue",
            category = "Templates",
            description = "Vibrant azure blue dots on soft cyan backdrop",
            style = QrStyle(
                moduleShape = ModuleShape.Rounded,
                eyeShape = EyeShape.Rounded,
                ballShape = EyeShape.Rounded,
                fgColor = 0xFF1565C0.toInt(),
                bgColor = 0xFFFFFFFF.toInt(),
                eyeColor = 0xFF0D47A1.toInt(),
                ballColor = 0xFF0D47A1.toInt(),
                gradientType = GradientType.None
            ),
            featured = true
        ),
        QrPreset(
            id = "tpl-floral",
            name = "Floral",
            category = "Templates",
            description = "Soft rose pink modules with elegant floral corner accents",
            style = QrStyle(
                moduleShape = ModuleShape.Dots,
                eyeShape = EyeShape.Rounded,
                ballShape = EyeShape.Circle,
                fgColor = 0xFFEC4899.toInt(),
                bgColor = 0xFFFFF0F5.toInt(),
                eyeColor = 0xFFDB2777.toInt(),
                ballColor = 0xFFDB2777.toInt(),
                gradientType = GradientType.None,
                artDirection = "floral"
            ),
            featured = true
        ),
        QrPreset(
            id = "tpl-note",
            name = "Note",
            category = "Templates",
            description = "Sticky notebook page style with golden stitched border",
            style = QrStyle(
                moduleShape = ModuleShape.Squircle,
                eyeShape = EyeShape.Rounded,
                ballShape = EyeShape.Circle,
                fgColor = 0xFF1E3A8A.toInt(),
                bgColor = 0xFFFEF08A.toInt(),
                eyeColor = 0xFF1E3A8A.toInt(),
                ballColor = 0xFF1E3A8A.toInt(),
                gradientType = GradientType.None,
                frameStyle = FrameStyle.Note,
                artDirection = "note"
            ),
            featured = true
        ),
        QrPreset(
            id = "tpl-love",
            name = "Love",
            category = "Templates",
            description = "Romantic pink theme with floating hearts",
            style = QrStyle(
                moduleShape = ModuleShape.Heart,
                eyeShape = EyeShape.Rounded,
                ballShape = EyeShape.Circle,
                fgColor = 0xFFF43F5E.toInt(),
                bgColor = 0xFFFFF1F2.toInt(),
                eyeColor = 0xFFE11D48.toInt(),
                ballColor = 0xFFE11D48.toInt(),
                gradientType = GradientType.None,
                artDirection = "love"
            ),
            featured = true
        ),
        QrPreset(
            id = "tpl-stars",
            name = "Stars",
            category = "Templates",
            description = "Starlight constellation theme with golden star finders",
            style = QrStyle(
                moduleShape = ModuleShape.Star,
                eyeShape = EyeShape.Diamond,
                ballShape = EyeShape.Circle,
                fgColor = 0xFF64748B.toInt(),
                bgColor = 0xFFFFFFFF.toInt(),
                eyeColor = 0xFF475569.toInt(),
                ballColor = 0xFFF59E0B.toInt(),
                gradientType = GradientType.None,
                artDirection = "stars"
            ),
            featured = true
        ),
        QrPreset(
            id = "tpl-seal",
            name = "Seal",
            category = "Templates",
            description = "Crimson red stamp badge with scalloped edge",
            style = QrStyle(
                moduleShape = ModuleShape.Rounded,
                eyeShape = EyeShape.Hex,
                ballShape = EyeShape.Circle,
                fgColor = 0xFF1E3A8A.toInt(),
                bgColor = 0xFFF87171.toInt(),
                eyeColor = 0xFF1E3A8A.toInt(),
                ballColor = 0xFF1E3A8A.toInt(),
                gradientType = GradientType.None,
                frameStyle = FrameStyle.Seal,
                artDirection = "seal"
            ),
            featured = true
        ),
        QrPreset(
            id = "tpl-christmas",
            name = "Christmas",
            category = "Templates",
            description = "Festive pine green & holly berry red holiday theme",
            style = QrStyle(
                moduleShape = ModuleShape.Leaf,
                eyeShape = EyeShape.Leaf,
                ballShape = EyeShape.Circle,
                fgColor = 0xFF166534.toInt(),
                bgColor = 0xFFF0FDF4.toInt(),
                eyeColor = 0xFFDC2626.toInt(),
                ballColor = 0xFFDC2626.toInt(),
                gradientType = GradientType.None,
                artDirection = "christmas"
            ),
            featured = true
        ),
        QrPreset(
            id = "tpl-birthday",
            name = "Birthday",
            category = "Templates",
            description = "Pastel celebration sprinkled with party confetti",
            style = QrStyle(
                moduleShape = ModuleShape.Confetti,
                eyeShape = EyeShape.Circle,
                ballShape = EyeShape.Circle,
                fgColor = 0xFF9333EA.toInt(),
                bgColor = 0xFFFAF5FF.toInt(),
                eyeColor = 0xFF7E22CE.toInt(),
                ballColor = 0xFF7E22CE.toInt(),
                gradientType = GradientType.None,
                artDirection = "birthday"
            ),
            featured = true
        ),
        QrPreset(
            id = "tpl-diamond",
            name = "Diamond",
            category = "Templates",
            description = "Rotated diamond frame motif with lavender purple theme",
            style = QrStyle(
                moduleShape = ModuleShape.Diamond,
                eyeShape = EyeShape.Diamond,
                ballShape = EyeShape.Diamond,
                fgColor = 0xFF1E3A8A.toInt(),
                bgColor = 0xFFDDD6FE.toInt(),
                eyeColor = 0xFF1E3A8A.toInt(),
                ballColor = 0xFF1E3A8A.toInt(),
                gradientType = GradientType.None,
                frameStyle = FrameStyle.Diamond,
                artDirection = "diamond"
            ),
            featured = true
        ),
        QrPreset(
            id = "tpl-scan-me",
            name = "Scan Me",
            category = "Templates",
            description = "Clean black & slate code with top SCAN ME banner",
            style = QrStyle(
                moduleShape = ModuleShape.Square,
                eyeShape = EyeShape.Square,
                ballShape = EyeShape.Square,
                fgColor = 0xFF334155.toInt(),
                bgColor = 0xFFF8FAFC.toInt(),
                eyeColor = 0xFF1E293B.toInt(),
                ballColor = 0xFF1E293B.toInt(),
                gradientType = GradientType.None,
                frameStyle = FrameStyle.BadgeScanMe,
                frameCaption = "SCAN ME"
            ),
            featured = true
        ),
        QrPreset(
            id = "tpl-night-sky",
            name = "Night Sky",
            category = "Templates",
            description = "Midnight navy backdrop with glowing gold star lattice",
            style = QrStyle(
                moduleShape = ModuleShape.Star,
                eyeShape = EyeShape.Hex,
                ballShape = EyeShape.Circle,
                fgColor = 0xFFF59E0B.toInt(),
                bgColor = 0xFF0F172A.toInt(),
                eyeColor = 0xFFFBBF24.toInt(),
                ballColor = 0xFFFBBF24.toInt(),
                gradientType = GradientType.None,
                artDirection = "night-sky"
            ),
            featured = true
        ),
        QrPreset(
            id = "tpl-hexagon",
            name = "Hexagon",
            category = "Templates",
            description = "Honeycomb amber orange container with hexagonal modules",
            style = QrStyle(
                moduleShape = ModuleShape.Hex,
                eyeShape = EyeShape.Hex,
                ballShape = EyeShape.Hex,
                fgColor = 0xFF1E3A8A.toInt(),
                bgColor = 0xFFFBBF24.toInt(),
                eyeColor = 0xFF1E3A8A.toInt(),
                ballColor = 0xFF1E3A8A.toInt(),
                gradientType = GradientType.None,
                frameStyle = FrameStyle.Hexagon,
                artDirection = "hexagon"
            ),
            featured = true
        ),
        QrPreset(
            id = "tpl-nature",
            name = "Nature",
            category = "Templates",
            description = "Fresh mint botanical leaves theme with forest green",
            style = QrStyle(
                moduleShape = ModuleShape.Leaf,
                eyeShape = EyeShape.Leaf,
                ballShape = EyeShape.Leaf,
                fgColor = 0xFF15803D.toInt(),
                bgColor = 0xFFF0FDF4.toInt(),
                eyeColor = 0xFF166534.toInt(),
                ballColor = 0xFF166534.toInt(),
                gradientType = GradientType.None,
                artDirection = "nature"
            ),
            featured = true
        ),
        QrPreset(
            id = "tpl-arrows",
            name = "Arrows",
            category = "Templates",
            description = "Inward pointing corner arrows with vibrant rose accent",
            style = QrStyle(
                moduleShape = ModuleShape.Cross,
                eyeShape = EyeShape.Ticks,
                ballShape = EyeShape.Square,
                fgColor = 0xFFF43F5E.toInt(),
                bgColor = 0xFFFFF1F2.toInt(),
                eyeColor = 0xFFE11D48.toInt(),
                ballColor = 0xFFE11D48.toInt(),
                gradientType = GradientType.None,
                artDirection = "arrows"
            ),
            featured = true
        ),
        QrPreset(
            id = "tpl-speech",
            name = "Speech",
            category = "Templates",
            description = "Chat speech bubble frame with ocean cyan modules",
            style = QrStyle(
                moduleShape = ModuleShape.Dots,
                eyeShape = EyeShape.Circle,
                ballShape = EyeShape.Circle,
                fgColor = 0xFF0284C7.toInt(),
                bgColor = 0xFF67E8F9.toInt(),
                eyeColor = 0xFF0369A1.toInt(),
                ballColor = 0xFF0369A1.toInt(),
                gradientType = GradientType.None,
                frameStyle = FrameStyle.Speech,
                artDirection = "speech"
            ),
            featured = true
        ),
        QrPreset(
            id = "tpl-wedding",
            name = "Wedding",
            category = "Templates",
            description = "Soft pastel pink wedding elegance with classy eyes",
            style = QrStyle(
                moduleShape = ModuleShape.Classy,
                eyeShape = EyeShape.Classy,
                ballShape = EyeShape.Circle,
                fgColor = 0xFFBE185D.toInt(),
                bgColor = 0xFFFDF2F8.toInt(),
                eyeColor = 0xFF9D174D.toInt(),
                ballColor = 0xFF9D174D.toInt(),
                gradientType = GradientType.None,
                artDirection = "wedding"
            ),
            featured = true
        ),
        QrPreset(
            id = "tpl-summer",
            name = "Summer",
            category = "Templates",
            description = "Sunny golden yellow & ocean blue beach wave theme",
            style = QrStyle(
                moduleShape = ModuleShape.Fluid,
                eyeShape = EyeShape.ExtraRounded,
                ballShape = EyeShape.Circle,
                fgColor = 0xFFD97706.toInt(),
                bgColor = 0xFFFFFBEB.toInt(),
                eyeColor = 0xFFB45309.toInt(),
                ballColor = 0xFFB45309.toInt(),
                gradientType = GradientType.None,
                artDirection = "summer"
            ),
            featured = true
        ),
        QrPreset(
            id = "tpl-pentagon",
            name = "Pentagon",
            category = "Templates",
            description = "5-sided pentagon frame design in warm coral red",
            style = QrStyle(
                moduleShape = ModuleShape.Squircle,
                eyeShape = EyeShape.Rounded,
                ballShape = EyeShape.Circle,
                fgColor = 0xFF1E3A8A.toInt(),
                bgColor = 0xFFFB7185.toInt(),
                eyeColor = 0xFF1E3A8A.toInt(),
                ballColor = 0xFF1E3A8A.toInt(),
                gradientType = GradientType.None,
                frameStyle = FrameStyle.Pentagon,
                artDirection = "pentagon"
            ),
            featured = true
        ),
        QrPreset(
            id = "tpl-autumn",
            name = "Autumn",
            category = "Templates",
            description = "Warm golden fall leaves and beige autumn palette",
            style = QrStyle(
                moduleShape = ModuleShape.Leaf,
                eyeShape = EyeShape.Rounded,
                ballShape = EyeShape.Circle,
                fgColor = 0xFFB45309.toInt(),
                bgColor = 0xFFFEF3C7.toInt(),
                eyeColor = 0xFF92400E.toInt(),
                ballColor = 0xFF92400E.toInt(),
                gradientType = GradientType.None,
                artDirection = "autumn"
            ),
            featured = true
        ),
        QrPreset(
            id = "tpl-halloween",
            name = "Halloween",
            category = "Templates",
            description = "Spooky midnight violet backdrop with pumpkin orange dots",
            style = QrStyle(
                moduleShape = ModuleShape.Plus,
                eyeShape = EyeShape.Ticks,
                ballShape = EyeShape.Square,
                fgColor = 0xFFF97316.toInt(),
                bgColor = 0xFF18181B.toInt(),
                eyeColor = 0xFFEA580C.toInt(),
                ballColor = 0xFFEA580C.toInt(),
                gradientType = GradientType.None,
                artDirection = "halloween"
            ),
            featured = true
        ),
        QrPreset(
            id = "tpl-bucket",
            name = "Bucket",
            category = "Templates",
            description = "Tapered bucket container frame with crisp blue modules",
            style = QrStyle(
                moduleShape = ModuleShape.Rounded,
                eyeShape = EyeShape.Rounded,
                ballShape = EyeShape.Circle,
                fgColor = 0xFF1D4ED8.toInt(),
                bgColor = 0xFF93C5FD.toInt(),
                eyeColor = 0xFF1E40AF.toInt(),
                ballColor = 0xFF1E40AF.toInt(),
                gradientType = GradientType.None,
                frameStyle = FrameStyle.Bucket,
                artDirection = "bucket"
            ),
            featured = true
        ),
        QrPreset(
            id = "tpl-ramadan",
            name = "Ramadan",
            category = "Templates",
            description = "Deep midnight navy with golden crescent moon & star lattice",
            style = QrStyle(
                moduleShape = ModuleShape.Star,
                eyeShape = EyeShape.Hex,
                ballShape = EyeShape.Circle,
                fgColor = 0xFFF59E0B.toInt(),
                bgColor = 0xFF0F172A.toInt(),
                eyeColor = 0xFFFBBF24.toInt(),
                ballColor = 0xFFFBBF24.toInt(),
                gradientType = GradientType.None,
                artDirection = "ramadan"
            ),
            featured = true
        ),
        QrPreset(
            id = "tpl-ocean",
            name = "Ocean",
            category = "Templates",
            description = "Wave patterns header and footer with marine blue modules",
            style = QrStyle(
                moduleShape = ModuleShape.Fluid,
                eyeShape = EyeShape.ExtraRounded,
                ballShape = EyeShape.Circle,
                fgColor = 0xFF0284C7.toInt(),
                bgColor = 0xFFE0F2FE.toInt(),
                eyeColor = 0xFF0369A1.toInt(),
                ballColor = 0xFF0369A1.toInt(),
                gradientType = GradientType.None,
                artDirection = "ocean"
            ),
            featured = true
        ),
        QrPreset(
            id = "tpl-plaque",
            name = "Plaque",
            category = "Templates",
            description = "Green plaque badge container frame with stitched border",
            style = QrStyle(
                moduleShape = ModuleShape.Squircle,
                eyeShape = EyeShape.Rounded,
                ballShape = EyeShape.Circle,
                fgColor = 0xFF3F6212.toInt(),
                bgColor = 0xFFBEF264.toInt(),
                eyeColor = 0xFF365314.toInt(),
                ballColor = 0xFF365314.toInt(),
                gradientType = GradientType.None,
                frameStyle = FrameStyle.Plaque,
                artDirection = "plaque"
            ),
            featured = true
        ),
        QrPreset(
            id = "tpl-snowflakes",
            name = "Snowflakes",
            category = "Templates",
            description = "Ice blue background with falling snowflake star dots",
            style = QrStyle(
                moduleShape = ModuleShape.Star,
                eyeShape = EyeShape.Circle,
                ballShape = EyeShape.Circle,
                fgColor = 0xFF3B82F6.toInt(),
                bgColor = 0xFFEFF6FF.toInt(),
                eyeColor = 0xFF2563EB.toInt(),
                ballColor = 0xFF2563EB.toInt(),
                gradientType = GradientType.None,
                artDirection = "snowflakes"
            ),
            featured = true
        ),
        QrPreset(
            id = "tpl-vintage",
            name = "Vintage",
            category = "Templates",
            description = "Parchment beige with retro antique corner flourishes",
            style = QrStyle(
                moduleShape = ModuleShape.Classy,
                eyeShape = EyeShape.Classy,
                ballShape = EyeShape.Square,
                fgColor = 0xFF78350F.toInt(),
                bgColor = 0xFFFEF3C7.toInt(),
                eyeColor = 0xFF451A03.toInt(),
                ballColor = 0xFF451A03.toInt(),
                gradientType = GradientType.None,
                frameStyle = FrameStyle.SimpleBorder,
                artDirection = "vintage"
            ),
            featured = true
        ),
        QrPreset(
            id = "tpl-card",
            name = "Card",
            category = "Templates",
            description = "Rounded card container with bottom scan me pill",
            style = QrStyle(
                moduleShape = ModuleShape.Rounded,
                eyeShape = EyeShape.Rounded,
                ballShape = EyeShape.Circle,
                fgColor = 0xFF0F766E.toInt(),
                bgColor = 0xFF2DD4BF.toInt(),
                eyeColor = 0xFF115E59.toInt(),
                ballColor = 0xFF115E59.toInt(),
                gradientType = GradientType.None,
                frameStyle = FrameStyle.Card,
                frameCaption = "SCAN ME"
            ),
            featured = true
        ),
        QrPreset(
            id = "tpl-spring",
            name = "Spring",
            category = "Templates",
            description = "Pastel spring garden theme with blooming floral dots",
            style = QrStyle(
                moduleShape = ModuleShape.Dots,
                eyeShape = EyeShape.Circle,
                ballShape = EyeShape.Circle,
                fgColor = 0xFFDB2777.toInt(),
                bgColor = 0xFFFDF2F8.toInt(),
                eyeColor = 0xFFBE185D.toInt(),
                ballColor = 0xFFBE185D.toInt(),
                gradientType = GradientType.None,
                artDirection = "spring"
            ),
            featured = true
        ),
        QrPreset(
            id = "tpl-music",
            name = "Music",
            category = "Templates",
            description = "Lavender purple theme with rhythm bubble notes",
            style = QrStyle(
                moduleShape = ModuleShape.Bubbles,
                eyeShape = EyeShape.ExtraRounded,
                ballShape = EyeShape.Circle,
                fgColor = 0xFF7C3AED.toInt(),
                bgColor = 0xFFF3E8FF.toInt(),
                eyeColor = 0xFF6D28D9.toInt(),
                ballColor = 0xFF6D28D9.toInt(),
                gradientType = GradientType.None,
                artDirection = "music"
            ),
            featured = true
        ),
        QrPreset(
            id = "tpl-label",
            name = "Label",
            category = "Templates",
            description = "Pill label container frame with scan me badge",
            style = QrStyle(
                moduleShape = ModuleShape.Rounded,
                eyeShape = EyeShape.ExtraRounded,
                ballShape = EyeShape.Circle,
                fgColor = 0xFF0284C7.toInt(),
                bgColor = 0xFF06B6D4.toInt(),
                eyeColor = 0xFF0E7490.toInt(),
                ballColor = 0xFF0E7490.toInt(),
                gradientType = GradientType.None,
                frameStyle = FrameStyle.Label,
                frameCaption = "SCAN ME"
            ),
            featured = true
        ),
        QrPreset(
            id = "tpl-dot-ring",
            name = "Dot Ring",
            category = "Templates",
            description = "Playful dotted ring frame surrounding the code",
            style = QrStyle(
                moduleShape = ModuleShape.Dots,
                eyeShape = EyeShape.Circle,
                ballShape = EyeShape.Circle,
                fgColor = 0xFF10B981.toInt(),
                bgColor = 0xFFFFFFFF.toInt(),
                eyeColor = 0xFF059669.toInt(),
                ballColor = 0xFF059669.toInt(),
                gradientType = GradientType.None,
                artDirection = "dot-ring"
            ),
            featured = true
        ),
        QrPreset(
            id = "tpl-bracket",
            name = "Bracket",
            category = "Templates",
            description = "Camera bracket frame with speech-bubble scan code badge",
            style = QrStyle(
                moduleShape = ModuleShape.Square,
                eyeShape = EyeShape.Square,
                ballShape = EyeShape.Square,
                fgColor = 0xFF1E293B.toInt(),
                bgColor = 0xFFFFFFFF.toInt(),
                eyeColor = 0xFF0F172A.toInt(),
                ballColor = 0xFF0F172A.toInt(),
                gradientType = GradientType.None,
                frameStyle = FrameStyle.Bracket,
                frameCaption = "SCAN CODE"
            ),
            featured = true
        ),
        QrPreset(
            id = "tpl-badge",
            name = "Badge",
            category = "Templates",
            description = "Circular scalloped badge with bottom ribbon",
            style = QrStyle(
                moduleShape = ModuleShape.Rounded,
                eyeShape = EyeShape.ExtraRounded,
                ballShape = EyeShape.Circle,
                fgColor = 0xFF6D28D9.toInt(),
                bgColor = 0xFFC4B5FD.toInt(),
                eyeColor = 0xFF5B21B6.toInt(),
                ballColor = 0xFF5B21B6.toInt(),
                gradientType = GradientType.None,
                frameStyle = FrameStyle.Badge,
                frameCaption = "SCAN CODE"
            ),
            featured = true
        ),
        QrPreset(
            id = "tpl-arch",
            name = "Arch",
            category = "Templates",
            description = "Archway dome container shape in regal purple",
            style = QrStyle(
                moduleShape = ModuleShape.Squircle,
                eyeShape = EyeShape.ExtraRounded,
                ballShape = EyeShape.Circle,
                fgColor = 0xFF581C87.toInt(),
                bgColor = 0xFFDDD6FE.toInt(),
                eyeColor = 0xFF3B0764.toInt(),
                ballColor = 0xFF3B0764.toInt(),
                gradientType = GradientType.None,
                frameStyle = FrameStyle.Arch,
                artDirection = "arch"
            ),
            featured = true
        ),
        QrPreset(
            id = "tpl-cup",
            name = "Cup",
            category = "Templates",
            description = "Takeaway coffee cup shape container in fresh teal",
            style = QrStyle(
                moduleShape = ModuleShape.Rounded,
                eyeShape = EyeShape.Rounded,
                ballShape = EyeShape.Circle,
                fgColor = 0xFF0D9488.toInt(),
                bgColor = 0xFF14B8A6.toInt(),
                eyeColor = 0xFF0F766E.toInt(),
                ballColor = 0xFF0F766E.toInt(),
                gradientType = GradientType.None,
                frameStyle = FrameStyle.Cup,
                artDirection = "cup"
            ),
            featured = true
        )
    )
}
