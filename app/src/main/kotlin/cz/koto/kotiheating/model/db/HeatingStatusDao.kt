package cz.koto.kotiheating.model.db

import android.arch.lifecycle.LiveData
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy.REPLACE
import android.arch.persistence.room.Query
import cz.koto.kotiheating.model.entity.HeatingDeviceStatus

interface HeatingStatusDao {

	@Insert(onConflict = REPLACE)
	fun putHeatingStatus(heatingStatus: HeatingDeviceStatus)

	@Query("SELECT * FROM deviceStatus WHERE deviceId = :deviceId")
	fun getHeatingStatus(deviceId: String): LiveData<HeatingDeviceStatus>

}