package com.fahendrena.teacherassistant.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {
    @Query("SELECT * FROM teacher_profile WHERE id = 1")
    fun observeProfile(): Flow<TeacherProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProfile(profile: TeacherProfile)
}

@Dao
interface ClassDao {
    @Query("SELECT * FROM school_class ORDER BY niveau, nom")
    fun observeClasses(): Flow<List<SchoolClass>>

    @Insert
    suspend fun insert(schoolClass: SchoolClass): Long

    @Update
    suspend fun update(schoolClass: SchoolClass)

    @Delete
    suspend fun delete(schoolClass: SchoolClass)
}

@Dao
interface ResourceDao {
    @Query("SELECT * FROM resource_document ORDER BY dateImport DESC")
    fun observeAll(): Flow<List<ResourceDocument>>

    @Query(
        """
        SELECT * FROM resource_document
        WHERE titre LIKE '%' || :query || '%'
           OR texteExtrait LIKE '%' || :query || '%'
           OR niveau LIKE '%' || :query || '%'
           OR matiere LIKE '%' || :query || '%'
        ORDER BY titre
        """
    )
    suspend fun search(query: String): List<ResourceDocument>

    @Query("SELECT * FROM resource_document WHERE categorie = :categorie")
    suspend fun listByCategory(categorie: ResourceCategory): List<ResourceDocument>

    @Insert
    suspend fun insert(resource: ResourceDocument): Long

    @Delete
    suspend fun delete(resource: ResourceDocument)
}

@Dao
interface ResourcePageDao {
    @Query("SELECT * FROM resource_page WHERE resourceId = :resourceId ORDER BY pageNumber")
    suspend fun pagesForResource(resourceId: Long): List<ResourcePage>

    @Insert
    suspend fun insertAll(pages: List<ResourcePage>)

    @Query("DELETE FROM resource_page WHERE resourceId = :resourceId")
    suspend fun deleteForResource(resourceId: Long)
}

@Dao
interface PreparationDao {
    @Query("SELECT * FROM preparation ORDER BY dateModification DESC")
    fun observeAll(): Flow<List<Preparation>>

    @Query("SELECT * FROM preparation WHERE id = :id")
    fun observeById(id: Long): Flow<Preparation?>

    @Insert
    suspend fun insert(preparation: Preparation): Long

    @Update
    suspend fun update(preparation: Preparation)

    @Delete
    suspend fun delete(preparation: Preparation)

    @Query(
        """
        SELECT * FROM preparation
        WHERE titreLecon LIKE '%' || :query || '%'
           OR theme LIKE '%' || :query || '%'
           OR contenuLecon LIKE '%' || :query || '%'
        ORDER BY dateModification DESC
        """
    )
    suspend fun search(query: String): List<Preparation>
}
