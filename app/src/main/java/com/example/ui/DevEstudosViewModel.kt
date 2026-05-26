package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class DevEstudosViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = SubjectRepository(database.subjectDao())

    // UI state for subjects and notes (automatically read from Room Streams)
    val subjectsState: StateFlow<List<SubjectWithTasks>> = repository.allSubjectsWithTasks
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val notesState: StateFlow<List<QuickNote>> = repository.allNotes
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val gamificationStatsState: StateFlow<UserStats> = repository.userStats
        .map { it ?: UserStats() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserStats()
        )

    val plannerRecommendations: StateFlow<List<PlannerRecommendation>> = repository.allSubjectsWithTasks
        .map { list -> generateRecommendations(list) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Pomodoro Timer States
    private val _remainingTime = MutableStateFlow(25 * 60) // 25 minutes default
    val remainingTime: StateFlow<Int> = _remainingTime.asStateFlow()

    private val _timerRunning = MutableStateFlow(false)
    val timerRunning: StateFlow<Boolean> = _timerRunning.asStateFlow()

    private val _isTimerBreak = MutableStateFlow(false)
    val isTimerBreak: StateFlow<Boolean> = _isTimerBreak.asStateFlow()

    private var timerJob: Job? = null

    init {
        // Run database existence check & initial pre-population on IO dispatcher
        viewModelScope.launch(Dispatchers.IO) {
            // We read the first available snapshot of the database stream
            val currentList = repository.allSubjectsWithTasks.first()
            if (currentList.isEmpty()) {
                prepopulateRoadmap()
            }
            // Initialize UserStats if not present
            val currentStats = repository.userStats.first()
            if (currentStats == null) {
                repository.insertUserStats(UserStats(id = 1, xp = 0, level = 1))
            }
        }
    }

    // --- Database Population (Developer Path: Beginner to Junior Dev) ---
    private suspend fun prepopulateRoadmap() {
        val defaultSubjects = listOf(
            PrepopSubject(
                title = "Lógica de Programação",
                description = "Fundamento absoluto de todo desenvolvedor. Compreensão de fluxos, condicionais, repetições e modularidade de algoritmos.",
                priority = "Crítica",
                phase = "Fundamentos",
                tasks = listOf(
                    "Declaração de Variáveis e Tipos de Dados Básicos",
                    "Estruturas Condicionais (if, else, switch)",
                    "Laços de Repetição (for, while)",
                    "Criação de Funções, Escopos e Retornos",
                    "Manipulação de Coleções Simples (Vetores e Listas)"
                )
            ),
            PrepopSubject(
                title = "Controle de Versão (Git/GitHub)",
                description = "Gerenciamento de histórico de códigos e fluxo de colaboração profissional dentro de times ágeis.",
                priority = "Crítica",
                phase = "Fundamentos",
                tasks = listOf(
                    "Instalação e comandos essenciais (init, add, commit, status)",
                    "Criação e navegação de ramificações (Branches)",
                    "Sincronização com repositório remoto (push, pull, clone)",
                    "Criação de Pull Requests (PR) e Code Review",
                    "Resolução prática de conflitos de mesclagem (Merge)"
                )
            ),
            PrepopSubject(
                title = "HTML5 e CSS3 Semânticos",
                description = "Construção de interfaces de portais e sistemas, com foco em estruturação semântica correta e estilização responsiva moderna.",
                priority = "Alta",
                phase = "Fundamentos",
                tasks = listOf(
                    "Estrutura básica de documentos e tags semânticas (main, nav, article)",
                    "Estilização flexível com Flexbox",
                    "Layouts robustos bidimensionais usando CSS Grid",
                    "Design responsivo para múltiplos dispositivos com Media Queries",
                    "Estilos reutilizáveis através de Variáveis CSS"
                )
            ),
            PrepopSubject(
                title = "Linguagem de Programação Core",
                description = "Aprofundar fundações em uma linguagem chave (como Kotlin ou JavaScript). Entender assincronismo, manipulação avançada de dados e POO.",
                priority = "Crítica",
                phase = "Tecnologias Core",
                tasks = listOf(
                    "Entendimento de Programação Orientada a Objetos (Classes, Herança, Interfaces)",
                    "Programação Assíncrona e Fluxos de Controle (Promises/Async-Await/Coroutines)",
                    "Manipulação profissional de erros estruturados (try-catch-finally)",
                    "Tratamento de Coleções Avançadas e Filtros (Map, Filter, Reduce)",
                    "Tipos nulos e segurança em tempo de execução"
                )
            ),
            PrepopSubject(
                title = "Bancos de Dados Relacionais & SQL",
                description = "Domínio básico de armazenamento relacional, modelagem e manipulação persistente de dados de negócios.",
                priority = "Alta",
                phase = "Tecnologias Core",
                tasks = listOf(
                    "Modelagem conceitual de dados relacionais (Tabelas, Chaves Primárias e Estrangeiras)",
                    "Consultas SQL Básicas e Avançadas com Filtros (SELECT, WHERE, ORDER BY)",
                    "Combinação de Dados através de junções (INNER, LEFT, RIGHT JOIN)",
                    "Consultas de manipulação (INSERT, UPDATE, DELETE)",
                    "Entendimento básico de transações e chaves únicas"
                )
            ),
            PrepopSubject(
                title = "APIs RESTful e HTTP",
                description = "Estudo do modelo cliente-servidor de comunicação de software moderno. Entendimento granular do ecossistema Web.",
                priority = "Alta",
                phase = "Tecnologias Core",
                tasks = listOf(
                    "Estrutura da Request e Response HTTP",
                    "Verbos padrão do mercado (GET, POST, PUT, DELETE)",
                    "Uso e interpretação de Códigos de Status (200, 201, 400, 401, 500)",
                    "Criação/Consumo de cabeçalhos (Headers) e Query Parameters",
                    "Noções de Autenticação via Token JWT (Bearer Token)"
                )
            ),
            PrepopSubject(
                title = "Framework de Desenvolvimento",
                description = "Trabalhar de forma acelerada adotando frameworks consolidados no mercado (ex. React, Node.js Express ou Jetpack Compose/Android).",
                priority = "Média",
                phase = "Especialização/Prática",
                tasks = listOf(
                    "Setup inicial, árvore de componentes e padrões de pastas",
                    "Ciclo de vida do componente e ganchos em runtime",
                    "Gerenciamento unificado de estados de interface",
                    "Integração eficiente de rede e preenchimento de dados de APIs",
                    "Estilação integrada aos componentes de interface"
                )
            ),
            PrepopSubject(
                title = "Testes de Programação",
                description = "Assegurar confiabilidade e evitar regressões de código usando testabilidade automática.",
                priority = "Alta",
                phase = "Especialização/Prática",
                tasks = listOf(
                    "Fundamento e importância de testes de unidade",
                    "Criação de cenários de sucesso e caminhos alternativos",
                    "Simulação (Mocking) de bases de dados e APIs externas",
                    "Leitura de relatórios de cobertura de testes",
                    "Criação de testes integrados simples de fluxo de dados"
                )
            ),
            PrepopSubject(
                title = "Princípios Clean Code e SOLID",
                description = "Boas práticas de estrutura de código para promover sustentabilidade técnica, legibilidade e expansibilidade amigável.",
                priority = "Média",
                phase = "Especialização/Prática",
                tasks = listOf(
                    "Uso coerente de nomes e funções de responsabilidade única (SRP)",
                    "Eliminação de duplicações desnecessárias (DRY)",
                    "Princípios de coesão e acoplamento baixo",
                    "Refatorações cíclicas de código legado",
                    "Evitar comentários excessivos mantendo o código autoexplicativo"
                )
            ),
            PrepopSubject(
                title = "Docker & Conteinerização",
                description = "Isolamento de ambientes facilitado por containers virtuais, extinguindo inconsistências entre ambientes local e produção.",
                priority = "Baixa",
                phase = "Preparação Jr",
                tasks = listOf(
                    "Escrever arquivos Dockerfile para serviços personalizados",
                    "Provisionar instâncias de depuração locais (ex: Banco de Dados)",
                    "Configuração básica de multi-contêineres usando Docker Compose",
                    "Criação de volumes para persistência de dados",
                    "Mapeamento de portas locais para acesso à aplicação"
                )
            ),
            PrepopSubject(
                title = "Preparação Júnior & Portfólio",
                description = "O salto final para contratação. Engajamento social, consolidação de habilidades práticas reais e entrevistas simuladas.",
                priority = "Crítica",
                phase = "Preparação Jr",
                tasks = listOf(
                    "Desenvolver e hospedar 3 projetos ricos no GitHub com documentação robusta",
                    "Escrever arquivos README.md claros descrevendo requisitos e soluções das apps",
                    "Otimizar currículo técnico e LinkedIn focando em termos buscados pelo mercado",
                    "Treinar desafios lógicos e de algoritmos simples (LeetCode/HackerRank)",
                    "Praticar entrevistas de comportamento e perguntas de arquitetura de TI"
                )
            )
        )

        for (prepop in defaultSubjects) {
            val subjectId = repository.insertSubject(
                Subject(
                    title = prepop.title,
                    description = prepop.description,
                    priority = prepop.priority,
                    phase = prepop.phase,
                    notes = prepop.notes,
                    resourcesUrl = prepop.resourcesUrl,
                    isCustom = false,
                    status = "A Estudar"
                )
            ).toInt()

            for (taskTitle in prepop.tasks) {
                repository.insertTask(
                    StudyTask(
                        subjectId = subjectId,
                        title = taskTitle,
                        isCompleted = false
                    )
                )
            }
        }
    }

    private data class PrepopSubject(
        val title: String,
        val description: String,
        val priority: String,
        val phase: String,
        val notes: String = "",
        val resourcesUrl: String = "",
        val tasks: List<String>
    )

    // --- Subject Operations (CRUD) ---
    fun addSubject(title: String, description: String, priority: String, phase: String, resourcesUrl: String, tasks: List<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            val subjectId = repository.insertSubject(
                Subject(
                    title = title,
                    description = description,
                    priority = priority,
                    phase = phase,
                    status = "A Estudar",
                    resourcesUrl = resourcesUrl,
                    isCustom = true
                )
            ).toInt()

            for (taskTitle in tasks) {
                if (taskTitle.isNotBlank()) {
                    repository.insertTask(
                        StudyTask(
                            subjectId = subjectId,
                            title = taskTitle.trim(),
                            isCompleted = false
                        )
                    )
                }
            }
        }
    }

    fun updateSubject(subject: Subject) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateSubject(subject)
        }
    }

    fun deleteSubject(subject: Subject) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteSubject(subject)
        }
    }

    fun updateSubjectStatus(subject: Subject, newStatus: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val oldStatus = subject.status
            repository.updateSubject(subject.copy(status = newStatus))
            if (newStatus == "Concluído" && oldStatus != "Concluído") {
                grantXP(100, "subject")
            }
        }
    }

    // --- Task Operations ---
    fun addTask(subjectId: Int, title: String) {
        if (title.isBlank()) return
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertTask(
                StudyTask(
                    subjectId = subjectId,
                    title = title.trim(),
                    isCompleted = false
                )
            )
        }
    }

    fun toggleTask(task: StudyTask) {
        viewModelScope.launch(Dispatchers.IO) {
            val updatedTask = task.copy(isCompleted = !task.isCompleted)
            repository.updateTask(updatedTask)
            
            if (updatedTask.isCompleted) {
                // Task became completed!
                grantXP(15, "task")
                
                // Track if this completes the whole subject
                val subjectWithTasks = repository.getSubjectWithTasksById(updatedTask.subjectId).first()
                if (subjectWithTasks != null) {
                    val notDoneCount = subjectWithTasks.tasks.count { !it.isCompleted }
                    if (notDoneCount == 0) { // All tasks are completed!
                        repository.updateSubject(subjectWithTasks.subject.copy(status = "Concluído"))
                        grantXP(100, "subject")
                    }
                }
            }
        }
    }

    fun deleteTask(taskId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteTaskById(taskId)
        }
    }

    // --- Quick Note Operations ---
    fun addNote(title: String, content: String) {
        if (title.isBlank() && content.isBlank()) return
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertNote(
                QuickNote(
                    title = title.trim(),
                    content = content.trim()
                )
            )
            grantXP(10, "note")
        }
    }

    fun updateNote(note: QuickNote) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateNote(note)
        }
    }

    fun deleteNote(note: QuickNote) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteNote(note)
        }
    }

    // --- Pomodoro Ticker Handler ---
    fun startTimer() {
        if (_timerRunning.value) return
        _timerRunning.value = true
        timerJob = viewModelScope.launch {
            while (_remainingTime.value > 0) {
                delay(1000)
                _remainingTime.value -= 1
            }
            onTimerFinish()
        }
    }

    fun pauseTimer() {
        _timerRunning.value = false
        timerJob?.cancel()
    }

    fun resetTimer() {
        pauseTimer()
        _remainingTime.value = if (_isTimerBreak.value) 5 * 60 else 25 * 60
    }

    fun skipTimer() {
        pauseTimer()
        toggleTimerMode()
    }

    private fun toggleTimerMode() {
        _isTimerBreak.value = !_isTimerBreak.value
        _remainingTime.value = if (_isTimerBreak.value) 5 * 60 else 25 * 60
    }

    private fun onTimerFinish() {
        _timerRunning.value = false
        val wasBreak = _isTimerBreak.value
        toggleTimerMode()
        if (!wasBreak) {
            // Completed a productive focus cycle!
            grantXP(50, "pomodoro")
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }

    // --- Gamification Engine ---
    fun grantXP(amount: Int, xpType: String = "") {
        viewModelScope.launch(Dispatchers.IO) {
            val stats = repository.userStats.first() ?: UserStats(id = 1, xp = 0, level = 1)
            var newXP = stats.xp + amount
            var newLevel = stats.level
            var unlockedFeatures = stats.unlockedFeatures
            var earnedBadges = stats.earnedBadges

            // Formula: Level up at Level * 200 XP
            while (newXP >= newLevel * 200) {
                newXP -= newLevel * 200
                newLevel++
                
                // Unlock specific features:
                val newFeature = when (newLevel) {
                    2 -> "Booster"     // Libera booster pomodoro
                    3 -> "Supremo"     // Libera Foco Supremo
                    4 -> "Entrevistas" // Libera Simulador de Perguntas
                    5 -> "README"      // Libera Gerador de README
                    else -> null
                }
                if (newFeature != null) {
                    val featureList = unlockedFeatures.split(",").filter { it.isNotBlank() }.toMutableList()
                    if (!featureList.contains(newFeature)) {
                        featureList.add(newFeature)
                        unlockedFeatures = featureList.joinToString(",")
                    }
                }
            }

            // Badge validation conditions
            val badgeList = earnedBadges.split(",").filter { it.isNotBlank() }.toMutableList()

            fun tryUnlockBadge(badgeId: String, bonusXp: Int = 100) {
                if (!badgeList.contains(badgeId)) {
                    badgeList.add(badgeId)
                    newXP += bonusXp
                }
            }

            if (xpType == "task") {
                tryUnlockBadge("badge_first_step", 50)
                if (stats.completedTasks + 1 >= 5) {
                    tryUnlockBadge("badge_task_master", 100)
                }
            }
            if (xpType == "pomodoro") {
                tryUnlockBadge("badge_timer_master", 50)
                if (stats.completedPomodoros + 1 >= 3) {
                    tryUnlockBadge("badge_steel_focus", 100)
                }
            }
            if (xpType == "subject") {
                tryUnlockBadge("badge_roadmap_pioneer", 100)
                if (stats.completedSubjects + 1 >= 3) {
                    tryUnlockBadge("badge_fullstack_challenger", 200)
                }
            }
            if (xpType == "note") {
                tryUnlockBadge("badge_scribe", 50)
            }

            earnedBadges = badgeList.joinToString(",")

            repository.insertUserStats(
                stats.copy(
                    xp = newXP,
                    level = newLevel,
                    completedTasks = stats.completedTasks + if (xpType == "task") 1 else 0,
                    completedSubjects = stats.completedSubjects + if (xpType == "subject") 1 else 0,
                    completedPomodoros = stats.completedPomodoros + if (xpType == "pomodoro") 1 else 0,
                    unlockedFeatures = unlockedFeatures,
                    earnedBadges = earnedBadges
                )
            )
        }
    }

    // --- Adaptive Recommendation Engine Generator ---
    private fun generateRecommendations(list: List<SubjectWithTasks>): List<PlannerRecommendation> {
        val recs = mutableListOf<PlannerRecommendation>()
        for (item in list) {
            val total = item.tasks.size
            if (total == 0) continue
            val completed = item.tasks.count { it.isCompleted }
            val progress = (completed.toFloat() / total.toFloat() * 100).toInt()

            if (item.subject.status == "Concluído" || progress == 100) {
                continue
            }

            if (item.subject.status == "A Estudar" && (item.subject.priority == "Crítica" || item.subject.priority == "Alta")) {
                recs.add(
                    PlannerRecommendation(
                        subjectId = item.subject.id,
                        subjectTitle = item.subject.title,
                        reason = "Base do Conhecimento Pendente",
                        suggestedAction = "Crítica para se destacar em processos. Dedique 45 minutos hoje para iniciar o checklist desta matéria.",
                        urgencyLevel = "Alta",
                        progress = progress
                    )
                )
            } else if (item.subject.status == "Trabalhando" && progress < 40) {
                recs.add(
                    PlannerRecommendation(
                        subjectId = item.subject.id,
                        subjectTitle = item.subject.title,
                        reason = "Obstáculo Identificado (Baixo Progresso Ativo)",
                        suggestedAction = "Você começou esta matéria mas teve progresso lento. Conclua pelo menos uma tarefa secundária no próximo Pomodoro.",
                        urgencyLevel = "Alta",
                        progress = progress
                    )
                )
            } else if (item.subject.status == "Trabalhando" && progress >= 70) {
                recs.add(
                    PlannerRecommendation(
                        subjectId = item.subject.id,
                        subjectTitle = item.subject.title,
                        reason = "Próximo de Finalizar!",
                        suggestedAction = "Falta pouquíssimo para obter o domínio absoluto. Conclua o roteiro e garanta o grande bônus de 100 pontos de XP!",
                        urgencyLevel = "Média",
                        progress = progress
                    )
                )
            } else {
                recs.add(
                    PlannerRecommendation(
                        subjectId = item.subject.id,
                        subjectTitle = item.subject.title,
                        reason = "Ritmo Geral de Estudos",
                        suggestedAction = "Matéria em andamento. Mantenha os checkpoints em dia e revise os links de referência de vez em quando.",
                        urgencyLevel = "Informativa",
                        progress = progress
                    )
                )
            }
        }

        return recs.sortedWith(compareBy {
            when (it.urgencyLevel) {
                "Alta" -> 1
                "Média" -> 2
                else -> 3
            }
        })
    }
}

data class PlannerRecommendation(
    val subjectId: Int,
    val subjectTitle: String,
    val reason: String,
    val suggestedAction: String,
    val urgencyLevel: String, // "Alta", "Média", "Informativa"
    val progress: Int
)
