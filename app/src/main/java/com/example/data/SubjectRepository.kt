package com.example.data

import kotlinx.coroutines.flow.Flow

class SubjectRepository(private val dao: SubjectDao) {
    val allSubjectsWithTasks: Flow<List<SubjectWithTasks>> = dao.getSubjectsWithTasks()
    val allNotes: Flow<List<QuickNote>> = dao.getAllNotes()
    val userStats: Flow<UserStats?> = dao.getUserStatsFlow()

    suspend fun insertUserStats(stats: UserStats) {
        dao.insertUserStats(stats)
    }

    suspend fun updateUserStats(stats: UserStats) {
        dao.updateUserStats(stats)
    }

    fun getSubjectWithTasksById(id: Int): Flow<SubjectWithTasks?> {
        return dao.getSubjectWithTasksById(id)
    }

    suspend fun insertSubject(subject: Subject): Long {
        return dao.insertSubject(subject)
    }

    suspend fun updateSubject(subject: Subject) {
        dao.updateSubject(subject)
    }

    suspend fun deleteSubject(subject: Subject) {
        dao.deleteSubject(subject)
    }

    suspend fun insertTask(task: StudyTask): Long {
        return dao.insertTask(task)
    }

    suspend fun updateTask(task: StudyTask) {
        dao.updateTask(task)
    }

    suspend fun deleteTaskById(id: Int) {
        dao.deleteTaskById(id)
    }

    suspend fun insertNote(note: QuickNote): Long {
        return dao.insertNote(note)
    }

    suspend fun updateNote(note: QuickNote) {
        dao.updateNote(note)
    }

    suspend fun deleteNote(note: QuickNote) {
        dao.deleteNote(note)
    }
}
