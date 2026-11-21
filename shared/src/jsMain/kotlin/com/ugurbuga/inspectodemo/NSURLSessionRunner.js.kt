package com.ugurbuga.inspectodemo

actual val isNSURLSessionSupported: Boolean = false
actual fun performNSURLSessionTestRequest(url: String, onResult: (String) -> Unit) {
    onResult("NSURLSession is only supported on iOS")
}
