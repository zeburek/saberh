package ru.zeburek.saberh.utils

fun getParse(regexp: Regex): (String, Any, String) -> String {
    return fun(line: String, field: Any, default: String): String {
        val parsed = regexp.find(line)
        if (parsed != null) {
            if (field is Int && parsed.groups[field] != null) {
                return parsed.groups[field]!!.value
            } else if (field is String && parsed.groups[field] != null) {
                return parsed.groups[field]!!.value
            }
        }
        return default
    }
}