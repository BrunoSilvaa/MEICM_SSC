import android.graphics.Bitmap
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

data class BboxResponse(
    val base64_image: String
)

interface LocalApiService {
    @Multipart
    @POST("/classify")
    fun classifyImage(
        @Part graph: MultipartBody.Part
    ): Call<ResponseBody>
}
