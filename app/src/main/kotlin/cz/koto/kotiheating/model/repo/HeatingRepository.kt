package cz.koto.kotiheating.model.repo

import cz.koto.kotiheating.model.HeatingCache
import cz.koto.kotiheating.model.entity.HeatingDeviceStatus
import cz.koto.kotiheating.model.entity.MockHeatingStatusLiveData
import cz.koto.ktools.inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async

class HeatingRepository {

	val cache by inject<HeatingCache>()

	fun getStatus(deviceId: String?) = if (deviceId == null) {
		MockHeatingStatusLiveData("0")
	} else {
		HeatingStatusLiveData().apply { refresh(deviceId) }
	}

	fun hasLocalScheduleChanges(localValues: MutableList<MutableList<Int>>?, serverValues: MutableList<MutableList<Int>>): Boolean {

		if (localValues == null) return false
		serverValues.forEachIndexed { index, remoteItem ->
			if (remoteItem.zip(localValues[index]) { a, b -> a.compareTo(b) != 0 }.contains(true)) return true
		}
		return false
	}

	suspend fun updateStatus(schedule: HeatingDeviceStatus) {

		val query = GlobalScope.async(Dispatchers.Default) {
			cache.putStatus(schedule)
		}

		query.await()
	}

	suspend fun removeStatus(deviceId: String) {
		val query = GlobalScope.async(Dispatchers.Default) {
			cache.removeSchedule(deviceId)
		}

		query.await()
	}

//	suspend fun updateLocalChange(localChange: HeatingLocalChange) {
//
//		val query = GlobalScope.async(Dispatchers.Default) {
//			cache.putLocalChange(localChange)
//		}
//
//		query.await()
//	}

}