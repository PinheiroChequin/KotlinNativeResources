package com.projeto.native_resource

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.content.ContentValues

class FormDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "formdata.db"
        const val DATABASE_VERSION = 1
        const val TABLE_NAME = "FormData"
        const val COLUMN_NAME = "name"
        const val COLUMN_EMAIL = "email"
        const val COLUMN_COMMENT = "comment"
        const val COLUMN_PHOTO_PATH = "photo_path"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE $TABLE_NAME (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_NAME TEXT,
                $COLUMN_EMAIL TEXT,
                $COLUMN_COMMENT TEXT,
                $COLUMN_PHOTO_PATH TEXT
            )
        """
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun insertFormData(name: String, email: String, comment: String, photoPath: String) {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NAME, name)
            put(COLUMN_EMAIL, email)
            put(COLUMN_COMMENT, comment)
            put(COLUMN_PHOTO_PATH, photoPath)
        }
        db.insert(TABLE_NAME, null, values)
        db.close()
    }

    fun getAllFormData(): List<String> {
        val db = readableDatabase
        val cursor = db.query(TABLE_NAME, null, null, null, null, null, null)
        val dataList = mutableListOf<String>()

        while (cursor.moveToNext()) {
            val name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME))
            val email = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL))
            val comment = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_COMMENT))
            val data = "Nome: $name, Email: $email, Comentário: $comment"
            dataList.add(data)
        }

        cursor.close()
        db.close()

        return dataList
    }
}
