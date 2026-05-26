package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.QuickNote
import com.example.data.Subject
import com.example.data.SubjectWithTasks
import com.example.data.StudyTask
import com.example.ui.DevEstudosViewModel
import java.util.*

// Bento Grid Design Color Palette
val BentoBg = Color(0xFFFDFCFF)
val BentoTextDark = Color(0xFF1A1C1E)
val BentoBluePrimary = Color(0xFF005FAF)
val BentoBlueLight = Color(0xFFD3E4FF)
val BentoBlueDarkText = Color(0xFF001C38)
val BentoPurpleLight = Color(0xFFEAE1F9)
val BentoPurpleDarkText = Color(0xFF21103E)
val BentoOrangeLight = Color(0xFFFFF1DB)
val BentoOrangeDarkText = Color(0xFF291800)
val BentoGrayLight = Color(0xFFF3F3F3)
val BentoGrayBorder = Color(0xFFE1E1E1)
val ObsidianBg = BentoBg // Keep matching fallback references
val CardSlate = Color(0xFFFFFFFF) // Map card colors cleanly to white card base or light background
val CyberGreen = BentoBluePrimary
val CyberPurple = BentoPurpleDarkText
val ActiveLine = BentoGrayBorder
val PriorityCritical = Color(0xFFBA1A1A) // Polished warning M3 Red
val PriorityHigh = Color(0xFFE65100)     // Rich orange
val PriorityMedium = Color(0xFFF9A825)   // Amber/gold
val PriorityLow = Color(0xFF5A5F6B)      // Neutral slate
val TextMuted = Color(0xFF5A5F6B)
val WhitePure = Color(0xFFFFFFFF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DevDashboardScreen(viewModel: DevEstudosViewModel) {
    val subjects by viewModel.subjectsState.collectAsState()
    val notes by viewModel.notesState.collectAsState()
    val remainingTime by viewModel.remainingTime.collectAsState()
    val timerRunning by viewModel.timerRunning.collectAsState()
    val isTimerBreak by viewModel.isTimerBreak.collectAsState()

    var activeTab by remember { mutableStateOf(0) } // 0: Trilha, 1: Planejador, 2: Anotações, 3: Foco, 4: Conquistas
    var selectedSubjectDetail by remember { mutableStateOf<SubjectWithTasks?>(null) }
    var showAddSubjectDialog by remember { mutableStateOf(false) }
    var showAddNoteDialog by remember { mutableStateOf(false) }
    var selectedPhaseFilter by remember { mutableStateOf("Todos") }

    val context = LocalContext.current

    val gamificationStats by viewModel.gamificationStatsState.collectAsState()
    val recommendations by viewModel.plannerRecommendations.collectAsState()

    // Calculate level progression metrics
    val totalTasksCount = subjects.sumOf { it.tasks.size }
    val completedTasksCount = subjects.sumOf { it.tasks.count { task -> task.isCompleted } }
    val progressPercentage = if (totalTasksCount > 0) {
        (completedTasksCount.toFloat() / totalTasksCount.toFloat() * 100).toInt()
    } else {
        0
    }

    val studentRole = when {
        progressPercentage <= 15 -> "Dev Inicial 🌱"
        progressPercentage <= 35 -> "Dev Aspirante 💻"
        progressPercentage <= 60 -> "Dev em Evolução ⚙️"
        progressPercentage <= 85 -> "Dev Quase Júnior 🛠️"
        else -> "Desenvolvedor Júnior Pronto! 🎉"
    }

    val progressAnim by animateFloatAsState(
        targetValue = if (totalTasksCount > 0) completedTasksCount.toFloat() / totalTasksCount.toFloat() else 0f,
        animationSpec = tween(durationMillis = 800)
    )

    // Flat Minimal Light Bento Background Canvas
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BentoBg)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Bento Custom Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "CAMINHO DEV JÚNIOR",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = BentoBluePrimary,
                        letterSpacing = 1.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "Olá, Dev! 👋",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = BentoTextDark,
                        letterSpacing = (-0.5).sp
                    )
                }
                // Header Avatar Pill
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(BentoBlueLight)
                        .border(2.dp, WhitePure, CircleShape)
                        .shadow(1.dp, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "L${gamificationStats.level}",
                        color = BentoBlueDarkText,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Level Progress Highlight Bento Box
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .shadow(3.dp, RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = BentoBluePrimary),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(WhitePure.copy(alpha = 0.2f))
                                .border(1.dp, WhitePure.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "TRILHA DEV JÚNIOR",
                                color = WhitePure,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Status",
                            tint = WhitePure.copy(alpha = 0.8f),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Nível ${gamificationStats.level} - ${gamificationStats.xp} / ${gamificationStats.level * 200} XP",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = WhitePure,
                        lineHeight = 26.sp
                    )
                    Text(
                        text = "Cargo atual: $studentRole (Estudos em $progressPercentage%)",
                        fontSize = 13.sp,
                        color = BentoBlueLight.copy(alpha = 0.9f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Progress Track bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(WhitePure.copy(alpha = 0.2f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progressAnim)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(4.dp))
                                .background(WhitePure)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "$completedTasksCount de $totalTasksCount tarefas da trilha dominadas",
                            fontSize = 11.sp,
                            color = WhitePure.copy(alpha = 0.7f)
                        )
                        Text(
                            text = "Rumo ao Próximo Nível!",
                            fontSize = 11.sp,
                            color = WhitePure,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tab Content
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
            ) {
                when (activeTab) {
                    0 -> TrilhaTab(
                        subjects = subjects,
                        selectedPhaseFilter = selectedPhaseFilter,
                        onFilterChange = { selectedPhaseFilter = it },
                        onSubjectClick = { selectedSubjectDetail = it },
                        onStatusChange = { subject, status -> viewModel.updateSubjectStatus(subject, status) },
                        onDeleteClick = { viewModel.deleteSubject(it) }
                    )
                    1 -> PlanejadorTab(
                        recommendations = recommendations,
                        onSubjectClick = { sId ->
                            val match = subjects.firstOrNull { it.subject.id == sId }
                            if (match != null) {
                                selectedSubjectDetail = match
                            }
                        }
                    )
                    2 -> DiariosTab(
                        notes = notes,
                        onAddNoteClick = { showAddNoteDialog = true },
                        onDeleteClick = { viewModel.deleteNote(it) }
                    )
                    3 -> FocoTab(
                        remainingTime = remainingTime,
                        timerRunning = timerRunning,
                        isBreak = isTimerBreak,
                        onStartPause = {
                            if (timerRunning) viewModel.pauseTimer() else viewModel.startTimer()
                        },
                        onReset = { viewModel.resetTimer() },
                        onSkip = { viewModel.skipTimer() }
                    )
                    4 -> ConquistasTab(
                        stats = gamificationStats,
                        subjects = subjects
                    )
                }
            }

            // Bento-Styled Bottom Custom Navigation row matching HTML aesthetic exactly
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .background(WhitePure)
                    .border(BorderStroke(1.dp, BentoGrayBorder.copy(alpha = 0.5f)))
                    .padding(horizontal = 4.dp)
                    .navigationBarsPadding(),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BentoTabButton(
                    selected = activeTab == 0,
                    icon = Icons.Default.List,
                    label = "Trilha",
                    selectedBg = BentoBlueLight,
                    selectedIconColor = BentoBlueDarkText,
                    onClick = { activeTab = 0 }
                )

                BentoTabButton(
                    selected = activeTab == 1,
                    icon = Icons.Default.Build,
                    label = "Planejar",
                    selectedBg = BentoBlueLight,
                    selectedIconColor = BentoBlueDarkText,
                    onClick = { activeTab = 1 }
                )

                BentoTabButton(
                    selected = activeTab == 2,
                    icon = Icons.Default.Edit,
                    label = "Anotar",
                    selectedBg = BentoPurpleLight,
                    selectedIconColor = BentoPurpleDarkText,
                    onClick = { activeTab = 2 }
                )

                BentoTabButton(
                    selected = activeTab == 3,
                    icon = Icons.Default.Favorite,
                    label = "Foco",
                    selectedBg = BentoOrangeLight,
                    selectedIconColor = BentoOrangeDarkText,
                    onClick = { activeTab = 3 }
                )

                BentoTabButton(
                    selected = activeTab == 4,
                    icon = Icons.Default.Star,
                    label = "Conquistas",
                    selectedBg = BentoPurpleLight,
                    selectedIconColor = BentoPurpleDarkText,
                    onClick = { activeTab = 4 }
                )
            }
        }

        // Add Floating Actions based on active tab with beautiful themed pill colors
        if (activeTab == 0) {
            FloatingActionButton(
                onClick = { showAddSubjectDialog = true },
                containerColor = BentoBluePrimary,
                contentColor = WhitePure,
                shape = CircleShape,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 96.dp, end = 20.dp) // Adjusted to overlap nicely above bottom bar
                    .size(56.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Adicionar Matéria ou Tópico", modifier = Modifier.size(28.dp))
            }
        } else if (activeTab == 2) {
            FloatingActionButton(
                onClick = { showAddNoteDialog = true },
                containerColor = BentoPurpleDarkText,
                contentColor = WhitePure,
                shape = CircleShape,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 96.dp, end = 20.dp) // Adjusted to overlap nicely above bottom bar
                    .size(56.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nova Anotação", modifier = Modifier.size(28.dp))
            }
        }
    }

    // --- DIALOGS & DRAWERS ---

    // 1. Detailed Subject view (Checklist of tasks)
    selectedSubjectDetail?.let { currentDetail ->
        // Retrieve fresh database reactive details to display updated progress instantly
        val currentSubjectStream = remember(currentDetail.subject.id, subjects) {
            subjects.firstOrNull { it.subject.id == currentDetail.subject.id }
        } ?: currentDetail

        AlertDialog(
            onDismissRequest = { selectedSubjectDetail = null },
            confirmButton = {
                TextButton(onClick = { selectedSubjectDetail = null }) {
                    Text("OK", color = BentoBluePrimary, fontWeight = FontWeight.Bold)
                }
            },
            title = {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = currentSubjectStream.subject.title,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = BentoBluePrimary
                        )
                        BadgeByPriority(currentSubjectStream.subject.priority)
                    }
                    Text(
                        text = currentSubjectStream.subject.phase,
                        fontSize = 12.sp,
                        color = TextMuted,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = currentSubjectStream.subject.description,
                        fontSize = 14.sp,
                        color = BentoTextDark,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    HorizontalDivider(color = BentoGrayBorder, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Roteiro / Checklist de Estudos:",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = BentoPurpleDarkText,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )

                    if (currentSubjectStream.tasks.isEmpty()) {
                        Text(
                            text = "Nenhum tópico adicionado a essa matéria.",
                            fontSize = 13.sp,
                            color = TextMuted
                        )
                    } else {
                        currentSubjectStream.tasks.forEach { task ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.toggleTask(task) }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = task.isCompleted,
                                    onCheckedChange = { viewModel.toggleTask(task) },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = BentoBluePrimary,
                                        uncheckedColor = TextMuted.copy(alpha = 0.5f)
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = task.title,
                                    fontSize = 14.sp,
                                    color = if (task.isCompleted) TextMuted else BentoTextDark,
                                    textDecoration = if (task.isCompleted) androidx.compose.ui.text.style.TextDecoration.LineThrough else null,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(onClick = { viewModel.deleteTask(task.id) }) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Deletar tarefa",
                                        tint = PriorityCritical.copy(alpha = 0.8f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = BentoGrayBorder, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Input to add a new task to this subject
                    var newTaskTitle by remember { mutableStateOf("") }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newTaskTitle,
                            onValueChange = { newTaskTitle = it },
                            label = { Text("Adicionar Tópico à Matéria", fontSize = 12.sp) },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = BentoTextDark,
                                unfocusedTextColor = BentoTextDark,
                                focusedBorderColor = BentoBluePrimary,
                                unfocusedBorderColor = BentoGrayBorder,
                                focusedLabelColor = BentoBluePrimary,
                                unfocusedLabelColor = TextMuted
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (newTaskTitle.isNotBlank()) {
                                    viewModel.addTask(currentSubjectStream.subject.id, newTaskTitle)
                                    newTaskTitle = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BentoBluePrimary)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add", tint = WhitePure)
                        }
                    }

                    if (currentSubjectStream.subject.resourcesUrl.isNotBlank()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Links de Referência:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = BentoBluePrimary
                        )
                        Text(
                            text = currentSubjectStream.subject.resourcesUrl,
                            fontSize = 12.sp,
                            color = BentoBlueDarkText,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            },
            containerColor = WhitePure,
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
        )
    }

    // 2. Add Subject Dialog
    if (showAddSubjectDialog) {
        var title by remember { mutableStateOf("") }
        var description by remember { mutableStateOf("") }
        var priority by remember { mutableStateOf("Média") }
        var phase by remember { mutableStateOf("Fundamentos") }
        var linkUrl by remember { mutableStateOf("") }
        var rawTasks by remember { mutableStateOf("") }

        val priorities = listOf("Crítica", "Alta", "Média", "Baixa")
        val phases = listOf("Fundamentos", "Tecnologias Core", "Especialização/Prática", "Preparação Jr")

        AlertDialog(
            onDismissRequest = { showAddSubjectDialog = false },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            val taskList = rawTasks.split(",").map { it.trim() }.filter { it.isNotBlank() }
                            viewModel.addSubject(title, description, priority, phase, linkUrl, taskList)
                            showAddSubjectDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BentoBluePrimary)
                ) {
                    Text("Salvar", color = WhitePure, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddSubjectDialog = false }) {
                    Text("Cancelar", color = TextMuted, fontWeight = FontWeight.SemiBold)
                }
            },
            title = { Text("Estúdio Dev: Nova Matéria", color = BentoBluePrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Título da Matéria") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = BentoTextDark,
                            unfocusedTextColor = BentoTextDark,
                            focusedBorderColor = BentoBluePrimary,
                            unfocusedBorderColor = BentoGrayBorder,
                            focusedLabelColor = BentoBluePrimary,
                            unfocusedLabelColor = TextMuted
                        )
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Descrição / Focos") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = BentoTextDark,
                            unfocusedTextColor = BentoTextDark,
                            focusedBorderColor = BentoBluePrimary,
                            unfocusedBorderColor = BentoGrayBorder,
                            focusedLabelColor = BentoBluePrimary,
                            unfocusedLabelColor = TextMuted
                        )
                    )

                    Text("Prioridade de Estudo:", color = BentoTextDark, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(priorities) { item ->
                            val selected = priority == item
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (selected) selectionPriorityColor(item) else BentoGrayLight)
                                    .clickable { priority = item }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = item,
                                    color = if (selected) WhitePure else BentoTextDark,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    Text("Nível / Fase de Trilha:", color = BentoTextDark, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(phases) { item ->
                            val selected = phase == item
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (selected) BentoPurpleDarkText else BentoGrayLight)
                                    .clickable { phase = item }
                                    .padding(horizontal = 12.dp, vertical = 7.dp)
                            ) {
                                Text(
                                    text = item,
                                    color = if (selected) WhitePure else BentoTextDark,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = linkUrl,
                        onValueChange = { linkUrl = it },
                        label = { Text("Links de Referência") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = BentoTextDark,
                            unfocusedTextColor = BentoTextDark,
                            focusedBorderColor = BentoBluePrimary,
                            unfocusedBorderColor = BentoGrayBorder,
                            focusedLabelColor = BentoBluePrimary,
                            unfocusedLabelColor = TextMuted
                        )
                    )

                    OutlinedTextField(
                        value = rawTasks,
                        onValueChange = { rawTasks = it },
                        label = { Text("Checklist Inicial (Separado por vírgula)") },
                        placeholder = { Text("Tópico A, Tópico B, Tópico C...") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = BentoTextDark,
                            unfocusedTextColor = BentoTextDark,
                            focusedBorderColor = BentoBluePrimary,
                            unfocusedBorderColor = BentoGrayBorder,
                            focusedLabelColor = BentoBluePrimary,
                            unfocusedLabelColor = TextMuted
                        )
                    )
                }
            },
            containerColor = WhitePure,
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp)
        )
    }

    // 3. Add Note Dialog
    if (showAddNoteDialog) {
        var noteTitle by remember { mutableStateOf("") }
        var noteContent by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddNoteDialog = false },
            confirmButton = {
                Button(
                    onClick = {
                        if (noteTitle.isNotBlank() || noteContent.isNotBlank()) {
                            viewModel.addNote(noteTitle, noteContent)
                            showAddNoteDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BentoPurpleDarkText)
                ) {
                    Text("Criar", color = WhitePure, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddNoteDialog = false }) {
                    Text("Cancelar", color = TextMuted, fontWeight = FontWeight.SemiBold)
                }
            },
            title = { Text("Nova Anotação / Lembrete Dev", color = BentoPurpleDarkText, fontWeight = FontWeight.Bold, fontSize = 20.sp) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = noteTitle,
                        onValueChange = { noteTitle = it },
                        label = { Text("Título / Assunto") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = BentoTextDark,
                            unfocusedTextColor = BentoTextDark,
                            focusedBorderColor = BentoPurpleDarkText,
                            unfocusedBorderColor = BentoGrayBorder,
                            focusedLabelColor = BentoPurpleDarkText,
                            unfocusedLabelColor = TextMuted
                        )
                    )

                    OutlinedTextField(
                        value = noteContent,
                        onValueChange = { noteContent = it },
                        label = { Text("Código, Dica ou Snippet") },
                        minLines = 4,
                        maxLines = 8,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = BentoTextDark,
                            unfocusedTextColor = BentoTextDark,
                            focusedBorderColor = BentoPurpleDarkText,
                            unfocusedBorderColor = BentoGrayBorder,
                            focusedLabelColor = BentoPurpleDarkText,
                            unfocusedLabelColor = TextMuted
                        )
                    )
                }
            },
            containerColor = WhitePure,
            shape = RoundedCornerShape(24.dp)
        )
    }
}

// Helper scroll handles
@Composable
fun rememberScrollState(): androidx.compose.foundation.ScrollState {
    return androidx.compose.foundation.rememberScrollState()
}

// Selection colors for priority list
fun selectionPriorityColor(priority: String): Color {
    return when (priority) {
        "Crítica" -> PriorityCritical
        "Alta" -> PriorityHigh
        "Média" -> PriorityMedium
        else -> PriorityLow
    }
}

// --- TAB COMPONENTS ---

// 1. Roadmap Tab (Trilha de Estudos)
@Composable
fun TrilhaTab(
    subjects: List<SubjectWithTasks>,
    selectedPhaseFilter: String,
    onFilterChange: (String) -> Unit,
    onSubjectClick: (SubjectWithTasks) -> Unit,
    onStatusChange: (Subject, String) -> Unit,
    onDeleteClick: (Subject) -> Unit
) {
    val phases = listOf("Todos", "Fundamentos", "Tecnologias Core", "Especialização/Prática", "Preparação Jr")

    Column(modifier = Modifier.fillMaxSize()) {
        // Horizontally Scrollable Phase Filter Section
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(phases) { phase ->
                val isSelected = selectedPhaseFilter == phase
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) BentoBlueLight else WhitePure)
                        .border(1.dp, if (isSelected) BentoBluePrimary else BentoGrayBorder, RoundedCornerShape(20.dp))
                        .clickable { onFilterChange(phase) }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = phase,
                        color = if (isSelected) BentoBlueDarkText else BentoTextDark.copy(alpha = 0.7f),
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }

        // List of filtered subjects
        val filteredSubjects = if (selectedPhaseFilter == "Todos") {
            subjects
        } else {
            subjects.filter { it.subject.phase == selectedPhaseFilter }
        }

        if (filteredSubjects.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.Star,
                    contentDescription = null,
                    tint = TextMuted.copy(alpha = 0.3f),
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Nenhuma matéria localizada para esta fase.",
                    color = BentoTextDark,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Toque no botão '+' no canto inferior para acrescentar seus próprios estudos!",
                    color = TextMuted,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp) // Offset for FAB
            ) {
                // Group by Phase to look incredibly structured just like a real road-map curriculum!
                val grouped = filteredSubjects.groupBy { it.subject.phase }
                grouped.forEach { (phaseGroup, listInPhase) ->
                    item {
                        Text(
                            text = phaseGroup.uppercase(Locale.getDefault()),
                            color = BentoPurpleDarkText,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(start = 20.dp, top = 16.dp, bottom = 8.dp)
                        )
                    }

                    items(listInPhase) { item ->
                        SubjectCard(
                            subjectWithTasks = item,
                            onClick = { onSubjectClick(item) },
                            onStatusChange = { newStatus -> onStatusChange(item.subject, newStatus) },
                            onDeleteClick = { onDeleteClick(item.subject) }
                        )
                    }
                }
            }
        }
    }
}

// Subject Card Composable representation
@Composable
fun SubjectCard(
    subjectWithTasks: SubjectWithTasks,
    onClick: () -> Unit,
    onStatusChange: (String) -> Unit,
    onDeleteClick: () -> Unit
) {
    val totalTasks = subjectWithTasks.tasks.size
    val completedTasks = subjectWithTasks.tasks.count { it.isCompleted }
    val progress = if (totalTasks > 0) completedTasks.toFloat() / totalTasks.toFloat() else 0f

    val statusOptions = listOf("A Estudar", "Trabalhando", "Concluído")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .shadow(2.dp, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = WhitePure),
        border = BorderStroke(1.dp, BentoGrayBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Priority Tag and Title row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BadgeByPriority(subjectWithTasks.subject.priority)
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Remover Matéria",
                        tint = TextMuted.copy(alpha = 0.5f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subjectWithTasks.subject.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = BentoTextDark,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = subjectWithTasks.subject.description,
                fontSize = 13.sp,
                color = TextMuted,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            // Progress Indicators
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = BentoBluePrimary,
                    trackColor = BentoGrayLight
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "$completedTasks/$totalTasks tópicos",
                    color = BentoTextDark,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Study Status selectors
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                statusOptions.forEach { statusText ->
                    val selected = subjectWithTasks.subject.status == statusText
                    val bg = if (selected) BentoBlueLight else BentoGrayLight
                    val borderFactor = if (selected) BentoBluePrimary else Color.Transparent
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(bg)
                            .border(1.dp, borderFactor, RoundedCornerShape(8.dp))
                            .clickable { onStatusChange(statusText) }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = statusText,
                            color = if (selected) BentoBlueDarkText else TextMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// ----------------- ADAPTIVE PLANNER & GAMIFICATION SCREENS -----------------

@Composable
fun PlanejadorTab(
    recommendations: List<com.example.ui.PlannerRecommendation>,
    onSubjectClick: (Int) -> Unit
) {
    var selectedBlocks by remember { mutableStateOf(2) } // default 2 Pomodoro blocks available for study today

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // 1. Bento header explaining the logic
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = BentoPurpleLight),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Planejador Inteligente Adaptativo 🧠",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = BentoPurpleDarkText
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Analisamos seu percurso de aprendizagem de forma contínua para recomendar a otimização de tempo perfeita para o perfil Júnior de desenvolvimento.",
                        fontSize = 12.sp,
                        color = BentoPurpleDarkText.copy(alpha = 0.8f),
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // 2. Select study hours/blocks today
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardSlate),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, BentoGrayBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Horário Disponível para Hoje",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = BentoTextDark
                    )
                    Text(
                        text = "Selecione quantos blocos Pomodoro (25 min) você tem hoje:",
                        fontSize = 11.sp,
                        color = TextMuted,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (blocks in 1..4) {
                            val isSelected = selectedBlocks == blocks
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) BentoBluePrimary else BentoGrayLight)
                                    .clickable { selectedBlocks = blocks }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "$blocks",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) WhitePure else BentoTextDark
                                    )
                                    Text(
                                        text = "${blocks * 25}min",
                                        fontSize = 9.sp,
                                        color = if (isSelected) BentoBlueLight else TextMuted
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Cronograma Sugerido Ajustado (Adaptativo):",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = BentoBluePrimary
                    )

                    // Suggest schedule depending on blocks count and recommendations available
                    val activeRecommendations = recommendations.filter { it.urgencyLevel == "Alta" || it.urgencyLevel == "Média" }
                    val currentRecommendationList = if (activeRecommendations.isNotEmpty()) activeRecommendations else recommendations

                    if (currentRecommendationList.isEmpty()) {
                        Text(
                            text = "Nenhuma recomendação pendente. Parabéns! Todas as suas metas foram atingidas.",
                            fontSize = 12.sp,
                            color = TextMuted,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            for (i in 0 until selectedBlocks) {
                                val matchRec = currentRecommendationList.getOrNull(i % currentRecommendationList.size)
                                if (matchRec != null) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(BentoGrayLight, RoundedCornerShape(8.dp))
                                            .padding(horizontal = 12.dp, vertical = 8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(BentoBlueLight),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "${i + 1}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = BentoBlueDarkText
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "Foco (25 min): ${matchRec.subjectTitle}",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = BentoTextDark
                                            )
                                            Text(
                                                text = "Ajuste Recomendado: ${matchRec.reason}",
                                                fontSize = 10.sp,
                                                color = TextMuted
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Recommendations lists
        item {
            Text(
                text = "Sugestões de Ajuste do Cronograma:",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = BentoTextDark,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            )
        }

        if (recommendations.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardSlate),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, BentoGrayBorder)
                ) {
                    Box(modifier = Modifier.padding(20.dp), contentAlignment = Alignment.Center) {
                        Text("Adicione matérias para começar a receber orientações adaptativas.", fontSize = 13.sp, color = TextMuted)
                    }
                }
            }
        } else {
            items(recommendations) { rec ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = CardSlate),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, BentoGrayBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSubjectClick(rec.subjectId) }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = rec.subjectTitle,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = BentoBluePrimary,
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            
                            val (pillBg, pillText, label) = when (rec.urgencyLevel) {
                                "Alta" -> Triple(PriorityCritical.copy(alpha = 0.15f), PriorityCritical, "FOCO URGENTE")
                                "Média" -> Triple(PriorityHigh.copy(alpha = 0.15f), PriorityHigh, "INTERMEDIÁRIA")
                                else -> Triple(PriorityLow.copy(alpha = 0.15f), PriorityLow, "REVISÃO")
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(pillBg)
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = label,
                                    color = pillText,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Por que focar nesta área: ${rec.reason}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = BentoTextDark
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = rec.suggestedAction,
                            fontSize = 11.sp,
                            color = TextMuted,
                            lineHeight = 15.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Progresso atual: ${rec.progress}%",
                                fontSize = 10.sp,
                                color = TextMuted,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "Verificar Tópicos ➔",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = BentoPurpleDarkText
                            )
                        }
                    }
                }
            }
        }
    }
}

data class GameBadge(
    val id: String,
    val title: String,
    val description: String,
    val iconChar: String,
    val activeColor: Color
)

@Composable
fun StatItem(count: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = BentoTextDark
        )
        Text(
            text = label,
            fontSize = 10.sp,
            color = TextMuted,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun UnlockedFeatureItem(
    levelRequired: Int,
    userLevel: Int,
    title: String,
    unlockedDescription: String,
    lockedDescription: String
) {
    val unlocked = userLevel >= levelRequired
    Card(
        colors = CardDefaults.cardColors(containerColor = CardSlate),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, BentoGrayBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (unlocked) BentoBluePrimary else TextMuted
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (unlocked) BentoBlueLight else BentoGrayLight)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (unlocked) "LIBERADO" else "REQ. NÍVEL $levelRequired",
                        color = if (unlocked) BentoBlueDarkText else TextMuted,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (unlocked) unlockedDescription else lockedDescription,
                fontSize = 11.sp,
                color = if (unlocked) BentoTextDark else TextMuted,
                lineHeight = 15.sp
            )
        }
    }
}

fun <T> androidx.compose.foundation.lazy.LazyListScope.gridItems(
    items: List<T>,
    nColumns: Int,
    horizontalSpacing: androidx.compose.ui.unit.Dp = 8.dp,
    content: @Composable (T) -> Unit
) {
    val rows = items.chunked(nColumns)
    items(rows) { rowItems ->
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(horizontalSpacing)
        ) {
            rowItems.forEach { item ->
                Box(modifier = Modifier.weight(1f)) {
                    content(item)
                }
            }
            val emptyColumns = nColumns - rowItems.size
            if (emptyColumns > 0) {
                Spacer(modifier = Modifier.weight(emptyColumns.toFloat()))
            }
        }
    }
}

@Composable
fun ConquistasTab(
    stats: com.example.data.UserStats,
    subjects: List<com.example.data.SubjectWithTasks>
) {
    val badgeTemplateList = listOf(
        GameBadge("badge_first_step", "Primeiro Passo 🚀", "Terminar seu primeiro tópico do roteiro profissional.", "🌱", BentoBluePrimary),
        GameBadge("badge_task_master", "Mestre de Código 💻", "Dominar pelo menos 5 tópicos de estudos.", "🤓", BentoPurpleDarkText),
        GameBadge("badge_timer_master", "Foco Absoluto ⏱️", "Completar o seu primeiro Pomodoro produtivo.", "🎯", PriorityHigh),
        GameBadge("badge_steel_focus", "Foco Inabalável 🌋", "Concluir 3 ou mais Pomodoros de concentração.", "⚡", PriorityCritical),
        GameBadge("badge_roadmap_pioneer", "Pioneiro da Trilha 🗺️", "Terminar uma matéria por inteiro.", "🌟", PriorityMedium),
        GameBadge("badge_fullstack_challenger", "Junior Competente 🏆", "Concluir 3 ou mais matérias por inteiro.", "👑", BentoBlueDarkText),
        GameBadge("badge_scribe", "Escriba Dev ✒️", "Fazer a sua primeira anotação técnica de códigos.", "📜", BentoPurpleDarkText)
    )

    val currentBadges = stats.earnedBadges.split(",").filter { it.isNotBlank() }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // 1. Level progress indicator card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardSlate),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, BentoGrayBorder),
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "RANKING DE ESTUDOS JÚNIOR 🎮",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = BentoBluePrimary,
                        letterSpacing = 1.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    // XP Arc Gauge Circle
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(130.dp)) {
                        val xpToNext = stats.level * 200
                        val progressFactor = (stats.xp.toFloat() / xpToNext.toFloat()).coerceIn(0f, 1f)

                        Canvas(modifier = Modifier.size(120.dp)) {
                            drawArc(
                                color = BentoGrayLight,
                                startAngle = -90f,
                                sweepAngle = 360f,
                                useCenter = false,
                                style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                            )
                            drawArc(
                                brush = Brush.sweepGradient(listOf(BentoBluePrimary, BentoPurpleDarkText, BentoBluePrimary)),
                                startAngle = -90f,
                                sweepAngle = progressFactor * 360f,
                                useCenter = false,
                                style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Nível",
                                fontSize = 11.sp,
                                color = TextMuted,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "${stats.level}",
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Bold,
                                color = BentoTextDark,
                                letterSpacing = (-1).sp
                            )
                            Text(
                                text = "${stats.xp} / $xpToNext XP",
                                fontSize = 11.sp,
                                color = BentoBluePrimary,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Basic statistics counters row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        StatItem(count = "${stats.completedTasks}", label = "Tópicos")
                        StatItem(count = "${stats.completedPomodoros}", label = "Pomodoros")
                        StatItem(count = "${stats.completedSubjects}", label = "Matérias")
                        StatItem(count = "${currentBadges.size}", label = "Distintivos")
                    }
                }
            }
        }

        // 2. Badge showcase header
        item {
            Text(
                text = "Medalhas e Distintivos Conquistados",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = BentoTextDark,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        gridItems(badgeTemplateList, nColumns = 2) { badge ->
            val hasEarned = currentBadges.contains(badge.id)
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (hasEarned) CardSlate else BentoGrayLight.copy(alpha = 0.4f)
                ),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, if (hasEarned) badge.activeColor.copy(alpha = 0.6f) else BentoGrayBorder.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(if (hasEarned) badge.activeColor.copy(alpha = 0.15f) else BentoGrayBorder.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (hasEarned) badge.iconChar else "🔒",
                            fontSize = 24.sp,
                            modifier = Modifier.alpha(if (hasEarned) 1f else 0.4f)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = badge.title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (hasEarned) badge.activeColor else TextMuted,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = badge.description,
                        fontSize = 10.sp,
                        color = TextMuted,
                        textAlign = TextAlign.Center,
                        lineHeight = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        // 3. Unlocks panel header
        item {
            Text(
                text = "Novas Funcionalidades e Conteúdos",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = BentoTextDark,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        item {
            UnlockedFeatureItem(
                levelRequired = 2,
                userLevel = stats.level,
                title = "Booster de Intervalos ⚡",
                unlockedDescription = "Liberado! Configurações completadas. Seus próximos ciclos Pomodoro renderão o dobro do rendimento prático.",
                lockedDescription = "Alcance o Nível 2 para habilitar micro-intervalos de tempos dinâmicos."
            )
        }

        item {
            UnlockedFeatureItem(
                levelRequired = 3,
                userLevel = stats.level,
                title = "Modo de Foco Supremo 🌋",
                unlockedDescription = "Liberado! Timer de 50 minutos concentrados para programadores de alto impacto.",
                lockedDescription = "Alcance o Nível 3 para habilitar o timer supremo recomendado para maratonas de código."
            )
        }

        // Level 4 feature - Technical Quizzes
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardSlate),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, BentoGrayBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Simulador de Entrevistas Júnior 💻",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = BentoPurpleDarkText
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (stats.level >= 4) BentoPurpleLight else BentoGrayLight)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (stats.level >= 4) "LIBERADO" else "REQ. NÍVEL 4",
                                color = if (stats.level >= 4) BentoPurpleDarkText else TextMuted,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    if (stats.level < 4) {
                        Text(
                            text = "Estude, complete tópicos do seu roteiro e realize sessões Pomodoro para atingir o Nível 4 e liberar o quiz interativo com perguntas frequentes de entrevistas de TI!",
                            fontSize = 11.sp,
                            color = TextMuted,
                            lineHeight = 15.sp
                        )
                    } else {
                        var currentQuestionIndex by remember { mutableStateOf(0) }
                        var revealAnswer by remember { mutableStateOf(false) }

                        val interviewQuestions = listOf(
                            Pair("Qual a diferença entre GET e POST numa API REST?", "GET solicita representações do recurso especificado e deve apenas recuperar dados sem causar efeitos colaterais. POST envia dados para processamento ao recurso especificado, frequentemente resultando na criação de novos registros ou efeitos colaterais no servidor."),
                            Pair("O que significa idempotência em métodos HTTP?", "Significa que fazer múltiplas requisições idênticas deve ter o mesmo efeito que fazer uma única requisição. GET, PUT, DELETE são idempotentes; POST não é."),
                            Pair("O que é herança e como difere de uma interface em POO?", "Herança permite que uma subclasse herde o comportamento e estado (campos) de uma superclasse, estabelecendo relação 'é um'. Interface estipula contratos (assinaturas de método) que classes devem implementar, permitindo herança múltipla de interfaces no Kotlin."),
                            Pair("O que é o Git Rebase e quando difere do Merge?", "Merge cria um commit de mesclagem (unindo históricos) e preserva a história original como aconteceu. Rebase aplica os commits do branch atual diretamente no topo de outro branch, criando um histórico linear limpo."),
                            Pair("Como funciona o recomposition no Jetpack Compose?", "O Jetpack Compose reconstrói apenas os componentes da interface cujos parâmetros ou dependências (como States) sofreram alteração, ignorando inteligentemente o restante da árvore para máxima performance.")
                        )

                        Text(
                            text = "Pratique respondendo mentalmente e clique em Revelar Resposta para bater o gabarito!",
                            fontSize = 11.sp,
                            color = TextMuted,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(BentoGrayLight, RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "Pergunta ${currentQuestionIndex + 1} de ${interviewQuestions.size}:",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = BentoBluePrimary,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = interviewQuestions[currentQuestionIndex].first,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = BentoTextDark,
                                lineHeight = 16.sp
                            )

                            if (revealAnswer) {
                                Spacer(modifier = Modifier.height(8.dp))
                                HorizontalDivider(color = BentoGrayBorder)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Resposta de Gabarito:",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = PriorityHigh,
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = interviewQuestions[currentQuestionIndex].second,
                                    fontSize = 12.sp,
                                    color = BentoTextDark,
                                    lineHeight = 16.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = { revealAnswer = !revealAnswer },
                                colors = ButtonDefaults.buttonColors(containerColor = BentoPurpleDarkText),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = if (revealAnswer) "Ocultar Resposta" else "Revelar Resposta",
                                    fontSize = 11.sp,
                                    color = WhitePure,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                TextButton(
                                    onClick = {
                                        revealAnswer = false
                                        currentQuestionIndex = (currentQuestionIndex - 1 + interviewQuestions.size) % interviewQuestions.size
                                    },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("Anterior", fontSize = 11.sp, color = BentoBluePrimary)
                                }
                                TextButton(
                                    onClick = {
                                        revealAnswer = false
                                        currentQuestionIndex = (currentQuestionIndex + 1) % interviewQuestions.size
                                    },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("Próxima", fontSize = 11.sp, color = BentoBluePrimary)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Level 5 feature - GitHub README Template Generator
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = CardSlate),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, BentoGrayBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Gerador de README do GitHub 📝",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = BentoBlueDarkText
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (stats.level >= 5) BentoBlueLight else BentoGrayLight)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (stats.level >= 5) "LIBERADO" else "REQ. NÍVEL 5",
                                color = if (stats.level >= 5) BentoBlueDarkText else TextMuted,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    if (stats.level < 5) {
                        Text(
                            text = "Alcance o Nível 5 para gerar um modelo elegante de arquivo README.md documentando seu progresso atual na trilha para colocar direto no seu portfólio profissional!",
                            fontSize = 11.sp,
                            color = TextMuted,
                            lineHeight = 15.sp
                        )
                    } else {
                        val completedList = subjects.filter { it.subject.status == "Concluído" }
                            .joinToString("\n") { "- [x] ${it.subject.title}" }
                        val safeCompletedList = if (completedList.isNotBlank()) completedList else "- [ ] Começando meus primeiros estudos práticos"

                        val readmeText = """
# 🚀 Trilhas de Estudos para Dev Júnior

Repositório estruturado de progresso técnico em desenvolvimento.

## 🎓 Concluídos com Sucesso:
${safeCompletedList}

## ⚡ Estatísticas Acumuladas:
- Nível Profissional: Nível ${stats.level} (Dev Gamer)
- XP Total de Conhecimento: ${stats.xp} pontos de XP
- Ciclos Pomodoro Concluídos: ${stats.completedPomodoros} blocos

_Gerado dinamicamente no aplicativo Dev Estudos._
""".trimIndent()

                        Text(
                            text = "Copie o template abaixo e suba no seu GitHub junto do seu repositório de portfólio:",
                            fontSize = 11.sp,
                            color = TextMuted,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(BentoGrayLight, RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = readmeText,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = BentoTextDark,
                                lineHeight = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

// Badge color representation based on study priority
@Composable
fun BadgeByPriority(priority: String) {
    val colorFactor = when (priority) {
        "Crítica" -> PriorityCritical
        "Alta" -> PriorityHigh
        "Média" -> PriorityMedium
        else -> PriorityLow
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(colorFactor.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = priority.uppercase(Locale.getDefault()),
            color = colorFactor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}

// 2. Diários Tab (Anotações Cheat Sheet)
@Composable
fun DiariosTab(
    notes: List<QuickNote>,
    onAddNoteClick: () -> Unit,
    onDeleteClick: (QuickNote) -> Unit
) {
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Anotações e Cheat Sheets 📓",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = BentoTextDark
                )
                Text(
                    text = "Guarde comandos, insights e lembretes rápidos de código.",
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }
        }

        if (notes.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = null,
                    tint = TextMuted.copy(alpha = 0.3f),
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Seu espelho de código está em branco.",
                    color = BentoTextDark,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Clique em '+' no canto inferior para salvar seus primeiros truques e hacks técnicos de estudo!",
                    color = TextMuted,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp, start = 16.dp, end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(notes) { note ->
                    NoteCard(
                        note = note,
                        onDelete = { onDeleteClick(note) },
                        onCopy = {
                            val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                            val clip = android.content.ClipData.newPlainText("Copiado!", note.content)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Snippet copiado para área de transferência! 📋", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun NoteCard(
    note: QuickNote,
    onDelete: () -> Unit,
    onCopy: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().shadow(1.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = WhitePure),
        border = BorderStroke(1.dp, BentoGrayBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = note.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = BentoPurpleDarkText,
                    modifier = Modifier.weight(1f)
                )
                Row {
                    IconButton(onClick = onCopy) {
                        Icon(
                            Icons.Default.Check, // In standard m3 package
                            contentDescription = "Copiar anotação",
                            tint = BentoBluePrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Deletar",
                            tint = PriorityCritical.copy(alpha = 0.8f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Code block styler for developer experience
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(BentoGrayLight)
                    .border(1.dp, BentoGrayBorder, RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = note.content,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    color = BentoTextDark,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

// 3. Foco Tab (Pomodoro Timer)
@Composable
fun FocoTab(
    remainingTime: Int,
    timerRunning: Boolean,
    isBreak: Boolean,
    onStartPause: () -> Unit,
    onReset: () -> Unit,
    onSkip: () -> Unit
) {
    val durationSeconds = if (isBreak) 5 * 60 else 25 * 60
    val progressFactor = remainingTime.toFloat() / durationSeconds.toFloat()

    val seconds = remainingTime % 60
    val minutes = remainingTime / 60
    val displayedTimer = String.format("%02d:%02d", minutes, seconds)

    val subtitle = if (isBreak) "☕ Intervalo Merecido" else "🎯 Sessão de Código e Foco"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = subtitle,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = if (isBreak) BentoBluePrimary else BentoPurpleDarkText,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Rounded Dynamic canvas circle
        Box(
            modifier = Modifier
                .size(240.dp)
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 14.dp.toPx()

                // Background Ring
                drawArc(
                    color = BentoGrayLight,
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth)
                )

                // Foreground Ring
                drawArc(
                    color = if (isBreak) BentoBluePrimary else BentoPurpleDarkText,
                    startAngle = -90f,
                    sweepAngle = 360f * progressFactor,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = displayedTimer,
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = BentoTextDark
                )
                Text(
                    text = "minutos",
                    fontSize = 12.sp,
                    color = TextMuted,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Timer Control buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth(0.85f)
        ) {
            // Play/Pause button
            Button(
                onClick = onStartPause,
                modifier = Modifier
                    .weight(1.5f)
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (timerRunning) PriorityCritical else BentoBluePrimary
                ),
                shape = RoundedCornerShape(26.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (timerRunning) {
                        CustomPauseIndicator()
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Pausar", color = WhitePure, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    } else {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = WhitePure)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Iniciar Foco", color = WhitePure, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }

            // Reset button
            IconButton(
                onClick = onReset,
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(BentoGrayLight)
                    .border(1.dp, BentoGrayBorder, CircleShape)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Reiniciar", tint = BentoTextDark)
            }

            // Skip mode button
            IconButton(
                onClick = onSkip,
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(BentoGrayLight)
                    .border(1.dp, BentoGrayBorder, CircleShape)
            ) {
                Icon(Icons.Default.Check, contentDescription = "Pular modo", tint = BentoTextDark)
            }
        }

        Spacer(modifier = Modifier.height(36.dp))

        // Tip text in an elegant orange bento card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            colors = CardDefaults.cardColors(containerColor = BentoOrangeLight),
            border = BorderStroke(1.dp, BentoOrangeLight.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "DICA DO JÚNIOR 💡",
                    fontSize = 11.sp,
                    color = BentoOrangeDarkText,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = "A técnica Pomodoro sugere 25 minutos de imersão de código focada, seguida de 5 minutos de descanso. Use o intervalo para respirar, alongar e tomar uma água!",
                    fontSize = 12.sp,
                    color = BentoOrangeDarkText.copy(alpha = 0.8f)
                )
            }
        }
    }
}

// Squeuomorphic Custom Pause Indicator so we don't depend on Material Extended Icons
@Composable
fun CustomPauseIndicator() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.size(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(14.dp)
                .background(WhitePure)
        )
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(14.dp)
                .background(WhitePure)
        )
    }
}

@Composable
fun BentoTabButton(
    selected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    selectedBg: Color,
    selectedIconColor: Color,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(if (selected) selectedBg else Color.Transparent)
                .padding(horizontal = 20.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (selected) selectedIconColor else BentoTextDark.copy(alpha = 0.4f),
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (selected) BentoTextDark else BentoTextDark.copy(alpha = 0.4f)
        )
    }
}
