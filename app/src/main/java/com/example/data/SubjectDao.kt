package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

data class SubjectWithTasks(
    @Embedded val subject: Subject,
    @Relation(
        parentColumn = "id",
        entityColumn = "subjectId"
    )
    val tasks: List<StudyTask>
)

@Dao
interface SubjectDao {
    // Subjects
    @Query("SELECT * FROM subjects ORDER BY id ASC")
    fun getAllSubjects(): Flow<List<Subject>>

    @Transaction
    @Query("SELECT * FROM subjects ORDER BY id ASC")
    fun getSubjectsWithTasks(): Flow<List<SubjectWithTasks>>

    @Query("SELECT * FROM subjects WHERE id = :id")
    suspend fun getSubjectById(id: Int): Subject?

    @Transaction
    @Query("SELECT * FROM subjects WHERE id = :id")
    fun getSubjectWithTasksById(id: Int): Flow<SubjectWithTasks?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubject(subject: Subject): Long

    @Update
    suspend fun updateSubject(subject: Subject)

    @Delete
    suspend fun deleteSubject(subject: Subject)

    // Study Tasks
    @Query("SELECT * FROM study_tasks WHERE subjectId = :subjectId ORDER BY id ASC")
    fun getTasksForSubject(subjectId: Int): Flow<List<StudyTask>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: StudyTask): Long

    @Update
    suspend fun updateTask(task: StudyTask)

    @Query("DELETE FROM study_tasks WHERE id = :id")
    suspend fun deleteTaskById(id: Int)

    @Query("DELETE FROM study_tasks WHERE subjectId = :subjectId")
    suspend fun deleteTasksForSubject(subjectId: Int)

    // Quick Notes
    @Query("SELECT * FROM quick_notes ORDER BY createdAt DESC")
    fun getAllNotes(): Flow<List<QuickNote>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: QuickNote): Long

    @Update
    suspend fun updateNote(note: QuickNote)

    @Delete
    suspend fun deleteNote(note: QuickNote)

    // User Stats Gamification
    @Query("SELECT * FROM user_stats WHERE id = 1")
    fun getUserStatsFlow(): Flow<UserStats?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserStats(stats: UserStats)

    @Update
    suspend fun updateUserStats(stats: UserStats)
}
