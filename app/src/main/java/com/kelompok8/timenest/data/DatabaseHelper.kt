package com.kelompok8.timenest.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.kelompok8.timenest.model.Task
import android.util.Log
import android.content.ContentValues

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        val createUsersTable = """
            CREATE TABLE IF NOT EXISTS users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                full_name TEXT NOT NULL,
                email TEXT NOT NULL UNIQUE,
                password TEXT NOT NULL
            );
        """.trimIndent()

        val createCategoriesTable = """
            CREATE TABLE IF NOT EXISTS categories (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER NOT NULL,
                name TEXT NOT NULL
            );
        """.trimIndent()

        val createTasksTable = """
            CREATE TABLE IF NOT EXISTS tasks (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER,
                category_id INTEGER,
                title TEXT,
                end_date TEXT,
                start_time TEXT,
                end_time TEXT,
                remind TEXT,
                is_completed INTEGER DEFAULT 0,
                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL
            );
        """.trimIndent()

        db.execSQL(createUsersTable)
        db.execSQL(createCategoriesTable)
        db.execSQL(createTasksTable)

        // Tambahkan data contoh
        db.execSQL("INSERT INTO categories (user_id, name) VALUES (1, 'Work')")
        db.execSQL("INSERT INTO categories (user_id, name) VALUES (1, 'Study')")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS tasks")
        db.execSQL("DROP TABLE IF EXISTS categories")
        db.execSQL("DROP TABLE IF EXISTS users")
        onCreate(db)
    }

    fun insertTask(
        userId: Int,
        categoryId: Int,
        title: String,
        endDate: String,
        startTime: String,
        endTime: String,
        remind: String,
        isCompleted: Boolean
    ): Boolean {
        val db = writableDatabase
        val query = """
            INSERT INTO tasks (user_id, category_id, title, end_date, start_time, end_time, remind, is_completed)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """.trimIndent()

        return try {
            val stmt = db.compileStatement(query)
            stmt.bindLong(1, userId.toLong())
            stmt.bindLong(2, categoryId.toLong())
            stmt.bindString(3, title)
            stmt.bindString(4, endDate)
            stmt.bindString(5, startTime)
            stmt.bindString(6, endTime)
            stmt.bindString(7, remind)
            stmt.bindLong(8, if (isCompleted) 1 else 0)
            stmt.executeInsert()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun getCategoryIdByName(userId: Int, categoryName: String): Int {
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT id FROM categories WHERE user_id = ? AND name = ? LIMIT 1",
            arrayOf(userId.toString(), categoryName)
        )
        var id = -1
        if (cursor.moveToFirst()) {
            id = cursor.getInt(cursor.getColumnIndexOrThrow("id"))
        }
        cursor.close()
        return id
    }

    fun getAllTasks(): List<Task> {
        val list = mutableListOf<Task>()
        val db = readableDatabase

        val query = """
            SELECT t.id, t.title, c.name AS category, t.end_date, t.start_time, t.end_time, t.remind, t.is_completed
            FROM tasks t
            LEFT JOIN categories c ON t.category_id = c.id
        """

        val cursor = db.rawQuery(query, null)

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(0)
                val title = cursor.getString(1)
                val category = cursor.getString(2) ?: "Uncategorized"
                val endDate = cursor.getString(3)
                val startTime = cursor.getString(4)
                val endTime = cursor.getString(5)
                val remind = cursor.getString(6)
                val isCompleted = cursor.getInt(cursor.getColumnIndexOrThrow("is_completed")).toString()

                list.add(
                    Task(
                        id, title, category,
                        endDate, startTime, endTime,
                        remind, isCompleted
                    )
                )
            } while (cursor.moveToNext())
        }

        cursor.close()
        return list
    }

    fun getAllCategories(userId: Int): List<String> {
        val db = this.readableDatabase
        val categoryList = mutableListOf<String>()

        val cursor = db.rawQuery(
            "SELECT name FROM categories WHERE user_id = ?",
            arrayOf(userId.toString())
        )

        while (cursor.moveToNext()) {
            val name = cursor.getString(0)
            categoryList.add(name)
            Log.d("DatabaseHelper", "Category found: $name")
        }

        cursor.close()
        db.close()
        return categoryList
    }

    fun insertCategory(userId: Int, name: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("user_id", userId)
            put("name", name)
        }
        val result = db.insert("categories", null, values)
        db.close()
        return result != -1L
    }

    companion object {
        const val DATABASE_NAME = "TimeNestDB"
        const val DATABASE_VERSION = 3
    }
}
