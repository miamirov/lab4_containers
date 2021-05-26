import io.netty.buffer.ByteBuf
import io.netty.handler.codec.http.HttpResponseStatus
import io.reactivex.netty.protocol.http.server.HttpServer
import io.reactivex.netty.protocol.http.server.HttpServerRequest
import io.reactivex.netty.protocol.http.server.HttpServerResponse
import rx.Observable
import rx.Observable.just
import java.util.Random

class StockServer {
    private val companies: MutableMap<String, Company> = mutableMapOf()
    private val random = Random()

    fun run() = HttpServer.newServer(8080)
        .start { req: HttpServerRequest<ByteBuf?>, resp: HttpServerResponse<ByteBuf?> ->
            val response: Observable<String>
            when (req.decodedPath.substring(1)) {
                "addCompany" -> {
                    response = addCompany(req.queryParameters)
                    resp.setStatus(HttpResponseStatus.OK)
                }
                "getSharesInfo" -> {
                    response = getSharesInfo(req.queryParameters)
                    resp.setStatus(HttpResponseStatus.OK)
                }
                "buyShares" -> {
                    response = buyShares(req.queryParameters)
                    resp.setStatus(HttpResponseStatus.OK)
                }
                "sellShares" -> {
                    response = sellShares(req.queryParameters)
                    resp.setStatus(HttpResponseStatus.OK)
                }
                "changePrice" -> {
                    response = changePrice(req.queryParameters)
                    resp.setStatus(HttpResponseStatus.OK)
                }
                else -> {
                    response = just("Wrong command")
                    resp.setStatus(HttpResponseStatus.BAD_REQUEST)
                }
            }
            resp.writeString(response)
        }.awaitShutdown()

    private fun addCompany(queryParam: Map<String, List<String>>): Observable<String> {
        val id = queryParam["id"]!![0]
        val price = queryParam["price"]!![0].toDouble()
        val amount = queryParam["amount"]!!.get(0).toInt()
        if (companies[id] != null)
            return just("This company is already in stock.")
        companies[id] = Company(id, price, amount)
        return just("ok")
    }

    private fun getSharesInfo(queryParam: Map<String, List<String>>): Observable<String> {
        val id = queryParam["id"]!![0]
        val company = companies[id]
            ?: return just("This company is not in stock yet.")
        return just(company.share.toString())
    }

    private fun buyShares(queryParam: Map<String, List<String>>): Observable<String> {
        val id = queryParam["id"]!![0]
        val amount = queryParam["amount"]!![0].toInt()
        val company = companies[id] ?: return just("This company is not in stock yet.")
        if (company.share.amount < amount)
            return just("There are only ${company.share.amount} shares on the stock")
        company.share.amount -= amount
        return just("ok")
    }

    private fun sellShares(queryParam: Map<String, List<String>>): Observable<String> {
        val id = queryParam["id"]!!.get(0)
        val amount = queryParam["amount"]!![0].toInt()
        val company = companies[id]
            ?: return just("This company is not in stock yet.")
        company.share.amount += amount
        return just("ok")
    }

    private fun changePrice(queryParam: Map<String, List<String>>): Observable<String> {
        val id = queryParam["id"]!![0]
        val company = companies[id]
            ?: return just("This company is not in stock yet.")
        val delta = random.nextGaussian() * company.share.price
        company.share.price += delta
        return just("ok")
    }
}
