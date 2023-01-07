package ru.netology.nmedia.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.*
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.insertSeparators
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import ru.netology.nmedia.R
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.dto.*
import ru.netology.nmedia.enumeration.AttachmentType
import ru.netology.nmedia.enumeration.SeparatorTimeType
import ru.netology.nmedia.hiltModules.CurrentTime
import ru.netology.nmedia.model.FeedModelState
import ru.netology.nmedia.model.PhotoModel
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.util.RetryTypes
import ru.netology.nmedia.util.SingleLiveEvent
import java.io.File
import java.text.SimpleDateFormat
import javax.inject.Inject
import kotlin.random.Random

private val empty = Post(
    id = 0,
    content = "",
    authorAvatar = "",
    author = "",
    authorId = 0,
    likedByMe = false,
    published = 221220L.toString(),
    likes = 0,
    viewed = false,
    repost = 0,
    views = 0,
    video = "",
    attachment = Attachment(
        url = "http://10.0.2.2:9999/media/d7dff806-4456-4e35-a6a1-9f2278c5d639.png",
        type = AttachmentType.IMAGE
    )
)
private val noPhoto = PhotoModel()

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel

class PostViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: PostRepository,
    private val auth: AppAuth,
    private val currentTime: CurrentTime
) : ViewModel() {


    /*  private val cached = repository
        .data
        .cachedIn(viewModelScope)*/

    private val cached
        get() = repository.data.cachedIn(viewModelScope)

    /*private val cached: Flow<PagingData<Post>> = repository
        .data
        .map { pagingData ->
            pagingData.insertSeparators(
                generator = { before, _ ->
                    if (before?.id?.rem(5) == 0L) {
                        Ad(Random.nextLong(), "figma.jpg")
                    } else {
                        null
                    }
                })
        }
        .cachedIn(viewModelScope)*/

    @SuppressLint("SimpleDateFormat")
    val data: Flow<PagingData<FeedItem>> = auth.authStateFlow
        .flatMapLatest { (myId, _) ->
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
                            is Post -> {
                                feedItem.copy(
                                    ownedByMe = feedItem.authorId == myId,
                                    published = SimpleDateFormat("dd.MM.yy HH:mm:ss").format(feedItem.published.toLong() * 1000L)
                                )
                            }
                            is TextItemSeparator -> {
                                feedItem
                            }
                            else -> {
                                feedItem
                            }
                        }
                    }
            }
        }

    val authenticated = auth
        .authStateFlow.map { it.id != 0L }
        .asLiveData(Dispatchers.Default)

    private val _dataState = MutableLiveData<FeedModelState>()
    val dataState: LiveData<FeedModelState>
        get() = _dataState

    val edited: MutableLiveData<Post> = MutableLiveData(empty)
    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated


    val newerCount: LiveData<Int> = repository.getNewerCount()
        .catch { e -> e.printStackTrace() }
        .asLiveData(Dispatchers.Default)

    /*MutableLiveData(empty.id)*/
    /* val newerCount: LiveData<Int> = data.switchMap {
            repository.getNewerCount(it.posts.firstOrNull()?.id ?: 0L)
                .catch { e -> e.printStackTrace() }
                .asLiveData(Dispatchers.Default)
        }
    }*/
    fun markNewerPostsViewed() = viewModelScope.launch {
        try {
            repository.newerPostsViewed()
        } catch (e: Exception) {
            println("PW $e")
            return@launch
        }
    }

    private val _photo = MutableLiveData(noPhoto)
    val photo: LiveData<PhotoModel>
        get() = _photo

    private val scope = MainScope()

    init {
        loadPosts()
    }

    fun forAuthenticated() {
        edited.postValue(empty.copy(authorId = auth.authStateFlow.value.id))
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
        edited.value = edited.value?.copy(content = text)
    }


    fun save() {
        edited.value?.let {
            _postCreated.value = Unit
            viewModelScope.launch {
                try {
                    when (_photo.value) {
                        noPhoto -> repository.save(it)
                        else -> _photo.value?.file?.let { file ->
                            repository.saveWithAttachment(it, MediaUpload(file))
                        }
                    }
                    _dataState.value = FeedModelState()
                } catch (e: Exception) {
                    _dataState.value = FeedModelState(error = true)
                }
            }
        }
        edited.value = empty
        _photo.value = noPhoto
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

    fun edit(post: Post) {
        edited.value = post
    }

    fun changePhoto(uri: Uri?, file: File?) {
        _photo.value = if (uri != null && file != null) {
            PhotoModel(uri, file)
        } else {
            null
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

    override fun onCleared() {
        super.onCleared()
        scope.cancel()
    }

    fun loadNewPosts() = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState(loading = true)
            repository.getNewPosts()
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
    }

    fun markRead() {
        viewModelScope.launch {
            repository.markRead()
        }
    }

    fun cancel() {
        edited.value = empty
    }

    fun registerUser(login: String, pass: String, name: String) = viewModelScope.launch {
        try {
            val authState = repository.registerUser(login, pass, name)
            auth.setAuth(authState.id, authState.token)
        } catch (e: Exception) {
            Log.i("updateUser", e.message.toString())
        }
    }

}

