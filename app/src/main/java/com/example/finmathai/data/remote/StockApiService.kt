import com.example.finmathai.data.remote.YahooResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface StockApiService {
    // Yahoo'nun herkese açık veri sağladığı adres yapısı
    @GET("v8/finance/chart/{symbol}")
    suspend fun getStockData(
        @Path("symbol") symbol: String
    ): YahooResponse
}