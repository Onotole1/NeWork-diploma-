package ru.netology.nework.repository

import ru.netology.nework.api.ApiService
import ru.netology.nework.db.AppDb
import ru.netology.nework.dto.Attachment
import ru.netology.nework.dto.MediaUpload
import ru.netology.nework.enumeration.AttachmentType
import ru.netology.nework.error.NetworkError
import ru.netology.nework.error.UnknownError

import java.io.IOException
import javax.inject.Inject

interface FeedRepository @Inject constructor(
    appDb: AppDb,
    private val dao: FeedDao,
    private val apiService: ApiService,
    RemoteKeyDao: FeedRemoteKeyDao,
) {

    override suspend fun saveWithAttachment(event: Event, upload: MediaUpload) {
        try {
            val media = ru.netology.nework.repository.uploadWithContent(upload, apiService)
            val eventWithAttachment =
                event.copy(attachment = Attachment(media.url, AttachmentType.IMAGE))
            save(eventWithAttachment)
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }
}