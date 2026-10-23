package com.michde.opennovalauncher

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.collection.LruCache
import androidx.core.content.ContextCompat

object TechnicalMinimalIconEngine {

    private val cache = LruCache<String, Drawable>(128)

    fun getStyledIcon(context: Context, packageName: String, originalIcon: Drawable): Drawable {
        val cached = cache.get(packageName)
        if (cached != null) return cached

        // 1. Check known handcrafted vector icons
        val vectorResId = getVectorForPackage(packageName)
        if (vectorResId != null) {
            val drawable = ContextCompat.getDrawable(context, vectorResId)
            if (drawable != null) {
                cache.put(packageName, drawable)
                return drawable
            }
        }

        // 2. Synthesize Nothing OS / Teenage Engineering monochrome badge
        val styled = synthesizeMinimalBadge(context, originalIcon)
        cache.put(packageName, styled)
        return styled
    }

    private fun getVectorForPackage(pkg: String): Int? {
        val lower = pkg.lowercase()
        return when {
            lower.contains("wannaphong.hostai") -> R.drawable.ic_tm_hostai
            lower.contains("s24optimizer") -> R.drawable.ic_tm_optimizer
            lower.contains("settings") -> R.drawable.ic_tm_settings
            lower.contains("dialer") || lower.contains("phone") -> R.drawable.ic_tm_phone
            lower.contains("messaging") || lower.contains("mms") || lower.contains("message") -> R.drawable.ic_tm_messages
            lower.contains("camera") -> R.drawable.ic_tm_camera
            lower.contains("gallery") || lower.contains("photos") -> R.drawable.ic_tm_gallery
            lower.contains("chrome") || lower.contains("browser") -> R.drawable.ic_tm_browser
            lower.contains("aurora.store") || lower.contains("vending") -> R.drawable.ic_tm_store
            lower.contains("fdroid") -> R.drawable.ic_tm_fdroid
            lower.contains("dicio") || lower.contains("voice") -> R.drawable.ic_tm_mic
            lower.contains("clock") -> R.drawable.ic_tm_clock
            lower.contains("calculator") -> R.drawable.ic_tm_calc
            lower.contains("myfiles") || lower.contains("documentsui") || lower.contains("file") -> R.drawable.ic_tm_files
            else -> null
        }
    }

    private fun synthesizeMinimalBadge(context: Context, originalIcon: Drawable): Drawable {
        val density = context.resources.displayMetrics.density
        val sizePx = (48 * density).toInt().coerceAtLeast(48)
        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val center = sizePx / 2f
        val radius = (sizePx / 2f) - (2f * density)

        // Circle disc background (#151515)
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#151515")
            style = Paint.Style.FILL
        }
        canvas.drawCircle(center, center, radius, bgPaint)

        // Hairline outline (#2E2E2E)
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#2E2E2E")
            style = Paint.Style.STROKE
            strokeWidth = 1.5f * density
        }
        canvas.drawCircle(center, center, radius, strokePaint)

        // Render original icon in grayscale/silver monochrome inside
        val iconSize = (sizePx * 0.58f).toInt()
        val iconOffset = (sizePx - iconSize) / 2
        originalIcon.setBounds(iconOffset, iconOffset, iconOffset + iconSize, iconOffset + iconSize)

        // Grayscale + High-Contrast Matrix
        val colorMatrix = ColorMatrix().apply {
            setSaturation(0.0f)
            val contrast = 1.35f
            val brightness = 25f
            val scale = floatArrayOf(
                contrast, 0f, 0f, 0f, brightness,
                0f, contrast, 0f, 0f, brightness,
                0f, 0f, contrast, 0f, brightness,
                0f, 0f, 0f, 1f, 0f
            )
            postConcat(ColorMatrix(scale))
        }
        val filter = ColorMatrixColorFilter(colorMatrix)
        originalIcon.colorFilter = filter
        originalIcon.draw(canvas)
        originalIcon.colorFilter = null // restore

        // Signature Teenage Engineering Orange Accent Dot (#FF5A1F)
        val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#FF5A1F")
            style = Paint.Style.FILL
        }
        val dotRadius = 2.0f * density
        val dotX = sizePx - (10f * density)
        val dotY = (12f * density)
        canvas.drawCircle(dotX, dotY, dotRadius, dotPaint)

        return BitmapDrawable(context.resources, bitmap)
    }
}
