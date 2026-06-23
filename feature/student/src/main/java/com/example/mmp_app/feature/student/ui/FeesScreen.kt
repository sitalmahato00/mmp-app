package com.example.mmp_app.feature.student.ui


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.History
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mmp_app.core.ui.SkeletonBox

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeesScreen(
    isLoading: Boolean = false,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Fees") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8F9FF))
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                if (isLoading) {
                    SkeletonBox(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        shape = RoundedCornerShape(16.dp)
                    )
                } else {
                    FeeSummaryCard()
                }
            }

            item {
                Text(
                    text = "Fee Summary",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            if (isLoading) {
                items(5) {
                    SkeletonBox(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(72.dp),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            } else {
                item {
                    FeeBreakdownItem("Tuition Fee", "₹ 45,000", true)
                }
                item {
                    FeeBreakdownItem("Library Fee", "₹ 2,000", true)
                }
                item {
                    FeeBreakdownItem("Exam Fee", "₹ 1,500", true)
                }
                item {
                    FeeBreakdownItem("Development Fee", "₹ 3,000", false)
                }
                item {
                    FeeBreakdownItem("Miscellaneous Fee", "₹ 1,000", false)
                }
            }

            item {
                Button(
                    onClick = { },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Pay Now", modifier = Modifier.padding(8.dp))
                }
            }
        }
    }
}

@Composable
fun FeeSummaryCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1976D2)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "Total Due", color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.labelMedium)
                Text(text = "₹ 12,500", color = Color.White, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "Last Date to Pay: Jun 15, 2025", color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.labelSmall)
            }
            Icon(
                imageVector = Icons.Rounded.AccountBalanceWallet,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.2f),
                modifier = Modifier.size(64.dp)
            )
        }
    }
}

@Composable
fun FeeBreakdownItem(title: String, amount: String, isPaid: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = title, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = amount, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(12.dp))
                Surface(
                    color = (if (isPaid) Color(0xFF00C853) else Color(0xFFFFAB00)).copy(alpha = 0.1f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = if (isPaid) "Paid" else "Pending",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isPaid) Color(0xFF00C853) else Color(0xFFFFAB00),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
