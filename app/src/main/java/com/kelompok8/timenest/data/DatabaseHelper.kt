package com.kelompok8.timenest.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, "TimeNestDB", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        val createTaskTable = """
            CREATE TABLE tasks (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                description TEXT,
                deadline TEXT,
                isDone INTEGER,
                priority INTEGER
            );
        """.trimIndent()

        val createEventTable = """
            CREATE TABLE events (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT NOT NULL,
                date TEXT,
                time TEXT,
                location TEXT,
                category TEXT
            );
        """.trimIndent()

        val createReminderTable = """
            CREATE TABLE reminders (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                targetId INTEGER,
                type TEXT,            -- "task" atau "event"
                remindAt TEXT
            );
        """.trimIndent()

        db.execSQL(createTaskTable)
        db.execSQL(createEventTable)
        db.execSQL(createReminderTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS tasks")
        db.execSQL("DROP TABLE IF EXISTS events")
        db.execSQL("DROP TABLE IF EXISTS reminders")
        onCreate(db)
    }
}