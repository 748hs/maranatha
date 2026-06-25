package com.example.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.database.entities.AdEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AdDao {
    @Query("SELECT * FROM advertisements ORDER BY createdAt DESC")
    fun getAllAdsFlow(): Flow<List<AdEntity>>

    @Query("SELECT * FROM advertisements ORDER BY createdAt DESC")
    suspend fun getAllAds(): List<AdEntity>

    @Query("SELECT * FROM advertisements WHERE id = :id LIMIT 1")
    suspend fun getAdById(id: Long): AdEntity?

    @Query("SELECT * FROM advertisements WHERE contactNumber = :phone ORDER BY createdAt DESC")
    fun getAdsBySellerPhoneFlow(phone: String): Flow<List<AdEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAd(ad: AdEntity)

    @Update
    suspend fun updateAd(ad: AdEntity)

    @Delete
    suspend fun deleteAd(ad: AdEntity)

    @Query("DELETE FROM advertisements WHERE id = :id")
    suspend fun deleteAdById(id: Long)
}
