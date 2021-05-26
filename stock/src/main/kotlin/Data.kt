data class Share(var amount: Int, var price: Double) {
    override fun toString(): String = "Share{amount=$amount, price=$price}"
}

class Company(val id: String, price: Double, amount: Int) {
    val share: Share = Share(amount, price)
    override fun toString(): String = "Company{id=$id, share=$share}"
}