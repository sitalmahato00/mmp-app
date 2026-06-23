package com.example.mmp_app.core.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
                Text(text = notice.date, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                Text(text = notice.content, style = MaterialTheme.typography.bodyMedium, maxLines = 2)
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
                date = "2024-05-10",
                type = "General"
            )
        )
    }
}
