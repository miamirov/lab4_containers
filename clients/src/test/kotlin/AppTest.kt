import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.FixedHostPortGenericContainer
import org.junit.Test
import org.junit.Before
import org.junit.ClassRule
import org.junit.Assert.assertEquals

class AppTest {
    companion object {
        @ClassRule
        @JvmField
        var simpleWebServer: GenericContainer<*> =
            FixedHostPortGenericContainer<FixedHostPortGenericContainer<Nothing>>("stock:1.0-SNAPSHOT")
                .withFixedExposedPort(8080, 8080)
                .withExposedPorts(8080)
        private val client = HttpClient.newBuilder().build()
        private val server = Server()
    }

    private fun sendRequest(url: String): String {
        val request = HttpRequest.newBuilder().uri(URI.create(url)).build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        return response.body()
    }

    @Before
    fun initiate() {
        sendRequest("http://localhost:8080/addCompany?id=1&price=10&amount=10")
        sendRequest("http://localhost:8080/addCompany?id=2&price=100&amount=100")
        server.addUser(mapOf("id" to listOf("1"), "balance" to listOf("50")))
        server.addUser(mapOf("id" to listOf("2"), "balance" to listOf("100000000"),))
        server.buyShares(mapOf("userId" to listOf("2"), "companyId" to listOf("2"), "amount" to listOf("100")))
    }

    @Test
    fun testAddingCompany() {
        assertEquals("ok", sendRequest("http://localhost:8080/addCompany?id=3&price=10&amount=0"))
        assertEquals("This company is already in stock.", sendRequest("http://localhost:8080/addCompany?id=3&price=10&amount=0"))
    }

    @Test
    fun testGetShares() {
        assertEquals(Share(10, 10.0).toString(), sendRequest("http://localhost:8080/shares?id=1"))
        assertEquals("This company is not in stock yet.", sendRequest("http://localhost:8080/shares?id=10"))
    }

    @Test
    fun testBuyingShares() {
        var params = mapOf(
            "userId" to listOf("1"),
            "companyId" to listOf("1"),
            "amount" to listOf("5"),
        )
        server.buyShares(params).test().assertValue("ok")
        server.buyShares(params).test().assertValue("User doesn't have enough money for purchase")
        params = mapOf(
            "userId" to listOf("2"),
            "companyId" to listOf("1"),
            "amount" to listOf("100"),
        )
        server.buyShares(params).test().assertValue("Company doesn't have this amount of shares")

    }

    @Test
    fun testSellingShares() {
        val params = mapOf(
            "userId" to listOf("2"),
            "companyId" to listOf("2"),
            "amount" to listOf("100"),
        )
        server.sellShares(params).test().assertValue("ok")
        server.sellShares(params).test().assertValue("User doesn't have this amount of shares")

    }

    @Test
    fun testBalance() {
        server.getBalance(mapOf("id" to listOf("1"))).test().assertValue(50.0.toString())
        server.getBalance(mapOf("id" to listOf("2"))).test().assertValue(100000000.0.toString())
    }
}
