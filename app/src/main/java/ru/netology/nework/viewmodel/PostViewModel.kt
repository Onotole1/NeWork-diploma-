package ru.netology.nework.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import androidx.lifecycle.*
import androidx.paging.*
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import ru.netology.nework.R
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.dto.*
import ru.netology.nework.dto.Post.Companion.emptyPost
import ru.netology.nework.enumeration.AttachmentType
import ru.netology.nework.enumeration.SeparatorTimeType
import ru.netology.nework.model.FeedModelState
import ru.netology.nework.model.FileModel
import ru.netology.nework.repository.post.PostRepository
import ru.netology.nework.util.*

import java.io.File
import java.text.SimpleDateFormat
import javax.inject.Inject
import kotlin.random.Random


private val noPhoto = FileModel()

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class PostViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: PostRepository,
    private val auth: AppAuth,
    private val currentTime: CurrentTimes
) : ViewModel() {

    private val cached
        get() = repository.data.cachedIn(viewModelScope)

    @SuppressLint("SimpleDateFormat")
    val data: Flow<PagingData<FeedItem>> = auth.authStateFlow
        .flatMapLatest { (myId, _) ->
            cached.map { pagingData ->
                pagingData.insertSeparators(
                    generator = { before, after ->
                        val beforeTime =
                            currentTime.getDaySeparatorType(time = before?.published?.toLong())
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
                    .map { feedItem->
                        when (feedItem) {
                            is Post -> {
                                feedItem.copy(
                                    ownedByMe = feedItem.authorId == myId,
                                    published = SimpleDateFormat("dd.MM.yy HH:mm:ss").format(
                                        feedItem.published.toLong() * 1000L),
                                    likedByMe = feedItem.likeOwnerIds.contains(myId),
                                    mentionedMe= feedItem.mentionIds.contains(myId),
                                    )
                            }
                            is TextItemSeparator -> {
                                feedItem
                            }
                            else -> {
                                feedItem
                            }
                        }
                    }                .filter { validPost ->
                        !hidePosts.contains(validPost)
                    }
            }
        }


    val authenticated = auth
        .authStateFlow.map { it.id != 0L }
        .asLiveData(Dispatchers.Default)

    private val _dataState = MutableLiveData<FeedModelState>()
    val dataState: LiveData<FeedModelState>
        get() = _dataState

    private val _edited = MutableLiveData(emptyPost)
    val edited: LiveData<Post>
        get() = _edited

    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    private val noFile = FileModel()
    private val _file = MutableLiveData(noFile)
    val file: LiveData<FileModel>
        get() = _file

    private var _mentionIds: MutableSet<Long> = mutableSetOf()

    fun edit(post: Post) = viewModelScope.launch {
        _edited.value = post
    }
    fun cancelEdit() = viewModelScope.launch {
        _edited.value = emptyPost
    }

    private val hidePosts = mutableSetOf<Post>()

    fun hidePost(post:Post) {
        hidePosts.add(post)
    }
    private val scope = MainScope()

    init {
        loadPosts()
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
        _edited.postValue(emptyPost.copy(authorId = auth.authStateFlow.value.id))
    }


    fun loadPosts() = viewModelScope.launch {
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
            _postCreated.value = Unit
            viewModelScope.launch {
                try {
                    repository.saveWithAttachment(
                        it, _file.value?.uri?.let { MediaUpload(it) }!!, _file.value?.type!!,false
                    )
                    when (_file.value) {
                        noFile -> repository.save(it,false)
                        else -> _file.value?.uri?.let { uri ->
                            repository.saveWithAttachment(it, MediaUpload(uri),AttachmentType.IMAGE, false)
                        }
                    }
                    _dataState.value = FeedModelState()
                } catch (e: Exception) {
                    _dataState.value = FeedModelState(error = true)
                }
            }
        }
        _edited.value = emptyPost
        _file.value = noPhoto
    }

    fun save(content: String) = viewModelScope.launch {
            changeContent(content)
            save()
    }

    fun retrySave(post: Post?) {
        viewModelScope.launch {
            try {
                if (post != null) {
                    repository.save(post,false)
                    loadPosts()
                }
            } catch (e: Exception) {
                _dataState.value =
                    FeedModelState(error = true, retryType = RetryTypes.SAVE, retryPost = post)
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

    fun checkMention(id: Long) {
        _mentionIds.let {
            if (it.contains(id))
                it.remove(id)
            else
                it.add(id)
        }
    }

    fun isCheckboxMention(id: Long): Boolean {
        return _mentionIds.any { it == id }
    }

    fun saveMention() {
        _edited.value = edited.value?.copy(mentionIds = _mentionIds)
        clearMention()
    }

    fun clearMention() {
        _mentionIds = mutableSetOf()
    }

    fun saveCoordinates(latitude: Double, longitude: Double) {
        _edited.value = edited.value?.copy(coordinates = Coordinates(latitude, longitude))
    }

    override fun onCleared() {
        super.onCleared()
        scope.cancel()
    }
}

