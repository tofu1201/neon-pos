package com.yourcompany.pos.presentation.pos.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CardMembership
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.yourcompany.pos.presentation.theme.Surface
import com.yourcompany.pos.presentation.theme.SurfaceBorder
import com.yourcompany.pos.presentation.theme.TextPrimary
import com.yourcompany.pos.presentation.theme.TextSecondary
import java.util.Locale

@Composable
fun MemberInfoBanner(
    memberId: String,
    memberName: String,
    points: Int,
    discountRate: Double,
    modifier: Modifier = Modifier,
    color: Color,
) {
    val shape = RoundedCornerShape(18.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Surface, shape)
            .border(1.dp, SurfaceBorder.copy(alpha = 0.7f), shape)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = Icons.Outlined.CardMembership,
            contentDescription = null,
            tint = color
        )
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(text = "會員資訊", color = TextPrimary, style = MaterialTheme.typography.titleMedium)
            Text(text = "$memberName  •  $memberId", color = TextSecondary, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = String.format(Locale.getDefault(), "Points %d  •  Discount %.0f%%", points, (1.0 - discountRate) * 100),
                color = TextSecondary,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
