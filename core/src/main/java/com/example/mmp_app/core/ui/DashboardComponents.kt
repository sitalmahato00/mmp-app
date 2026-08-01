package com.example.mmp_app.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mmp_app.domain.model.NoticeDto
import com.example.mmp_app.core.ui.theme.MMPAppTheme


@Composable
fun KpiCard(
    title: String,
    value: String,
    icon: ImageVector,
    containerColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier.height(120.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(24.dp))
            Column {
                Text(text = value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(text = title, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun ModernBottomNavBar(
    selectedItem: Int,
    onItemSelected: (Int) -> Unit,
    items: List<ModernNavItem>,
    primaryColor: Color,
    secondaryColor: Color,
    cardBgColor: Color,
    textColor: Color,
    onCenterClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 24.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .shadow(12.dp, RoundedCornerShape(36.dp)),
            shape = RoundedCornerShape(36.dp),
            color = cardBgColor
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Items (first 2)
                items.take(2).forEachIndexed { index, item ->
                    CapsuleNavItem(item.icon, item.label, { onItemSelected(index + 1) }, textColor, selected = selectedItem == index + 1)
                }
                
                Spacer(modifier = Modifier.width(48.dp)) // Center space for FAB
                
                // Right Items (remaining)
                items.drop(2).take(2).forEachIndexed { index, item ->
                    val actualIndex = index + 3
                    CapsuleNavItem(item.icon, item.label, { onItemSelected(actualIndex) }, textColor, selected = selectedItem == actualIndex)
                }
            }
        }
        
        // Floating Center Button
        Surface(
            modifier = Modifier
                .size(72.dp)
                .offset(y = (-20).dp)
                .shadow(8.dp, CircleShape)
                .clickable(onClick = { onItemSelected(0) }),
            shape = CircleShape,
            color = Color.Transparent
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(listOf(primaryColor, secondaryColor))),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.Dashboard, 
                    contentDescription = "Dashboard", 
                    tint = Color.White, 
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

data class ModernNavItem(
    val icon: ImageVector,
    val label: String
)

@Composable
fun CapsuleNavItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    textColor: Color,
    selected: Boolean = false,
    activeColor: Color = Color(0xFF6366F1)
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(4.dp)
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint = if (selected) activeColor else textColor.copy(alpha = 0.6f),
            modifier = Modifier.size(24.dp)
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) activeColor else textColor.copy(alpha = 0.6f),
            fontSize = 10.sp
        )
    }
}

@Composable
fun NoticeItem(notice: NoticeDto) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.Notifications,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = notice.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Text(text = notice.publishedAt, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                Text(text = notice.content ?: "", style = MaterialTheme.typography.bodyMedium, maxLines = 2)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun KpiCardPreview() {
    MMPAppTheme {
        KpiCard(
            title = "Attendance",
            value = "85%",
            icon = Icons.Rounded.Notifications,
            containerColor = Color(0xFFE3F2FD)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun NoticeItemPreview() {
    MMPAppTheme {
        NoticeItem(
            notice = NoticeDto(
                id = 1,
                title = "Holiday Notice",
                content = "College will remain closed on Friday for the festival celebration.",
                publishedAt = "2024-05-10",
                type = "General",
                attachmentCount = 0
            )
        )
    }
}
