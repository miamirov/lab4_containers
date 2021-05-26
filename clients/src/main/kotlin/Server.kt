import io.netty.handler.codec.http.HttpResponseStatus
import io.reactivex.netty.protocol.http.server.HttpServer
import rx.Observable

class Server {
    private val client = Client()
    private val users: MutableMap<String, User> = mutableMapOf()

    fun run() = HttpServer.newServer(8080)
        .start { req, resp ->
            var response: Observable<String>
            val params = req.queryParameters
            val path = req.decodedPath

            try {
                when (path) {
                    "/addUser" -> {
                        response = addUser(params)
                        resp.setStatus(HttpResponseStatus.OK)
                    }
                    "/depositMoney" -> {
                        response = depositMoney(params)
                        resp.setStatus(HttpResponseStatus.OK)
                    }
                    "/getSharesInfo" -> {
                        response = getSharesInfo(params)
                        resp.setStatus(HttpResponseStatus.OK)
                    }
                    "/getBalance" -> {
                        response = getBalance(params)
                        resp.setStatus(HttpResponseStatus.OK)
                    }
                    "/buyShare" -> {
                        response = buyShares(params)
                        resp.setStatus(HttpResponseStatus.OK)
                    }
                    "/sellShare" -> {
                        response = sellShares(params)
                        resp.setStatus(HttpResponseStatus.OK)
                    }
                    else -> {
                        response = Observable.just("Wrong command")
                        resp.setStatus(HttpResponseStatus.BAD_REQUEST)
                    }
                }
            } catch (e: Exception) {
                response = Observable.just("Error occurred")
                resp.setStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR)
            }
            resp.writeString(response)
        }.awaitShutdown()

    fun addUser(queryParam: Map<String, List<String>>): Observable<String> {
        val id = queryParam["id"]!![0]

        val balance = queryParam["balance"]!![0].toDouble()

        if (id in users)
            return Observable.just("This user is already exists.")
        users[id] = User(balance)
        return Observable.just("ok")
    }

    fun depositMoney(queryParam: Map<String, List<String>>): Observable<String> {
        val id = queryParam["id"]!![0]
        val deposit = queryParam["deposit"]!![0].toDouble()
        val user = users[id]
            ?: return Observable.just("This user doesn't exist.")
        user.balance += deposit
        return Observable.just(user.balance.toString())
    }

    fun getSharesInfo(queryParam: Map<String, List<String>>): Observable<String> {
        val id = queryParam["id"]!![0]
        val user = users[id]
            ?: return Observable.just("This user doesn't exist.")
        return Observable.just(user.shares
            .map { (key, _) -> client.getShareInfo(key) }
            .joinToString(separator = "\n"))
    }

    fun getBalance(queryParam: Map<String, List<String>>): Observable<String> {
        val id = queryParam["id"]!![0]
        val user = users[id]
            ?: return Observable.just("This user doesn't exist.")
        val shares = user.shares
            .map { (key, value) -> value * (client.getShareInfo(key)?.price ?: 0.0) }
            .sum()
        val total = user.balance + shares
        return Observable.just(total.toString())
    }

    fun buyShares(queryParam: Map<String, List<String>>): Observable<String> {
        val userId = queryParam["userId"]!![0]

        val companyId = queryParam["companyId"]!![0]

        val amount = queryParam["amount"]!![0].toInt()

        val user = users[userId]
            ?: return Observable.just("This user doesn't exist.")
        val shareInfo = client.getShareInfo(companyId)
            ?: return Observable.just("This company is not in stock yet")
        if (shareInfo.price * amount > user.balance)
            return Observable.just("User doesn't have enough money for purchase")
        if (amount > shareInfo.amount)
            return Observable.just("Company doesn't have this amount of shares")
        client.buyShares(companyId, amount)
        user.balance -= shareInfo.price * amount
        user.shares[companyId] = (user.shares[companyId] ?: 0) + amount
        return Observable.just("ok")
    }

    fun sellShares(queryParam: Map<String, List<String>>): Observable<String> {
        val userId = queryParam["userId"]!![0]

        val companyId = queryParam["companyId"]!![0]

        val amount = queryParam["amount"]!![0].toInt()
        val user = users[userId]
            ?: return Observable.just("This user doesn't exist.")
        if (amount > user.shares[companyId] ?: 0)
            return Observable.just("User doesn't have this amount of shares")
        val price = client.getShareInfo(companyId)?.price ?: 0.0
        client.sellShares(companyId, amount)
        user.balance += price * amount
        user.shares[companyId] = (user.shares[companyId] ?: 0) - amount
        return Observable.just("ok")
    }
}
