package com.truestratix.qipositionfinder.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.truestratix.qipositionfinder.ui.theme.*

@Composable
fun AboutAppDialog(
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = CardNavy),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.verticalGradient(
                    listOf(NeonCyan.copy(alpha = 0.6f), CardBorderNavy)
                )
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Icon
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(DarkNavy)
                        .border(2.dp, NeonCyan, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = null,
                        tint = WarningAmber,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "QiCharge",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary
                )
                Text(
                    text = "Wireless Optimizer",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = NeonCyan
                )
                Text(
                    text = "Version 1.1.0 (Build 3) • Android 16 Ready",
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 12.sp),
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 2.dp, bottom = 16.dp)
                )

                HorizontalDivider(color = CardBorderNavy, modifier = Modifier.padding(bottom = 16.dp))

                // Developer Details
                AboutInfoRow(
                    icon = Icons.Default.Business,
                    label = "Organization",
                    value = "TrueStratix"
                )
                Spacer(modifier = Modifier.height(8.dp))
                AboutInfoRow(
                    icon = Icons.Default.Person,
                    label = "Developer",
                    value = "Dr. Delér Langenberg"
                )
                Spacer(modifier = Modifier.height(8.dp))
                AboutInfoRow(
                    icon = Icons.Default.Email,
                    label = "Support Contact",
                    value = "delerla@yahoo.de"
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons (Links)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://delerla.github.io/QiCharge/"))
                            context.startActivity(intent)
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "Privacy Policy", fontSize = 11.sp, maxLines = 1)
                    }

                    OutlinedButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/delerla/QiCharge"))
                            context.startActivity(intent)
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = "GitHub", fontSize = 11.sp, maxLines = 1)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://truestratix.com"))
                        context.startActivity(intent)
                    },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(imageVector = Icons.Default.Language, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Visit truestratix.com", fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Zero Data Collection • Local Hardware Telemetry\nLicensed under Apache 2.0",
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 11.sp),
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismissRequest,
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, contentColor = DarkNavy),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Close", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AboutInfoRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = NeonCyan,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(text = "$label: ", style = MaterialTheme.typography.bodyLarge.copy(fontSize = 13.sp), color = TextSecondary)
        Text(text = value, style = MaterialTheme.typography.bodyLarge.copy(fontSize = 13.sp), fontWeight = FontWeight.SemiBold, color = TextPrimary)
    }
}
