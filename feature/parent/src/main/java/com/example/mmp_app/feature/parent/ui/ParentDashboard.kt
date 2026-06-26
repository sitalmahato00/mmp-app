package com.example.mmp_app.feature.parent.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mmp_app.domain.model.ParentDashboardDto
import com.example.mmp_app.domain.model.ChildSummaryDto
import com.example.mmp_app.core.ui.KpiCard
import com.example.mmp_app.core.ui.theme.MMPAppTheme


@Composable
fun ParentDashboard(
    data: ParentDashboardDto,
    onChildClick: (Int, String) -> Unit = { _, _ -> }
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Welcome, ${data.parentName}",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Children Overview",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        items(data.children) { child ->
            ChildCard(
                child = child,
                onClick = { onChildClick(child.id, child.name) }
            )
        }
    }
}

@Composable
fun ChildCard(
    child: ChildSummaryDto,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Icon(Icons.Rounded.Person, contentDescription = null, modifier = Modifier.size(32.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = child.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(text = child.program, style = MaterialTheme.typography.bodySmall)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                KpiCard(
                    title = "Attendance",
                    value = "${child.attendancePercent.toInt()}%",
                    icon = Icons.Rounded.Person,
                    containerColor = getAttendanceColor(child.attendanceStatus),
                    modifier = Modifier.weight(1f)
                )
                // Using program as a placeholder for another KPI if needed, or keeping it clean
                KpiCard(
                    title = "Status",
                    value = child.attendanceStatus.replaceFirstChar { it.uppercase() },
                    icon = Icons.Rounded.Person,
                    containerColor = Color(0xFFF1F8E9),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

fun getAttendanceColor(status: String): Color {
    return when (status.lowercase()) {
        "good" -> Color(0xFFE8F5E9)
        "medium" -> Color(0xFFFFF8E1)
        "low" -> Color(0xFFFFEBEE)
        else -> Color(0xFFE3F2FD)
    }
}

@Preview(showBackground = true)
@Composable
fun ParentDashboardPreview() {
    MMPAppTheme {
        ParentDashboard(
            data = ParentDashboardDto(
                parentName = "John Smith",
                childrenCount = 2,
                children = listOf(
                    ChildSummaryDto(1, "Alice Smith", "S001", "CS", 1, "A", "", 92.0, "good"),
                    ChildSummaryDto(2, "Bob Smith", "S002", "CS", 1, "A", "", 65.0, "medium")
                )
            )
        )
    }
}
