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
        "All", "Styles", "Art", "Fresh", "Neon", "Pastel",
        "Luxury", "Retro", "Playful", "Minimal", "Classic",
        "Nature", "Ember", "Studio", "Pulse", "Photo", "Scenes"
    )

    fun initialize(context: Context) {
        if (_presets.isNotEmpty()) return
        try {
            val jsonString = context.assets.open("qrwho/presets.json").bufferedReader().use { it.readText() }
            val array = JSONArray(jsonString)
            val parsed = mutableListOf<QrPreset>()

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
            artDirection = o.optString("artDirection", null)
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
        )
    )
}
