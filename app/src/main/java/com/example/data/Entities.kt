package com.example.data

import androidx.room.*

@Entity(tableName = "subjects")
data class Subject(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val priority: String, // "Crítica", "Alta", "Média", "Baixa"
    val phase: String,    // "Fundamentos", "Tecnologias Core", "Especialização/Prática", "Preparação Jr"
    val status: String,   // "A Estudar", "Trabalhando", "Concluído"
    val notes: String = "",
    val resourcesUrl: String = "",
    val isCustom: Boolean = false
)

@Entity(
    tableName = "study_tasks",
    foreignKeys = [
        ForeignKey(
            entity = Subject::class,
            parentColumns = ["id"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["subjectId"])]
)
data class StudyTask(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val subjectId: Int,
    val title: String,
    val isCompleted: Boolean = false
)

@Entity(tableName = "quick_notes")
data class QuickNote(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_stats")
data class UserStats(
    @PrimaryKey val id: Int = 1,
    val xp: Int = 0,
    val level: Int = 1,
    val completedTasks: Int = 0,
    val completedSubjects: Int = 0,
    val completedPomodoros: Int = 0,
    val unlockedFeatures: String = "", // features unlocked, comma separated: "Booster", "Entrevistas", "README"
    val earnedBadges: String = "" // badge IDs unlocked, comma separated: "badge_first_step", etc.
)

