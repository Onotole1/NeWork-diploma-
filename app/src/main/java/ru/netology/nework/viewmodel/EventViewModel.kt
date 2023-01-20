package ru.netology.nework.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import androidx.lifecycle.*
import androidx.paging.*
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import ru.netology.nework.R
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.dto.*
import ru.netology.nework.dto.Event.Companion.emptyEvent
import ru.netology.nework.enumeration.AttachmentType
import ru.netology.nework.enumeration.EventType
import ru.netology.nework.enumeration.SeparatorTimeType
import ru.netology.nework.model.FeedModelState
import ru.netology.nework.model.FileModel
import ru.netology.nework.repository.event.EventRepository
import ru.netology.nework.util.CurrentTimes
import ru.netology.nework.enumeration.RetryTypes
import ru.netology.nework.util.SingleLiveEvent
import java.io.File
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.OffsetDateTime
import java.util.*

import javax.inject.Inject
import kotlin.random.Random

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class EventViewModel@Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: EventRepository,
    private val auth: AppAuth,
    private val currentTime: CurrentTimes,
    private val calendar: Calendar,
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

    val eventById: LiveData<Event>
        get() = _eventById
    private val _eventById = MutableLiveData<Event>()

    val authenticated = auth
        .authStateFlow.map { it.id != 0L }
        .asLiveData(Dispatchers.Default)

    private val _dataState = MutableLiveData<FeedModelState>()
    val dataState: LiveData<FeedModelState>
        get() = _dataState

    val edited = MutableLiveData(emptyEvent)

    private val _eventCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _eventCreated

    private val noFile = FileModel()
    private val _file = MutableLiveData(noFile)
    val file: LiveData<FileModel>
        get() = _file

    val datetime = MutableLiveData(OffsetDateTime.MIN)
    var type = EventType.NONE

    private var _speakerIds: MutableSet<Long> = mutableSetOf()

    fun edit(event: Event) = viewModelScope.launch {
        edited.value = event
    }

    fun cancelEdit() = viewModelScope.launch {
        edited.value = Event.emptyEvent
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
            edited.value = it.copy(
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
        edited.postValue(Event.emptyEvent.copy(authorId = auth.authStateFlow.value.id))
    }

    fun loadEvents() = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState(loading = true)
            repository.getAll()
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true, retryType = RetryTypes.LOAD)
        }
    }

    fun likeById(id: Long, likedByMe: Boolean) = viewModelScope.launch {
        try {
            repository.likeEventById(id, likedByMe)
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value =
                FeedModelState(error = true, retryType = RetryTypes.LIKE, retryId = id)
        }
    }

    fun refreshEvents() = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState(refreshing = true)
            repository.getAll()
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value =   FeedModelState(error = true, retryType = RetryTypes.RETRY)
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
        edited.value = edited.value?.copy(content = text)
    }
    fun changeEvent(content: String,
                    link: String?,
                    coords: Coordinates?
    ) {
        val text = content.trim()
        if (edited.value?.content == text) {
            return
        }
        edited.value = edited.value?.copy(content = text,
            datetime = datetime.value.toString(),
            type = type,
            link = link,
            coordinates = coords
        )
    }
    fun changeContent(event: Event) {
        if (edited.value == event) {
            return
        }
        edited.value = event.copy(published = calendar.time.time.toString())
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
                    _dataState.value = FeedModelState(error = true, retryType = RetryTypes.SAVE)
                }
            }
        }
        edited.value = Event.emptyEvent
        _file.value = noFile
    }

    fun save(content: String) = viewModelScope.launch {
        changeContent(content)
        save()
    }

    fun retrySave(event: Event?) {
        viewModelScope.launch {
            try {
                _dataState.postValue(FeedModelState(loading = true))
                if (event != null) {
                    repository.save(event)
                    loadEvents()
                }
                _dataState.postValue(FeedModelState())
            } catch (e: Exception) {
                _dataState.value =
                    FeedModelState(error = true, retryType = RetryTypes.SAVE, retryEvent = event)
            }
        }
    }

    fun getEventById(id: Long) = viewModelScope.launch {
        try {
            val event = repository.getEventById(id)

            _eventById.postValue(event.copy(published = convertTimeFormat(event.published), datetime = convertTimeFormat(event.datetime)))

        } catch (e: Exception) {

        }
    }

    fun removeById(id: Long) = viewModelScope.launch {
        try {
            _dataState.postValue(FeedModelState(loading = true))
            repository.removeById(id)
            _dataState.postValue(FeedModelState())
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
        edited.value = edited.value?.copy(speakerIds = _speakerIds)
        clearSpeaker()
    }

    fun clearSpeaker() {
        _speakerIds = mutableSetOf()
    }

    fun saveCoordinates(latitude: Double, longitude: Double) {
        edited.value = edited.value?.copy(coordinates = Coordinates(latitude, longitude))
    }

    fun joinById(id: Long) = viewModelScope.launch {
        try {
            _dataState.postValue(FeedModelState(loading = true))
            repository.joinById(id)
            _dataState.postValue(FeedModelState())
        } catch (e: Exception) {
            _dataState.value =
                FeedModelState(error = true, retryType = RetryTypes.PARTICIPATE, retryId = id)
        }
    }

    fun denyById(id: Long) = viewModelScope.launch {
        try {
            _dataState.postValue(FeedModelState(loading = true))
            repository.leaveById(id)
            _dataState.postValue(FeedModelState())
        } catch (e: Exception) {
            _dataState.value =
                FeedModelState(error = true, retryType = RetryTypes.UNPARTICIPATE, retryId = id)
        }
    }

    private fun convertTimeFormat(timeString: String): String {
        return try {
            val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
                .parse(timeString.replace("T", " ").replace("Z", ""))
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date)
        } catch (e: Exception) {
            timeString
        }
    }
    fun changeDatetime(datetime: Long) {
        if (datetime < 0) {
            return
        }
        edited.value = edited.value?.copy(datetime = Instant.ofEpochMilli(datetime).toString())
    }

    fun changeEventType(type: String) {
        when (type) {
            EventType.OFFLINE.toString() -> edited.value = edited.value?.copy(type = EventType.OFFLINE)
            else -> edited.value = edited.value?.copy(type = EventType.ONLINE)
        }
    }

    override fun onCleared() {
        super.onCleared()
        scope.cancel()
    }

}