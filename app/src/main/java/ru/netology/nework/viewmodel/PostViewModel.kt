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
import ru.netology.nework.enumeration.RetryTypes
import ru.netology.nework.enumeration.SeparatorTimeType
import ru.netology.nework.model.*
import ru.netology.nework.repository.WallRepositoryImpl
import ru.netology.nework.repository.post.PostRepository
import ru.netology.nework.ui.user.USER_ID
import ru.netology.nework.util.*
import java.io.InputStream

import java.text.SimpleDateFormat
import javax.inject.Inject
import kotlin.random.Random

private val noMedia = MediaModel()

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class PostViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: PostRepository,
    private val wallRepository: WallRepositoryImpl,
    private val auth: AppAuth,
    private val currentTime: CurrentTimes,
    private val stateHandle: SavedStateHandle,
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

    private val _media = MutableLiveData(noMedia)
    val media: LiveData<MediaModel>
        get() = _media

    private var _mentionIds: MutableSet<Long> = mutableSetOf()

    fun edit(post: Post) = viewModelScope.launch {
        _edited.value = post
    }
    fun cancelEdit() = viewModelScope.launch {
        _edited.value = emptyPost
    }

    val hidePosts = mutableSetOf<Post>()

    fun hidePost(post:Post) {
        hidePosts.add(post)
    }

    private val scope = MainScope()

    init {
        loadPosts()
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
            _dataState.value = FeedModelState(error = true, retryType = RetryTypes.LOAD)
        }
    }

    fun attachmentRepost(attachment: Attachment) {
        edited.value?.let {
            _edited.value = it.copy(attachment = attachment)
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
            _dataState.value = FeedModelState(error = true, retryType = RetryTypes.RETRY)
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

    fun changeContent(content: String,coord: Coordinates?) {
        edited.value?.let {
            val text = content.trim()
        if (it.content != text ||
            it.coordinates != coord )
            _edited.value = it.copy(content = text, coordinates = coord)
    }
    }

    fun save() {
        edited.value?.let {post ->
            _postCreated.value = Unit
            viewModelScope.launch {
                _dataState.postValue(FeedModelState(loading = true))
                try {
                    when (_media.value) {
                        noMedia  -> repository.save(post)
                        else -> _media.value?.inputStream?.let { MediaUpload(it) }
                            ?.let { repository.saveWithAttachment(post,it,_media.value?.type!!)
                        }
                    }
                    _dataState.value = FeedModelState()
                } catch (e: Exception) {
                    _dataState.value = FeedModelState(error = true)
                }
            }
        }
        _edited.value = emptyPost
        _media.value = noMedia
    }

    fun changeMedia(uri: Uri?, inputStream: InputStream?, type: AttachmentType?) {
        _media.value = MediaModel(uri, inputStream, type)
    }

    fun save(content: String) = viewModelScope.launch {
            changeContent(content)
            save()
    }

    fun retrySave(post: Post?) {
        viewModelScope.launch {
            try {
                if (post != null) {
                    repository.save(post)
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

    fun changeMentionIds(id: Long) {
        _edited.value?.let {
            if (_edited.value?.mentionIds?.contains(id) == false) {
                _edited.value = _edited.value?.copy(mentionIds = it.mentionIds.plus(id))
            }
        }
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

    fun saveCoordinates(coordinates:Coordinates) {
        _edited.value = edited.value?.copy(coordinates = coordinates)
    }
    @OptIn(ExperimentalCoroutinesApi::class)
    val userWall: Flow<PagingData<FeedItem>> =  auth.authStateFlow
        .flatMapLatest { (myId, _) ->
            wallRepository.userWall(stateHandle[USER_ID] ?: myId).map { pagingData ->
                pagingData.map { post ->
                    post.copy(
                        published = SimpleDateFormat("dd.MM.yy HH:mm:ss").format(
                            post.published.toLong() * 1000L),
                        likedByMe = post.likeOwnerIds.contains(myId),
                        mentionedMe= post.mentionIds.contains(myId),
                        ownedByMe = post.authorId == myId,
                    )
                }
            }
        }
    fun refreshWall(userId:Long) = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState(refreshing = true)
            wallRepository.userWall(userId)
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
    }

    fun loadWallPosts(userId:Long) = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState(loading = true)
            wallRepository.userWall(userId)
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
    }

    override fun onCleared() {
        super.onCleared()
        scope.cancel()
    }

}

