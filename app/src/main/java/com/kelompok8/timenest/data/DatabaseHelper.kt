package com.kelompok8.timenest.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
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
