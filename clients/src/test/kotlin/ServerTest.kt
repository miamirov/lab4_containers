import org.junit.jupiter.api.Test

class ServerTest {
    companion object {
        private val server = Server()
    }

    @Test
    fun addUser() {
        val params: Map<String, List<String>> = mapOf("id" to listOf("user"), "balance" to listOf("0"))
        server.addUser(params).test().assertValue("ok")
        server.addUser(params).test().assertValue("This user is already exists.")
    }

    @Test
    fun depositMoney() {
        server.addUser(mapOf("id" to listOf("user"), "balance" to listOf("0")))
        server.depositMoney(mapOf("id" to listOf("user"), "deposit" to listOf("100"))).test().assertValue(100.0.toString())
    }
}
