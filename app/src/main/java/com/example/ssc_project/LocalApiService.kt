import android.graphics.Bitmap
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

data class BboxResponse(
    val bbox_predictions: List<List<Float>>,
    val class_predictions: List<List<Float>>
)

interface LocalApiService {
    @Multipart
    @POST("/classify")
    fun classifyImage(
        @Part graph: MultipartBody.Part
    ): Call<BboxResponse>
}
