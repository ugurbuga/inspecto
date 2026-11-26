package com.ugurbuga.inspectodemo

expect fun performNSURLSessionTestRequest(
    url: String,
    method: String = "GET",
    body: String? = null,
    onResult: (String) -> Unit
)

expect fun performNSURLSessionDelegateTestRequest(
    url: String,
    method: String = "GET",
    body: String? = null,
    onResult: (String) -> Unit
)

expect val isNSURLSessionSupported: Boolean
