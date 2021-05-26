data class User(var balance: Double, val shares: MutableMap<String, Int> = mutableMapOf())
data class Share(val amount: Int, val price: Double) {
    override fun toString(): String = "Share{amount=$amount, price=$price}"
}