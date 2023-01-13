package ru.netology.nework.viewmodel

import androidx.lifecycle.*
import androidx.paging.PagingData
import androidx.paging.map
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.netology.nework.api.*
import ru.netology.nework.auth.AppAuth
import ru.netology.nework.dto.FeedItem
import ru.netology.nework.dto.User
import ru.netology.nework.hiltModules.CurrentTime
import ru.netology.nework.model.FeedModelState
import ru.netology.nework.model.UserModel
import ru.netology.nework.model.UsersModelState
import ru.netology.nework.repository.UserRepository
import ru.netology.nework.repository.WallRepository
import ru.netology.nework.ui.USER_ID
import java.text.SimpleDateFormat
import javax.inject.Inject

class WallViewModel@Inject constructor(
    private val repository: WallRepository,
    private val userRepository: UserRepository,
    private val auth: AppAuth,
    stateHandle: SavedStateHandle,
) : ViewModel() {

    val authenticated = auth
        .authStateFlow.map { it.id != 0L }
        .asLiveData(Dispatchers.Default)

    private val _dataState = MutableLiveData<FeedModelState>()
    val dataState: LiveData<FeedModelState>
        get() = _dataState

    @OptIn(ExperimentalCoroutinesApi::class)
    val userWall: Flow<PagingData<FeedItem>> = auth.authStateFlow
        .flatMapLatest { (myId, _) ->
            repository.userWall(stateHandle[USER_ID] ?: myId).map { pagingData ->
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
            repository.userWall(userId)
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
    }

    fun loadWallPosts(userId:Long) = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState(loading = true)
            repository.userWall(userId)
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
    }

}