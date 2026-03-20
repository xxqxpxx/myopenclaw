package com.myopenclaw.ui.screens.files

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.myopenclaw.ui.theme.*

data class GeneratedFile(
    val id: String,
    val filename: String,
    val size: Long,
    val conversationTitle: String,
    val createdAt: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilesScreen(
    onOpenFile: (String) -> Unit = {}
) {
    // Files will be populated from the backend in future.
    // For now, show empty state that explains what this tab does.
    val files = remember { mutableStateListOf<GeneratedFile>() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Files",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundDark
                )
            )
        },
        containerColor = BackgroundDark
    ) { padding ->
        if (files.isEmpty()) {
            // Empty state
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(32.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(72.dp),
                        color = Primary.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.FolderOpen,
                                contentDescription = null,
                                tint = Primary,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }

                    Text(
                        "No files yet",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )

                    Text(
                        "Files generated during your AI conversations will appear here. Ask the AI to create code files, CSVs, PDFs, and more.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Supported file types
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        FileTypeChip("Python", Icons.Default.Code)
                        FileTypeChip("CSV", Icons.Default.TableChart)
                        FileTypeChip("PDF", Icons.Default.PictureAsPdf)
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(files, key = { it.id }) { file ->
                    FileItem(
                        file = file,
                        onClick = { onOpenFile(file.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun FileTypeChip(
    label: String,
    icon: ImageVector
) {
    Surface(
        color = SurfaceDark,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = Primary,
                modifier = Modifier.size(16.dp)
            )
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun FileItem(
    file: GeneratedFile,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = SurfaceDark,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val icon = when {
                file.filename.endsWith(".py") -> Icons.Default.Code
                file.filename.endsWith(".csv") -> Icons.Default.TableChart
                file.filename.endsWith(".pdf") -> Icons.Default.PictureAsPdf
                file.filename.endsWith(".js") || file.filename.endsWith(".ts") -> Icons.Default.Code
                else -> Icons.Default.InsertDriveFile
            }

            Surface(
                modifier = Modifier.size(40.dp),
                color = Primary.copy(alpha = 0.15f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp))
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    file.filename,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "From: ${file.conversationTitle}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.5f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Icon(
                Icons.Default.Download,
                contentDescription = "Download",
                tint = Color.White.copy(alpha = 0.4f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
