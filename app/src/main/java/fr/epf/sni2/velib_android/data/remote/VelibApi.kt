package fr.epf.sni2.velib_android.data.remote

import fr.epf.sni2.velib_android.data.remote.dto.GbfsResponse
import fr.epf.sni2.velib_android.data.remote.dto.StationInformationDto
import fr.epf.sni2.velib_android.data.remote.dto.StationStatusDto
import fr.epf.sni2.velib_android.data.remote.dto.StationsPayload
import retrofit2.http.GET

interface VelibApi {

    @GET("station_information.json")
    suspend fun getStationInformation(): GbfsResponse<StationsPayload<StationInformationDto>>

    @GET("station_status.json")
    suspend fun getStationStatus(): GbfsResponse<StationsPayload<StationStatusDto>>

    companion object {
        const val BASE_URL = "https://velib-metropole-opendata.smovengo.cloud/opendata/Velib_Metropole/"
    }
}
