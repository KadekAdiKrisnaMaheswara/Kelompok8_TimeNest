package com.kelompok8.timenest.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.kelompok8.timenest.model.Category
import com.kelompok8.timenest.model.Task

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        val createCategoryTable = """
            CREATE TABLE IF NOT EXISTS $TABLE_CATEGORY (
                id_category INTEGER PRIMARY KEY AUTOINCREMENT,
                category_name TEXT NOT NULL,
                type TEXT CHECK(type IN ('Task', 'Event', 'All')) NOT NULL
            );
        """.trimIndent()

        val createEventTable = """
            CREATE TABLE IF NOT EXISTS $TABLE_EVENT (
                id_event INTEGER PRIMARY KEY AUTOINCREMENT,
                event_name TEXT NOT NULL,
                event_date TEXT NOT NULL,
                event_time TEXT,
                location TEXT,
                category_id INTEGER,
                reminder_time TEXT,
                FOREIGN KEY (category_id) REFERENCES $TABLE_CATEGORY(id_category)
            );
        """.trimIndent()

        val createTaskTable = """
            CREATE TABLE IF NOT EXISTS $TABLE_TASK (
                id_task INTEGER PRIMARY KEY AUTOINCREMENT,
                task_name TEXT NOT NULL,
                description TEXT,
                due_date TEXT NOT NULL,
                priority_level TEXT CHECK(priority_level IN ('Low', 'Medium', 'High')) DEFAULT 'Medium',
                status TEXT CHECK(status IN ('Done', 'Not Done')) DEFAULT 'Not Done',
                category_id INTEGER,
                reminder_time TEXT,
                FOREIGN KEY (category_id) REFERENCES $TABLE_CATEGORY(id_category)
            );
        """.trimIndent()

        val createUserTable = """
            CREATE TABLE IF NOT EXISTS $TABLE_USER (
                id_user INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                email TEXT UNIQUE NOT NULL,
                password TEXT NOT NULL
            );
        """.trimIndent()

        db.execSQL(createCategoryTable)
        db.execSQL(createEventTable)
        db.execSQL(createTaskTable)
        db.execSQL(createUserTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_TASK")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_EVENT")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CATEGORY")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USER")
        onCreate(db)
    }

    fun getAllCategories(): List<Category> {
        val list = mutableListOf<Category>()
        val db = readableDatabase

        // Ambil nama kategori dan jumlah task terkait
        val query = """
        SELECT c.category_name, COUNT(t.id_task) as total_tasks
        FROM $TABLE_CATEGORY c
        LEFT JOIN $TABLE_TASK t ON c.id_category = t.category_id
        GROUP BY c.id_category, c.category_name
    """.trimIndent()

        val cursor = db.rawQuery(query, null)

        if (cursor.moveToFirst()) {
            do {
                val name = cursor.getString(cursor.getColumnIndexOrThrow("category_name"))
                val count = cursor.getInt(cursor.getColumnIndexOrThrow("total_tasks"))
                list.add(Category(name, count))
            } while (cursor.moveToNext())
        }

        cursor.close()
        return list
    }


    fun getOngoingTasks(): List<Task> {
        val list = mutableListOf<Task>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT task_name, due_date FROM $TABLE_TASK WHERE status = 'Not Done'", null)

        if (cursor.moveToFirst()) {
            do {
                val name = cursor.getString(cursor.getColumnIndexOrThrow("task_name"))
                val dueDate = cursor.getString(cursor.getColumnIndexOrThrow("due_date"))
                val startTime = cursor.getString(cursor.getColumnIndexOrThrow("start_time"))
                val endTime = cursor.getString(cursor.getColumnIndexOrThrow("end_time"))

                list.add(Task(name, dueDate, startTime, endTime))
            } while (cursor.moveToNext())
        }

        cursor.close()
        return list
    }

    fun insertTask(title: String, dueDate: String, startTime: String, endTime: String, remind: String): Boolean {
        val db = writableDatabase
        val query = """
        INSERT INTO $TABLE_TASK (task_name, due_date, reminder_time, description, priority_level, status)
        VALUES (?, ?, ?, '', 'Medium', 'Not Done')
    """.trimIndent()

        return try {
            val stmt = db.compileStatement(query)
            stmt.bindString(1, title)
            stmt.bindString(2, dueDate)
            stmt.bindString(3, "$startTime - $endTime")
            stmt.executeInsert()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    companion object {
        const val DATABASE_NAME = "TimeNestDB"
        const val DATABASE_VERSION = 1

        const val TABLE_CATEGORY = "tbl_category"
        const val TABLE_EVENT = "tbl_event"
        const val TABLE_TASK = "tbl_task"
        const val TABLE_USER = "tbl_user"
    }
}
