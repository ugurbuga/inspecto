import Foundation
import Shared

class InspectoURLProtocol: URLProtocol {
    private var startTime: TimeInterval = 0
    private var responseData: NSMutableData?
    private var httpResponse: HTTPURLResponse?
    private var taskSession: URLSession?

    override class func canInit(with request: URLRequest) -> Bool {
        if URLProtocol.property(forKey: "InspectoURLProtocolHandledKey", in: request) != nil {
            return false
        }
        guard let scheme = request.url?.scheme?.lowercased() else { return false }
        return scheme == "http" || scheme == "https"
    }

    override class func canonicalRequest(for request: URLRequest) -> URLRequest {
        return request
    }

    override func startLoading() {
        let mutableRequest = (request as NSURLRequest).mutableCopy() as! NSMutableURLRequest
        URLProtocol.setProperty(true, forKey: "InspectoURLProtocolHandledKey", in: mutableRequest)
        startTime = Date().timeIntervalSince1970
        responseData = NSMutableData()

        let config = URLSessionConfiguration.default
        taskSession = URLSession(configuration: config, delegate: self, delegateQueue: nil)
        taskSession?.dataTask(with: mutableRequest as URLRequest).resume()
    }

    override func stopLoading() {
        taskSession?.invalidateAndCancel()
    }
}

extension InspectoURLProtocol: URLSessionDataDelegate {
    func urlSession(_ session: URLSession, dataTask: URLSessionDataTask, didReceive response: URLResponse, completionHandler: @escaping (URLSession.ResponseDisposition) -> Void) {
        httpResponse = response as? HTTPURLResponse
        client?.urlProtocol(self, didReceive: response, cacheStoragePolicy: .allowed)
        completionHandler(.allow)
    }

    func urlSession(_ session: URLSession, dataTask: URLSessionDataTask, didReceive data: Data) {
        responseData?.append(data)
        client?.urlProtocol(self, didLoad: data)
    }

    func urlSession(_ session: URLSession, task: URLSessionTask, didCompleteWithError error: Error?) {
        let duration = Int64((Date().timeIntervalSince1970 - startTime) * 1000)
        let url = request.url?.absoluteString ?? ""
        let method = request.httpMethod ?? "GET"

        var reqHeaders: [String: String] = [:]
        if let headers = request.allHTTPHeaderFields {
            reqHeaders = headers
        }

        var resHeaders: [String: String] = [:]
        if let headers = httpResponse?.allHeaderFields as? [String: String] {
            resHeaders = headers
        }

        var reqBodyString = ""
        if let bodyData = request.httpBody, let str = String(data: bodyData, encoding: .utf8) {
            reqBodyString = str
        }

        var resBodyString = ""
        if let data = responseData as Data?, let str = String(data: data, encoding: .utf8) {
            resBodyString = str
        }

        if let err = error {
            client?.urlProtocol(self, didFailWithError: err)
            resBodyString = "Error: \(err.localizedDescription)"
        } else {
            client?.urlProtocolDidFinishLoading(self)
        }

        let statusCode = httpResponse?.statusCode ?? (error != nil ? 0 : 200)

        Inspecto.shared.addLog(
            url: url,
            method: method,
            statusCode: Int32(statusCode),
            durationMs: duration,
            requestHeaders: reqHeaders,
            requestQueryParams: [:],
            requestBody: reqBodyString.isEmpty ? nil : reqBodyString,
            responseHeaders: resHeaders,
            responseBody: resBodyString.isEmpty ? nil : resBodyString,
            tag: InspectoTag.shared.NSURLSESSION
        )
    }
}
