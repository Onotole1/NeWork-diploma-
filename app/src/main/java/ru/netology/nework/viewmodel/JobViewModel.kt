package ru.netology.nework.viewmodel

import android.util.Patterns
import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import ru.netology.nework.R
import ru.netology.nework.dto.Job
import ru.netology.nework.dto.Job.Companion.emptyJob
import ru.netology.nework.dto.User
import ru.netology.nework.model.FeedModelState
import ru.netology.nework.repository.JobRepository
import ru.netology.nework.enumeration.RetryTypes
import ru.netology.nework.util.SingleLiveEvent
import javax.inject.Inject

@HiltViewModel
class JobViewModel @Inject constructor(
    private val repository: JobRepository,
    userId:Long
) : ViewModel() {

    val jobData = repository.data


    private val _dataState = MutableLiveData<FeedModelState>()
    val dataState: LiveData<FeedModelState>
        get() = _dataState

    private val edited = MutableLiveData(emptyJob)

    private val _jobCreated = SingleLiveEvent<Unit>()
    val jobCreated: LiveData<Unit>
        get() = _jobCreated

    init {
        getJobsByUserId(userId)
    }

    fun getJobsByUserId(id: Long) = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState(loading = true)
            repository.getJobsByUserId(id)
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
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

    fun hideById(id: Long) = viewModelScope.launch {
        try {
            repository.removeById(id)
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value =
                FeedModelState(error = true, retryType = RetryTypes.HIDE, retryId = id)
        }
    }

    fun edit(job: Job) {
        edited.value = job
    }

    fun changeData(start: Long, finish: Long?, company: String, link: String?, position: String) {
        edited.value?.let {
            val textCompany = company.trim()
            val textLink = link!!.trim()
            val textPosition = position.trim()

            if (it.start != start ||
                it.finish != finish ||
                it.name != textCompany ||
                it.link != textLink ||
                it.position != textPosition
            )
                edited.value = it.copy(
                    start = start,
                    finish = finish,
                    name = textCompany,
                    link = textLink,
                    position = textPosition
                )
        }
    }

        fun isLinkValid(link: String) {
            val linkTrim = link.trim()
            if (!Patterns.WEB_URL.matcher(linkTrim).matches()) {
                _dataState.value = FeedModelState(linkError = R.string.invalid_link)
            } else {
                _dataState.value = FeedModelState(isDataValid = true)
            }
        }

        fun requireData(
            start: String, position: String, company: String
        ) {
            _dataState.value = FeedModelState(
                emptyToDate = if (start.isBlank()) R.string.empty_field else null,
                emptyPositionError = if (position.isBlank()) R.string.empty_field else null,
                emptyCompanyError = if (company.isBlank()) R.string.empty_field else null,
                isDataNotBlank =
                start.isNotBlank()
                        &&
                        position.isNotBlank() &&
                        company.isNotBlank()
            )

        }

       /* fun save() {
            edited.value?.let {
                _jobCreated.value = Unit
                viewModelScope.launch {
                   try{
                       repository.saveJob(it)
                        _dataState.value = FeedModelState()
                        edited.value = emptyJob
                    } catch (e: Exception) {
                        _dataState.value = FeedModelState(error = true)
                    }
                }
            }
        }*/

   /* suspend fun getJobName(id: Long) {
        try{
            repository.getJobName(id)
            _dataState.value = FeedModelState()
            edited.value = emptyJob
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
    }*/
    fun changeStart(start: Long) {
        edited.value = edited.value?.copy(start = start)
    }

    fun changeFinish(finish: Long? = null) {
        edited.value = edited.value?.copy(finish = finish)
    }

    fun changeNameAndPosition(name: String, position: String) {
        edited.value = edited.value?.copy(name = name, position = position)
    }

    fun changeLink(link: String? = null) {
        edited.value = edited.value?.copy(link = link)
    }
    fun save(userId: Long) = viewModelScope.launch {
        try {
            _dataState.postValue(FeedModelState(loading = true))
            edited.value?.let { job ->
                userId.let {
                    repository.saveJob(job,userId)
                }
            }
            _dataState.postValue(FeedModelState())
            edited.postValue(emptyJob)
        } catch (e: Exception) {
            FeedModelState(error = true)
        }
    }
}