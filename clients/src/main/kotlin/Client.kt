import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class Client {
    companion object {
        private val client = HttpClient.newBuilder().build()
    }

    private fun sendRequest(url: String): String {
        val request = HttpRequest.newBuilder().uri(URI.create(url)).build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    fun getShareInfo(id: String): Share? {
        val response = sendRequest("http://localhost:8080/getSharesInfo?id=$id")
        if (!response.startsWith("Share")) return null
        val (amount, price) = response
            .removePrefix("Share{")
            .removeSuffix("}")
            .split(", ")
            .map { it.split("=").last() }
        return Share(amount.toInt(), price.toDouble())
    }

    fun buyShares(id: String, count: Int) =
        sendRequest("http://localhost:8080/buyShares?id=$id&shares=$count")

    fun sellShares(id: String, count: Int) =
        sendRequest("http://localhost:8080/sellShares?id=$id&shares=$count")
}
