package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalThemeConfig

enum class BannerType {
    INFO,
    SUCCESS,
    WARNING,
    ERROR,
    AI_ACTION
}

data class InAppBannerMessage(
    val message: String,
    val type: BannerType = BannerType.INFO,
    val title: String? = null,
    val actionLabel: String? = null,
    val onAction: (() -> Unit)? = null
)

@Composable
fun InAppNotificationBanner(
    banner: InAppBannerMessage?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val themeConfig = LocalThemeConfig.current
    val glowAccent = themeConfig.glowAccent

    AnimatedVisibility(
        visible = banner != null,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut(),
        modifier = modifier
    ) {
        if (banner != null) {
            val (bgColor, borderColor, iconColor, iconVector) = when (banner.type) {
                BannerType.ERROR -> Quadruple(
                    Color(0xFFFEF2F2),
                    Color(0xFFEF4444),
                    Color(0xFFDC2626),
                    Icons.Default.ErrorOutline
                )
                BannerType.WARNING -> Quadruple(
                    Color(0xFFFFFBEB),
                    Color(0xFFF59E0B),
                    Color(0xFFD97706),
                    Icons.Default.Warning
                )
                BannerType.SUCCESS -> Quadruple(
                    Color(0xFFF0FDF4),
                    Color(0xFF22C55E),
                    Color(0xFF16A34A),
                    Icons.Default.CheckCircle
                )
                BannerType.AI_ACTION -> Quadruple(
                    Color(0xFFF5F3FF),
                    glowAccent.primary,
                    glowAccent.primary,
                    Icons.Default.AutoAwesome
                )
                BannerType.INFO -> Quadruple(
                    Color(0xFFF0F9FF),
                    Color(0xFF0EA5E9),
                    Color(0xFF0284C7),
                    Icons.Default.Info
                )
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = bgColor,
                border = BorderStroke(1.5.dp, borderColor.copy(alpha = 0.6f)),
                shadowElevation = 4.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .testTag("in_app_notification_banner")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = iconVector,
                            contentDescription = null,
                            tint = iconColor,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            if (!banner.title.isNullOrBlank()) {
                                Text(
                                    text = banner.title,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E293B)
                                )
                            }
                            Text(
                                text = banner.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF334155),
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("btn_dismiss_banner")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss",
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
