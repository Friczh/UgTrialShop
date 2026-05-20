package com.hanami.app.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

// ── DataStore ────────────────────────────────────────────────────────────────

private val Context.dataStore by preferencesDataStore(name = "settings")
private val TOKEN_KEY = stringPreferencesKey("api_token")

class TokenStore(private val context: Context) {
    val tokenFlow: Flow<String> = context.dataStore.data.map { it[TOKEN_KEY] ?: "" }
    suspend fun saveToken(token: String) {
        context.dataStore.edit { it[TOKEN_KEY] = token }
    }
}

// ── Models ────────────────────────────────────────────────────────────────────

data class Product(
    val id: String,
    val name: String,
    val hcoin: Int,
    val tag: String,
    val iconUrl: String,
    val description: String,
    val inputPlaceholder: String = "",  // "" means no input needed
    val shopId: String = "k3m9zq1p0a"
)

val PRODUCT_LIST = listOf(
    Product("ID02", "Buff kim cương UGPhone",      70,     "Ugphone",      "https://i.postimg.cc/7YQFLHd3/ugphone.png",    "Lệnh buff kim cương mã mời của Ugphone.",                          "Nhập mã mời UGPhone"),
    Product("ID05", "BB Cloud trial",              60,     "BB Cloud",     "https://i.postimg.cc/fbGP1QvT/BBcloud.png",    "BB Cloud trial 2 giờ."),
    Product("ID07", "Google mail domain 12h",      180,    "Email",        "https://i.postimg.cc/Fs0H6hg0/google-mail.webp","Tài khoản Google domain edu sống 12 giờ."),
    Product("ID10", "OneTap Cloud code 15 ngày",   50000,  "Onetap Cloud", "https://i.postimg.cc/05Z3XQxJ/Onetapcloud.png","Mã kích hoạt onetap cloud dùng 15 ngày."),
    Product("ID12", "UGPhone Trial LocalStorage",  180,    "Ugphone",      "https://i.postimg.cc/7YQFLHd3/ugphone.png",    "Tài khoản Ugphone Trial 4 giờ sử dụng."),
    Product("ID13", "VMOS Cloud trial",            60,     "VMOS Cloud",   "https://i.postimg.cc/fy3gKPfb/vmos.jpg",       "Tài khoản Vmos Cloud trial."),
    Product("ID14", "Vsphone trial",               60,     "VSPhone",      "https://i.postimg.cc/63bmCKRS/vsphone.png",    "Tài khoản vsphone trial kích hoạt máy 4h."),
    Product("ID18", "Vipplayer SVIP code 1 ngày",  6200,   "vipplayer",    "https://i.postimg.cc/nrhL4q1P/vipplayer.png",  "Code kích hoạt thiết bị SVip vipplayer 1 ngày."),
    Product("ID19", "Vipplayer SVIP code 7 ngày",  37000,  "vipplayer",    "https://i.postimg.cc/nrhL4q1P/vipplayer.png",  "Code kích hoạt thiết bị SVip vipplayer 7 ngày."),
    Product("ID21", "Ugphone GVIP code 7 ngày",    55000,  "Ugphone",      "https://i.postimg.cc/7YQFLHd3/ugphone.png",    "Code dùng kích hoạt thiết bị Ugphone 7 ngày."),
    Product("ID23", "Vipplayer SVIP code 30 ngày", 135000, "vipplayer",    "https://i.postimg.cc/nrhL4q1P/vipplayer.png",  "Code kích hoạt thiết bị SVip vipplayer 30 ngày."),
    Product("ID25", "Vsphone KVIP code 1 ngày",    6000,   "VSPhone",      "https://i.postimg.cc/63bmCKRS/vsphone.png",    "Code kích hoạt vsphone kvip 1 ngày, 4GB RAM, 8 CORE."),
    Product("ID28", "Redfinger VIP code 7 ngày",   35000,  "Redfinger",    "https://i.postimg.cc/6pfBshKp/Redfinger.png",  "Code Redfinger VIP 4GB RAM, 8 CORE, 64GB, Android 10, 7 ngày."),
    Product("ID30", "Redfinger VIP code 30 ngày",  110000, "Redfinger",    "https://i.postimg.cc/6pfBshKp/Redfinger.png",  "Code Redfinger VIP 4GB RAM, 8 CORE, 64GB, Android 10, 30 ngày."),
    Product("ID31", "Onetap Cloud code 7 ngày",    35000,  "Onetap Cloud", "https://i.postimg.cc/05Z3XQxJ/Onetapcloud.png","Mã kích hoạt Onetap Cloud 7 ngày."),
    Product("ID33", "Vsphone KVIP code 7 ngày",    30000,  "VSPhone",      "https://i.postimg.cc/63bmCKRS/vsphone.png",    "Code kích hoạt vsphone kvip 7 ngày, 4GB RAM, 8 CORE."),
    Product("ID34", "Google Mail domain 10 phút",  140,    "Email",        "https://i.postimg.cc/Fs0H6hg0/google-mail.webp","Tài khoản Google domain edu sống 10 phút."),
)

// ── API Client ────────────────────────────────────────────────────────────────

sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val message: String) : ApiResult<Nothing>()
}

object HanamiApi {
    private const val BASE = "https://hanaminikata.com"
    private val client = OkHttpClient()
    private val JSON = "application/json; charset=utf-8".toMediaType()

    private fun post(path: String, body: JSONObject): JSONObject {
        val req = Request.Builder()
            .url("$BASE$path")
            .post(body.toString().toRequestBody(JSON))
            .build()
        val resp = client.newCall(req).execute()
        return JSONObject(resp.body!!.string())
    }

    private fun get(path: String): JSONObject {
        val req = Request.Builder().url("$BASE$path").build()
        val resp = client.newCall(req).execute()
        return JSONObject(resp.body!!.string())
    }

    // Buy Product — create order
    suspend fun createOrder(
        token: String,
        productId: String,
        quantity: Int,
        inputData: String?,
        shopId: String
    ): ApiResult<String> = runCatching {
        val body = JSONObject().apply {
            put("token", token)
            put("product_id", productId)
            put("quantity", quantity)
            put("shop_id", shopId)
            if (!inputData.isNullOrBlank()) put("input_data", JSONObject().put("0", inputData))
        }
        val resp = post("/dev/create_order", body)
        if (resp.getInt("code") == 0)
            ApiResult.Success(resp.getJSONObject("data").getString("order_id"))
        else
            ApiResult.Error(resp.getString("message"))
    }.getOrElse { ApiResult.Error(it.message ?: "Network error") }

    // Check order status
    suspend fun checkOrder(token: String, orderId: String): ApiResult<JSONObject> = runCatching {
        val resp = get("/dev/check_orderid?token=$token&order_id=$orderId")
        if (resp.getInt("code") == 0)
            ApiResult.Success(resp.getJSONObject("data"))
        else
            ApiResult.Error(resp.getString("message"))
    }.getOrElse { ApiResult.Error(it.message ?: "Network error") }

    // UGPhone status
    suspend fun checkUgphoneStatus(token: String): ApiResult<Map<String, Boolean>> = runCatching {
        val resp = get("/dev/check_status_ugphone?token=$token")
        if (resp.getInt("code") == 0) {
            val data = resp.getJSONObject("data")
            val map = mutableMapOf<String, Boolean>()
            data.keys().forEach { key -> map[key] = data.getBoolean(key) }
            ApiResult.Success(map as Map<String, Boolean>)
        } else {
            ApiResult.Error(resp.getString("message"))
        }
    }.getOrElse { ApiResult.Error(it.message ?: "Network error") }

    // Buy device cloud
    suspend fun buyDeviceCloud(
        token: String,
        cloudId: String,
        server: String?,
        inputData: String?
    ): ApiResult<String> = runCatching {
        val body = JSONObject().apply {
            put("token", token)
            put("cloud_id", cloudId)
            if (!server.isNullOrBlank()) put("server", server)
            if (!inputData.isNullOrBlank()) put("input_data", inputData)
        }
        val resp = post("/dev/buy_device_cloud", body)
        if (resp.getInt("code") == 0)
            ApiResult.Success(resp.getString("message"))
        else
            ApiResult.Error(resp.getString("message"))
    }.getOrElse { ApiResult.Error(it.message ?: "Network error") }
}
