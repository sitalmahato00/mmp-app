package com.example.mmp_app.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChildCare
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mmp_app.data.remote.model.ChildDto
import com.example.mmp_app.data.remote.model.ParentDashboardDto
import com.example.mmp_app.ui.dashboard.components.KpiCard
import com.example.mmp_app.ui.theme.MMPAppTheme

@Composable
fun ParentDashboard(data: ParentDashboardDto) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Children Overview",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        items(data.children) { child ->
            ChildCard(child = child)
        }
    }
}

@Composable
fun ChildCard(child: ChildDto) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Icon(Icons.Rounded.Person, contentDescription = null, modifier = Modifier.size(32.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text(text = child.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                KpiCard(
                    title = "Attendance",
                    value = "${child.attendancePercentage}%",
                    icon = Icons.Rounded.Person, // Replace with appropriate icon
                    containerColor = Color(0xFFE3F2FD),
                    modifier = Modifier.weight(1f)
                )
                KpiCard(
                    title = "Avg Marks",
                    value = "${child.averageMarks}%",
                    icon = Icons.Rounded.Person, // Replace with appropriate icon
                    containerColor = Color(0xFFF1F8E9),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ParentDashboardPreview() {
    MMPAppTheme {
        ParentDashboard(
            data = ParentDashboardDto(
                children = listOf(
                    ChildDto(1, "Alice Smith", "Student", 92f, 88.5f),
                    ChildDto(2, "Bob Smith", "Student", 85f, 76f)
                )
            )
        )
    }
}
