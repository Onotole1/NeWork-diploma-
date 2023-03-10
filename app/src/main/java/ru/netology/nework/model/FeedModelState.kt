package ru.netology.nework.model

import ru.netology.nework.dto.Event
import ru.netology.nework.dto.Post
import ru.netology.nework.enumeration.RetryTypes


data class FeedModelState(
    val loading: Boolean = false,
    val error: Boolean = false,
    val refreshing: Boolean = false,
    val retryId: Long = 0,
    val retryType: RetryTypes? = null,
    val retryPost: Post? = null,
    val retryEvent: Event? = null,
    val linkError: Int? = null,
    val isDataValid: Boolean = false,
    val emptyToDate:Int? = null,
    val isDataNotBlank: Boolean = false,
    val emptyPositionError: Int? = null,
    val emptyCompanyError: Int? = null,
    val loginError: Boolean = false,
    val passwordError: Int? = null,
    val errorAuth: Boolean = false,
    val errorRegistration: Boolean = false,

)