package com.madproject.roombookingapp.data.remote

import com.madproject.roombookingapp.data.model.AuthResponse
import com.madproject.roombookingapp.data.model.AvailableRoomResponse
import com.madproject.roombookingapp.data.model.BookingRequest
import com.madproject.roombookingapp.data.model.BookingResponse
import com.madproject.roombookingapp.data.model.ChangeEmailRequest
import com.madproject.roombookingapp.data.model.ForgotPasswordRequest
import com.madproject.roombookingapp.data.model.LoginRequest
import com.madproject.roombookingapp.data.model.MessageResponse
import com.madproject.roombookingapp.data.model.PagedResponse
import com.madproject.roombookingapp.data.model.ProfilePictureResponse
import com.madproject.roombookingapp.data.model.ResetPasswordRequest
import com.madproject.roombookingapp.data.model.RoomResponse
import com.madproject.roombookingapp.data.model.RoomSearchRequest
import com.madproject.roombookingapp.data.model.SignupRequest
import com.madproject.roombookingapp.data.model.SignupResponse
import com.madproject.roombookingapp.data.model.UpdateProfileRequest
import com.madproject.roombookingapp.data.model.UserResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @POST("api/auth/signup")
    suspend fun signup(@Body request: SignupRequest): Response<SignupResponse>

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @GET("api/auth/verify-email")
    suspend fun verifyEmail(@Query("token") token: String): Response<MessageResponse>

    @POST("api/auth/resend-verification")
    suspend fun resendVerification(@Query("email") email: String): Response<MessageResponse>

    @POST("api/auth/refresh")
    suspend fun refreshToken(): Response<AuthResponse>

    @POST("api/auth/logout")
    suspend fun logout(): Response<MessageResponse>

    @POST("api/auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): Response<MessageResponse>

    @POST("api/auth/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): Response<MessageResponse>

    // Profile
    @GET("api/user/profile")
    suspend fun getProfile(): Response<UserResponse>

    @PUT("api/user/profile")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): Response<UserResponse>

    @POST("api/user/change-email")
    suspend fun requestEmailChange(@Body request: ChangeEmailRequest): Response<Map<String, String>>

    @GET("api/user/verify-email-change")
    suspend fun verifyEmailChange(@Query("token") token: String): Response<Map<String, String>>

    @DELETE("api/user/cancel-email-change")
    suspend fun cancelEmailChange(): Response<Map<String, String>>

    @FormUrlEncoded
    @POST("api/user/change-password")
    suspend fun changePassword(
        @Field("oldPassword") oldPassword: String,
        @Field("newPassword") newPassword: String
    ): Response<Map<String, String>>

    @Multipart
    @POST("api/user/profile-picture")
    suspend fun uploadProfilePicture(
        @Part file: MultipartBody.Part
    ): Response<ProfilePictureResponse>

    @DELETE("api/user/profile-picture")
    suspend fun deleteProfilePicture(): Response<Map<String, String>>

    // Bookings
    @POST("api/bookings")
    suspend fun createBooking(@Body request: BookingRequest): Response<BookingResponse>

    @GET("api/bookings/{id}")
    suspend fun getBookingById(@Path("id") id: Long): Response<BookingResponse>

    @GET("api/bookings/number/{bookingNumber}")
    suspend fun getBookingByNumber(@Path("bookingNumber") bookingNumber: String): Response<BookingResponse>

    @GET("api/bookings/my-bookings")
    suspend fun getMyBookings(
        @Query("page") page: Int? = null,
        @Query("size") size: Int? = null,
        @Query("sort") sort: String? = null
    ): Response<PagedResponse<BookingResponse>>

    @GET("api/bookings")
    suspend fun getAllBookings(
        @Query("page") page: Int? = null,
        @Query("size") size: Int? = null,
        @Query("sort") sort: String? = null
    ): Response<PagedResponse<BookingResponse>>

    @DELETE("api/bookings/{id}")
    suspend fun cancelBooking(@Path("id") id: Long): Response<Map<String, String>>

    // Rooms
    @GET("api/rooms/{id}")
    suspend fun getRoomById(@Path("id") id: Long): Response<RoomResponse>

    @GET("api/rooms/number/{roomNumber}")
    suspend fun getRoomByNumber(@Path("roomNumber") roomNumber: String): Response<RoomResponse>

    @GET("api/rooms")
    suspend fun getAllRooms(
        @Query("page") page: Int? = null,
        @Query("size") size: Int? = null,
        @Query("sort") sort: String? = null
    ): Response<PagedResponse<RoomResponse>>

    @POST("api/rooms/search")
    suspend fun searchAvailableRooms(@Body request: RoomSearchRequest): Response<List<AvailableRoomResponse>>

    @GET("api/rooms/{id}/availability")
    suspend fun checkRoomAvailability(
        @Path("id") id: Long,
        @Query("checkInDate") checkInDate: String,
        @Query("checkOutDate") checkOutDate: String
    ): Response<Map<String, Boolean>>
}