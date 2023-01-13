package ru.netology.nework.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import androidx.lifecycle.*
import androidx.paging.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.netology.nework.R
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.dto.*
import ru.netology.nework.enumeration.AttachmentType
import ru.netology.nework.enumeration.SeparatorTimeType
import ru.netology.nework.model.FeedModelState
import ru.netology.nework.model.FileModel
import ru.netology.nework.repository.event.EventRepository
import ru.netology.nework.util.CurrentTimes
import ru.netology.nework.util.RetryTypes
import ru.netology.nework.util.SingleLiveEvent
import java.io.File
import java.text.SimpleDateFormat

import javax.inject.Inject
import kotlin.random.Random

class EventViewModel@Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: EventRepository,
    private val auth: AppAuth,
    private val currentTime: CurrentTimes
) : ViewModel() {

    private val cached
        get() = repository.data.cachedIn(viewModelScope)

    @SuppressLint("SimpleDateFormat")

    val data: Flow<PagingData<FeedItem>> = auth.authStateFlow.
    flatMapLatest { (myId, _) ->
            cached.map { pagingData ->
                pagingData.insertSeparators(
                    generator = { before, after ->
                        val beforeTime =currentTime.getDaySeparatorType(time = before?.published?.toLong())
                        val afterTime = currentTime.getDaySeparatorType(after?.published?.toLong())

                        val text = when {
                            beforeTime == SeparatorTimeType.NULL && afterTime == SeparatorTimeType.TODAY ->
                                context.getString(R.string.today)
                            beforeTime == SeparatorTimeType.TODAY && afterTime == SeparatorTimeType.YESTERDAY ->
                                context.getString(R.string.yesterday)
                            beforeTime == SeparatorTimeType.YESTERDAY && afterTime == SeparatorTimeType.MORE_OLD ->
                                context.getString(R.string.more_old)
                            else -> null
                        }
                        text?.let { TextItemSeparator(Random.nextLong(), it) } ?: run { null }
                    }
                )
                pagingData.insertSeparators(
                    generator = { before, _ ->
                        if (before?.id?.rem(5) == 0L) {
                            Ad(Random.nextLong(), "figma.jpg")
                        } else {
                            null
                        }
                    })
                    .map { feedItem ->
                        when (feedItem) {
                            is Event -> {
                                feedItem.copy(
                                    ownedByMe = feedItem.authorId == myId,
                                    published = SimpleDateFormat("dd.MM.yy HH:mm:ss").format(
                                        feedItem.published.toLong() * 1000L),
                                    likedByMe = feedItem.likeOwnerIds.contains(myId),
                                    participatedByMe= feedItem.participantsIds.contains(myId),
                                )
                            }
                            is TextItemSeparator -> {
                                feedItem
                            }
                            else -> {
                                feedItem
                            }
                        }
                    }                .filter { validEvent ->
                        !hideEvents.contains(validEvent)
                    }
            }
        }


    val authenticated = auth
        .authStateFlow.map { it.id != 0L }
        .asLiveData(Dispatchers.Default)

    private val _dataState = MutableLiveData<FeedModelState>()
    val dataState: LiveData<FeedModelState>
        get() = _dataState

    private val _edited = MutableLiveData(Event.emptyEvent)
    val edited: LiveData<Event>
        get() = _edited

    private val _eventCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _eventCreated

    private val noFile = FileModel()
    private val _file = MutableLiveData(noFile)
    val file: LiveData<FileModel>
        get() = _file

    private var _speakerIds: MutableSet<Long> = mutableSetOf()

    fun edit(event: Event) = viewModelScope.launch {
        _edited.value = event
    }
    fun cancelEdit() = viewModelScope.launch {
        _edited.value = Event.emptyEvent
    }

    private val hideEvents = mutableSetOf<Event>()

    fun hideEvent(event: Event) {
        hideEvents.add(event)
    }
    private val scope = MainScope()

    init {
        loadEvents()
    }

    fun changeFile(uri: Uri?, type: AttachmentType?) {
        _file.value = FileModel(uri, type=type)
        edited.value?.let {
            _edited.value = it.copy(
                attachment = null
            )
        }
    }

    fun changePhoto(uri: Uri?, file: File?) {
        _file.value = if (uri != null && file != null) {
            FileModel(uri, file)
        } else {
            null
        }
    }

    fun forAuthenticated() {
        _edited.postValue(Event.emptyEvent.copy(authorId = auth.authStateFlow.value.id))
    }


    fun loadEvents() = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState(loading = true)
            repository.getAll()
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
    }

    fun likeById(id: Long, likedByMe: Boolean) = viewModelScope.launch {
        try {
            repository.likeById(id, likedByMe)
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value =
                FeedModelState(error = true, retryType = RetryTypes.REMOVE, retryId = id)
        }
    }

    fun refreshPosts() = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState(refreshing = true)
            repository.getAll()
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
    }

    fun shareById(id: Long) = viewModelScope.launch {
        repository.shareById(id)

    }

    fun changeContent(content: String) {
        val text = content.trim()
        if (edited.value?.content == text) {
            return
        }
        _edited.value = edited.value?.copy(content = text)
    }


    fun save() {
        edited.value?.let {
            _eventCreated.value = Unit
            viewModelScope.launch {
                try {
                    repository.saveWithAttachment(
                        it, _file.value?.uri?.let { it }!!, _file.value?.type!!
                    )
                    when (_file.value) {
                        noFile -> repository.save(it)
                        else -> _file.value?.uri?.let { uri ->
                            repository.saveWithAttachment(it, uri,
                                AttachmentType.IMAGE)
                        }
                    }
                    _dataState.value = FeedModelState()
                } catch (e: Exception) {
                    _dataState.value = FeedModelState(error = true)
                }
            }
        }
        _edited.value = Event.emptyEvent
        _file.value = noFile
    }

    fun save(content: String) = viewModelScope.launch {
        changeContent(content)
        save()
    }

    fun retrySave(event: Event?) {
        viewModelScope.launch {
            try {
                if (event != null) {
                    repository.save(event)
                    loadEvents()
                }
            } catch (e: Exception) {
                _dataState.value =
                    FeedModelState(error = true, retryType = RetryTypes.SAVE, retryEvent = event)
            }
        }
    }

    fun removeById(id: Long) = viewModelScope.launch {
        try {
            repository.removeById(id)
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value =
                FeedModelState(error = true, retryType = RetryTypes.REMOVE, retryId = id)
        }
    }

    fun checkSpeaker(id: Long) {
        _speakerIds.let {
            if (it.contains(id))
                it.remove(id)
            else
                it.add(id)
        }
    }

    fun isCheckboxSpeaker(id: Long): Boolean {
        return _speakerIds.any { it == id }
    }

    fun saveSpeaker() {
        _edited.value = edited.value?.copy(speakerIds = _speakerIds)
        clearSpeaker()
    }

    fun clearSpeaker() {
        _speakerIds = mutableSetOf()
    }

    fun saveCoordinates(latitude: Double, longitude: Double) {
        _edited.value = edited.value?.copy(coordinates = Coordinates(latitude, longitude))
    }

    override fun onCleared() {
        super.onCleared()
        scope.cancel()
    }
    fun joinById(id: Long) = viewModelScope.launch {
        try {
            repository.joinById(id)
        } catch (e: Exception) {
            _dataState.value =
                FeedModelState(error = true, retryType = RetryTypes.PARTICIPATE, retryId = id)
        }
    }

    fun denyById(id: Long) = viewModelScope.launch {
        try {
            repository.denyById(id)
        } catch (e: Exception) {
            _dataState.value =
                FeedModelState(error = true, retryType = RetryTypes.UNPARTICIPATE, retryId = id)
        }
    }
}