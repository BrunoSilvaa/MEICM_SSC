import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

data class BboxResponse(
    val bbox_predictions: List<List<Float>>,
    val class_predictions: List<List<Float>>
)

interface LocalApiService {
    @GET("/classify")
    fun classifyImage(
        @Query("graph") imageBase64: String
    ): Call<BboxResponse>
}
