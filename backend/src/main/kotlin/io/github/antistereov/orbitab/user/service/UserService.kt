package io.github.antistereov.orbitab.user.service

import io.github.antistereov.orbitab.user.exception.UserDoesNotExistException
import io.github.antistereov.orbitab.user.model.DeviceInfo
import io.github.antistereov.orbitab.user.model.UserDocument
import io.github.antistereov.orbitab.user.repository.UserRepository
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service

@Service
class UserService(
    private val userRepository: UserRepository,
) {

    private val logger: KLogger
        get() = KotlinLogging.logger {}

    suspend fun findById(userId: String): UserDocument {
        logger.debug { "Finding user by ID: $userId" }

        return userRepository.findById(userId) ?: throw UserDoesNotExistException(userId)
    }

    suspend fun findByIdOrNull(userId: String): UserDocument? {
        logger.debug { "Finding user by ID: $userId" }

        return userRepository.findById(userId)
    }

    suspend fun findByUsername(username: String): UserDocument? {
        logger.debug { "Fetching user with username $username" }

        return userRepository.findByUsername(username)
    }

    suspend fun existsByUsername(username: String): Boolean {
        logger.debug { "Checking if username $username already exists" }

        return userRepository.existsByUsername(username)
    }

    suspend fun save(user: UserDocument): UserDocument {
        logger.debug { "Saving user: ${user.id}" }

        return userRepository.save(user)
    }

    suspend fun delete(userId: String) {
        logger.debug { "Deleting user $userId" }

        userRepository.deleteById(userId)
    }

    suspend fun getDevices(userId: String): List<DeviceInfo> {
        logger.debug { "Getting devices for user $userId" }

        val user = findById(userId)
        return user.devices
    }

    suspend fun addOrUpdateDevice(userId: String, deviceInfo: DeviceInfo): UserDocument {
        logger.debug { "Adding or updating device ${deviceInfo.deviceId} for user $userId" }

        val user = findById(userId)
        val updatedDevices = user.devices.toMutableList()

        val existingDevice = updatedDevices.find { it.deviceId == deviceInfo.deviceId }
        if (existingDevice != null) {
            updatedDevices.remove(existingDevice)
        }

        updatedDevices.add(deviceInfo)
        return save(user.copy(devices = updatedDevices))
    }

    suspend fun deleteDevice(userId: String, deviceId: String): UserDocument {
        logger.debug { "Deleting device $deviceId for user $userId" }

        val user = findById(userId)
        val updatedDevices = user.devices.filterNot { it.deviceId == deviceId }

        return save(user.copy(devices = updatedDevices))
    }
}