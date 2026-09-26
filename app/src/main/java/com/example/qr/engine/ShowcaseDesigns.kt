package com.example.qr.engine

data class ShowcaseItem(
    val id: String,
    val title: String,
    val category: String,
    val tagline: String,
    val badge: String,
    val presetId: String,
    val defaultUrl: String = "https://qrwho.vercel.app"
)

object ShowcaseDesigns {
    val items = listOf(
        ShowcaseItem(
            id = "art-ukiyo",
            title = "Ukiyo Wave",
            category = "Japanese Woodblock",
            tagline = "Ocean indigo on handmade washi texture with leaf finders",
            badge = "Edo Aesthetic",
            presetId = "art-ukiyo"
        ),
        ShowcaseItem(
            id = "art-cyberpunk",
            title = "Cyberpunk 2099",
            category = "Sci-Fi & Tech",
            tagline = "Electric magenta to cyan neon data stream on dark onyx",
            badge = "High Voltage",
            presetId = "art-cyberpunk"
        ),
        ShowcaseItem(
            id = "art-vaporwave",
            title = "Vaporwave 1995",
            category = "Retro & Synth",
            tagline = "Pastel lilac, mint green and electric blue dreamscape",
            badge = "Retro Aesthetic",
            presetId = "art-vaporwave"
        ),
        ShowcaseItem(
            id = "art-royal",
            title = "Royal Gold",
            category = "Luxury & Fashion",
            tagline = "Obsidian black with polished gold gradient & classy finders",
            badge = "Editorial Luxe",
            presetId = "art-royal"
        ),
        ShowcaseItem(
            id = "art-sakura",
            title = "Sakura Bloom",
            category = "Weddings & Florals",
            tagline = "Two-lobe heart modules with emerald leaf eyes on blush cream",
            badge = "Romantic",
            presetId = "art-sakura"
        ),
        ShowcaseItem(
            id = "art-matcha",
            title = "Matcha Latte",
            category = "Café & Organic",
            tagline = "Earthy matcha green leaf modules on steamed cream",
            badge = "Organic",
            presetId = "art-matcha"
        ),
        ShowcaseItem(
            id = "art-solarpunk",
            title = "Solarpunk Dawn",
            category = "Green Tech & Nature",
            tagline = "Radiant lime & golden solar amber on forest night",
            badge = "Solarpunk",
            presetId = "art-solarpunk"
        ),
        ShowcaseItem(
            id = "art-neon-fungi",
            title = "Neon Fungi",
            category = "Creative & Music",
            tagline = "Bioluminescent emerald bubbles on pitch obsidian",
            badge = "Bioluminescent",
            presetId = "art-neon-fungi"
        ),
        ShowcaseItem(
            id = "art-mono",
            title = "Mono Luxe",
            category = "Swiss Minimalist",
            tagline = "Ultra-crisp geometric precision for architecture & design",
            badge = "Architectural",
            presetId = "art-mono"
        )
    )
}
