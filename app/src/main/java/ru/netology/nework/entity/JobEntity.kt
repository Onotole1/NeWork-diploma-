package ru.netology.nework.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.nework.dto.Job

@Entity(tableName = "jobs")
data class JobEntity (
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val name: String,
    val position: String,
    val start: Long?,
    val finish: Long? = null,
    val link: String? = null,
    val ownedId: Long,
    val ownedByMe: Boolean=false,
    ) {
        fun toDto() = Job(
            id,
            name,
            position,
            start,
            finish,
            link,
            ownedId,
            ownedByMe
        )

        companion object {
            fun fromDto(dto: Job) =
                JobEntity(
                    dto.id,
                    dto.name,
                    dto.position,
                    dto.start,
                    dto.finish,
                    dto.link,
                    dto.ownerId,
                    dto.ownedByMe
                )

        }
    }

    fun List<JobEntity>.toDto(): List<Job> = map(JobEntity::toDto)
    fun List<Job>.toEntity(): List<JobEntity> = map(JobEntity.Companion::fromDto)