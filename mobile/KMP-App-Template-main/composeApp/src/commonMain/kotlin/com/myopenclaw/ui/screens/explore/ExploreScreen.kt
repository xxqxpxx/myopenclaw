package com.myopenclaw.ui.screens.explore

import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.myopenclaw.ui.theme.*

data class PromptTemplate(
    val title: String,
    val description: String,
    val prompt: String,
    val icon: ImageVector,
    val category: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    onStartChatWithPrompt: (String) -> Unit = {}
) {
    val categories = remember {
        listOf("All", "Code", "Debug", "Learn", "Build")
    }
    var selectedCategory by remember { mutableStateOf("All") }

    val templates = remember {
        listOf(
            PromptTemplate(
                title = "Write a Function",
                description = "Generate a function in any language with tests",
                prompt = "Write a function that ",
                icon = Icons.Default.Code,
                category = "Code"
            ),
            PromptTemplate(
                title = "Debug My Code",
                description = "Paste your code and get help finding bugs",
                prompt = "Help me debug this code:\n\n",
                icon = Icons.Default.BugReport,
                category = "Debug"
            ),
            PromptTemplate(
                title = "Explain a Concept",
                description = "Get clear explanations of programming concepts",
                prompt = "Explain the concept of ",
                icon = Icons.Default.School,
                category = "Learn"
            ),
            PromptTemplate(
                title = "Code Review",
                description = "Get feedback on code quality and best practices",
                prompt = "Review this code and suggest improvements:\n\n",
                icon = Icons.Default.RateReview,
                category = "Code"
            ),
            PromptTemplate(
                title = "Build a Project",
                description = "Get step-by-step guidance for building apps",
                prompt = "Help me build a ",
                icon = Icons.Default.Rocket,
                category = "Build"
            ),
            PromptTemplate(
                title = "Convert Code",
                description = "Translate code between programming languages",
                prompt = "Convert this code to ",
                icon = Icons.Default.SwapHoriz,
                category = "Code"
            ),
            PromptTemplate(
                title = "Write Tests",
                description = "Generate unit tests for your code",
                prompt = "Write unit tests for this code:\n\n",
                icon = Icons.Default.CheckCircle,
                category = "Code"
            ),
            PromptTemplate(
                title = "Fix an Error",
                description = "Paste an error message and get solutions",
                prompt = "I'm getting this error. Help me fix it:\n\n",
                icon = Icons.Default.Error,
                category = "Debug"
            ),
            PromptTemplate(
                title = "Data Structures",
                description = "Learn about arrays, trees, graphs, and more",
                prompt = "Explain the data structure ",
                icon = Icons.Default.AccountTree,
                category = "Learn"
            ),
            PromptTemplate(
                title = "API Design",
                description = "Design REST or GraphQL APIs",
                prompt = "Help me design an API for ",
                icon = Icons.Default.Api,
                category = "Build"
            )
        )
    }

    val filteredTemplates = if (selectedCategory == "All") templates
    else templates.filter { it.category == selectedCategory }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Explore",
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Category chips
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { selectedCategory = category },
                            label = { Text(category) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Green2,
                                selectedLabelColor = Color.White,
                                containerColor = SurfaceDark,
                                labelColor = Color.White.copy(alpha = 0.7f)
                            )
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(4.dp))
            }

            // Template cards
            items(filteredTemplates) { template ->
                PromptTemplateCard(
                    template = template,
                    onClick = { onStartChatWithPrompt(template.prompt) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun PromptTemplateCard(
    template: PromptTemplate,
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
            Surface(
                modifier = Modifier.size(44.dp),
                color = Primary.copy(alpha = 0.15f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        template.icon,
                        contentDescription = null,
                        tint = Primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = template.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Text(
                    text = template.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.3f)
            )
        }
    }
}
