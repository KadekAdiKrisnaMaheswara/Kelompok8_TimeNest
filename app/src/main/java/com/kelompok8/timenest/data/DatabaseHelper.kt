package com.kelompok8.timenest.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.kelompok8.timenest.model.Task

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        val createTasksTable = """
            CREATE TABLE IF NOT EXISTS tasks (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT,
                end_date TEXT,
                start_time TEXT,
                end_time TEXT,
                remind TEXT
            );
        """.trimIndent()

        val createUsersTable = """
            CREATE TABLE IF NOT EXISTS users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                full_name TEXT NOT NULL,
                email TEXT NOT NULL UNIQUE,
                password TEXT NOT NULL
            );
        """.trimIndent()

        db.execSQL(createTasksTable)
        db.execSQL(createUsersTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS tasks")
        db.execSQL("DROP TABLE IF EXISTS users")
        onCreate(db)
    }

    fun insertTask(
        title: String,
        endDate: String,
        startTime: String,
        endTime: String,
        remind: String
    ): Boolean {
        val db = writableDatabase
        val query = """
            INSERT INTO tasks (title, end_date, start_time, end_time, remind)
            VALUES (?, ?, ?, ?, ?)
        """.trimIndent()

        return try {
            val stmt = db.compileStatement(query)
            stmt.bindString(1, title)
            stmt.bindString(2, endDate)
            stmt.bindString(3, startTime)
            stmt.bindString(4, endTime)
            stmt.bindString(5, remind)
            stmt.executeInsert()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun getAllTasks(): List<Task> {
        val list = mutableListOf<Task>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT title, end_date, start_time, end_time, remind FROM tasks", null)

        if (cursor.moveToFirst()) {
            do {
                val title = cursor.getString(0)
                val endDate = cursor.getString(1)
                val startTime = cursor.getString(2)
                val endTime = cursor.getString(3)
                val remind = cursor.getString(4)
                list.add(Task(title, endDate, startTime, endTime, remind))
            } while (cursor.moveToNext())
        }

        cursor.close()
        return list
    }

    companion object {
        const val DATABASE_NAME = "TimeNestDB"
        const val DATABASE_VERSION = 1
    }
}
