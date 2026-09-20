package com.example.database

import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.watersort.core.database.AppDatabase
import org.junit.Assert.assertTrue
import org.junit.Test
import java.lang.reflect.Proxy

class MigrationTest {

    private fun createMockDb(executedSql: MutableList<String>): SupportSQLiteDatabase {
        return Proxy.newProxyInstance(
            SupportSQLiteDatabase::class.java.classLoader,
            arrayOf(SupportSQLiteDatabase::class.java)
        ) { _, method, args ->
            if (method.name == "execSQL" && args != null && args.isNotEmpty()) {
                executedSql.add(args[0] as String)
            }
            null
        } as SupportSQLiteDatabase
    }

    @Test
    fun testMigration1To2SqlExecution() {
        val executedSql = mutableListOf<String>()
        val mockDb = createMockDb(executedSql)
        AppDatabase.MIGRATION_1_2.migrate(mockDb)
        assertTrue(executedSql.any { it.contains("CREATE TABLE IF NOT EXISTS missions") })
    }

    @Test
    fun testMigration2To3SqlExecution() {
        val executedSql = mutableListOf<String>()
        val mockDb = createMockDb(executedSql)
        AppDatabase.MIGRATION_2_3.migrate(mockDb)
        assertTrue(executedSql.any { it.contains("CREATE TABLE IF NOT EXISTS inventory_items_new") })
        assertTrue(executedSql.any { it.contains("INSERT OR IGNORE INTO inventory_items_new") })
        assertTrue(executedSql.any { it.contains("DROP TABLE IF EXISTS inventory_items") })
        assertTrue(executedSql.any { it.contains("ALTER TABLE inventory_items_new RENAME TO inventory_items") })
    }

    @Test
    fun testMigration3To4SqlExecution() {
        val executedSql = mutableListOf<String>()
        val mockDb = createMockDb(executedSql)
        AppDatabase.MIGRATION_3_4.migrate(mockDb)
        assertTrue(executedSql.any { it.contains("CREATE TABLE IF NOT EXISTS game_progress") })
        assertTrue(executedSql.any { it.contains("CREATE TABLE IF NOT EXISTS game_save") })
    }
}


