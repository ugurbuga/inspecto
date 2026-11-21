package io.github.ugurbuga.inspectodemo

expect fun performNSURLSessionTestRequest(url: String, onResult: (String) -> Unit)
expect val isNSURLSessionSupported: Boolean
