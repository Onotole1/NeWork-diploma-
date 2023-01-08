package ru.netology.nework.entity
const val LIST_DELIMITER = ','
@JvmName("toEntityLong")
internal fun List<Long>.fromDto(): String {
    val sb = StringBuilder()
    for (i in 0 until this.size) {
        sb.append(this[i])
        if (i < this.size - 1) {
            sb.append(LIST_DELIMITER)
        }
    }
    return sb.toString()
}

internal fun String.toDto(): List<Long> {
    if (this == "") {
        return emptyList()
    }
    return this.split(LIST_DELIMITER).map { it.toLong() }
}