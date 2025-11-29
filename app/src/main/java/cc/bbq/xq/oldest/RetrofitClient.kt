package cc.bbq.xq.oldest

import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

object RetrofitClient {
    private const val BASE_URL = "http://apk.xiaoqu.online/"
  
    val instance: ApiService by lazy {
        val client = OkHttpClient.Builder().build()
      
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
  
    // 模型类定义
    object models {
        // 基础响应模型（新增）
        data class BaseResponse(
            val code: Int,
            val msg: String,
            val data: Any? = null,
            val timestamp: Long
        )

        // 帖子详情响应模型
        data class PostDetailResponse(
            val code: Int,
            val msg: String,
            val data: PostDetail,
            val timestamp: Long
        )

        data class PostDetail(
            val id: Long,
            val title: String,
            val content: String,            
            val userid: Long,
            val create_time: String,
            val update_time: String,
            val username: String,
            val nickname: String,
            val usertx: String,
            val hierarchy: String,
            val section_name: String,
            val sub_section_name: String,
            val view: String,
            val thumbs: String,
            val comment: String,
            val img_url: List<String>? = null,
            val video_url: String? = null,
            val ip_address: String,
            val create_time_ago: String,
            val is_thumbs: Int, // 是否点赞 (0/1)
            val is_collection: Int // 是否收藏 (0/1)
        )

        // 评论列表响应模型
        data class CommentListResponse(
            val code: Int,
            val msg: String,
            val data: CommentListData,
            val timestamp: Long
        )

        data class CommentListData(
            val list: List<Comment>,
            val pagecount: Int,
            val current_number: Int
        )

        data class Comment(
            val id: Long,
            val content: String,
            val userid: Long,
            val time: String,
            val username: String,
            val nickname: String,
            val usertx: String,
            val hierarchy: String,
            val parentid: Long? = null,
            val parentnickname: String? = null,
            val parentcontent: String? = null,
            val image_path: List<String>? = null,
            val sub_comments_count: Int,
            val time_ago: String
        )
      
        // 帖子列表响应模型
        data class PostListResponse(
            val code: Int,
            val msg: String,
            val data: PostListData,
            val timestamp: Long
        )

        data class PostListData(
            val list: List<Post>,
            val pagecount: Int,
            val current_number: Int
        )

        data class Post(
            val postid: Long,
            val title: String,
            val content: String,
            val userid: Long,
            val create_time: String,
            val update_time: String,
            val username: String,
            val nickname: String,
            val usertx: String,
            val hierarchy: String,
            val section_name: String,
            val sub_section_name: String,
            val view: String,
            val thumbs: String,
            val comment: String,
            val img_url: List<String>? = null
        )
      
        data class LoginResponse(
            val code: Int,
            val msg: String,
            val data: LoginData,
            val timestamp: Long
        )

        data class LoginData(
            val usertoken: String,
            val id: Long,
            val username: String,
            val nickname: String
        )
      
        data class HeartbeatResponse(
            val code: Int,
            val msg: String,
            val timestamp: Long
        )
      
        data class UserInfoResponse(
            val code: Int,
            val msg: String,
            val data: UserData,
            val timestamp: Long
        )

        data class UserData(
            val id: Long,
            val username: String,
            val usertx: String,
            val nickname: String,
            val hierarchy: String,
            val money: Int,
            val followerscount: String,
            val fanscount: String,
            val postcount: String,
            val likecount: String
        )

        // ======== 新增的应用列表模型 ========
        data class AppListResponse(
            val code: Int,
            val msg: String,
            val data: AppListData,
            val timestamp: Long
        )

        data class AppListData(
            val list: List<AppItem>,
            val pagecount: Int,
            val current_number: Int
        )

        data class AppItem(
            val id: Long,
            val appname: String,
            val app_icon: String,
            val app_size: String,
            val download_count: Int,
            val create_time: String,
            val nickname: String,
            val apps_version_id: Long // 添加缺失的版本ID字段
        )
        
        // 应用详情响应模型
        data class AppDetailResponse(
            val code: Int,
            val msg: String,
            val data: AppDetail,
            val timestamp: Long
        )
        
        // 在 RetrofitClient.models 中添加以下数据类

// 消息通知响应
        data class MessageNotificationResponse(
            val code: Int,
            val msg: String,
            val data: MessageNotificationData,
            val timestamp: Long
        )

        data class MessageNotificationData(
            val list: List<MessageNotification>,
            val pagecount: Int,
            val current_number: Int
        )        

        data class MessageNotification(
            val id: Long,
            val title: String,
            val content: String,
            val send_to: Long, // 接收者用户ID
            val appid: Int,
            val type: Int,     // 消息类型（3:点赞，5:评论）        
            val time: String,   // 消息时间                
            val postid: Long?,  // 关联的帖子ID（可能为空）
            val pic_url: String?, // 图片URL
            val user_id: Long,  // 发送者用户ID
            val status: Int,    // 状态
            val is_admin: Int   // 是否是管理员
        )

        data class AppDetail(
            val id: Long,
            val appname: String,
            val app_icon: String,
            val app_size: String,
            val app_explain: String?,
            val app_introduce: String?,
            val app_introduction_image: String?,
            val is_pay: Int,
            val pay_money: Int,
            val download: String?,
            val create_time: String,
            val update_time: String,
            val userid: Long,
            val appid: Int,
            val category_id: Int,
            val sub_category_id: Int,
            val category_name: String,
            val category_icon: String,
            val sub_category_name: String,
            val username: String,
            val nickname: String,
            val usertx: String,
            val sex: Int,
            val signature: String,
            val exp: Int,
            val version: String,
            val apps_version_id: Long,
            val version_create_time: String,
            val app_introduction_image_array: List<String>?,
            val ip_address: String,
            val sexName: String,
            val badge: List<Any>,
            val vip: Boolean,
            val hierarchy: String,
            val is_user_pay: Boolean,
            val download_count: Int,
            val comment_count: Int,
            val user_pay_count: Int,
            val reward_count: Int,
            val posturl: String?
        )
        
        data class BrowseHistoryResponse(
            val code: Int,
            val msg: String,
            val data: BrowseHistoryData,
            val timestamp: Long
        )

        data class BrowseHistoryData(
            val list: List<BrowseHistoryItem>,
            val pagecount: Int,
            val current_number: Int
        )

        data class BrowseHistoryItem(
            val postid: Long,
            val title: String,
            val content: String,
            val userid: Long,
            val create_time: String,
            val update_time: String,
            val username: String,
            val nickname: String,
            val usertx: String,
            val hierarchy: String,
            val section_name: String,
            val sub_section_name: String,
            val view: String,
            val thumbs: String,
            val comment: String,
            val img_url: List<String>? = null      
        )
        
        // 应用评论列表响应模型
        data class AppCommentListResponse(
            val code: Int,
            val msg: String,
            val data: AppCommentListData,
            val timestamp: Long
        )

        data class AppCommentListData(
            val list: List<AppComment>,
            val pagecount: Int,
            val current_number: Int
        )

        data class AppComment(
            val id: Long,
            val content: String,
            val userid: Long,
            val time: String,
            val username: String,        
            val nickname: String,
            val usertx: String,
            val hierarchy: String,
            val parentid: Long? = null,
            val parentnickname: String? = null,
            val parentcontent: String? = null,
            val image_path: List<String>? = null,
            val time_ago: String
        )
        
        // 用户列表响应模型
        data class UserListResponse(
            val code: Int,
            val msg: String,
            val data: UserListData,
            val timestamp: Long
        )

        data class UserListData(
            val list: List<UserItem>,
            val pagecount: Int,
            val current_number: Int
        )

        data class UserItem(
            val id: Long,
            val username: String,
            val nickname: String,
            val usertx: String,
            val sex: Int,
            val signature: String,
            val title: List<String>,
            val badge: List<Any>,
            val vip: Boolean,
            val hierarchy: String,
            val status: Int,
            val sexName: String
        )
        
        // Add to RetrofitClient.kt in models object
        data class UserInformationResponse(
            val code: Int,
            val msg: String,
            val data: UserInformationData,
            val timestamp: Long
        )

        data class UserInformationData(
            val id: Long,
            val username: String,
            val usertx: String,
            val nickname: String,
            val money: Int,
            val exp: Int,                        
            val followerscount: String,
            val fanscount: String,
            val postcount: String,
            val likecount: String,
            val hierarchy: String
        )
    }        

    interface ApiService {
        // 登录接口
        @POST("api/login")
        @FormUrlEncoded
        suspend fun login(
            @Field("appid") appid: Int = 1,
            @Field("username") username: String,
            @Field("password") password: String,
            @Field("device") device: String
        ): Response<models.LoginResponse>
      
        // 心跳接口
        @POST("api/user_heartbeat")
        @FormUrlEncoded
        suspend fun heartbeat(
            @Field("appid") appid: Int = 1,
            @Field("usertoken") token: String
        ): Response<models.HeartbeatResponse>
      
        // 帖子列表接口
        @POST("api/get_posts_list")
        @FormUrlEncoded
        suspend fun getPostsList(
            @Field("appid") appid: Int = 1,
            @Field("limit") limit: Int,
            @Field("page") page: Int,
            @Field("sort") sort: String = "create_time",
            @Field("sortOrder") sortOrder: String = "desc",
            @Field("sectionid") sectionId: Int? = null,
            @Field("userid") userId: Long? = null  // 新增userId参数
        ): Response<models.PostListResponse>
      
        // 用户信息接口
        @POST("api/get_user_other_information")
        @FormUrlEncoded
        suspend fun getUserInfo(
            @Field("appid") appid: Int = 1,
            @Field("usertoken") token: String
        ): Response<models.UserInfoResponse>
      
        // 帖子详情接口
        @POST("api/get_post_information")
        @FormUrlEncoded
        suspend fun getPostDetail(
            @Field("appid") appid: Int = 1,
            @Field("usertoken") token: String,
            @Field("postid") postId: Long
        ): Response<models.PostDetailResponse>
      
        // 评论列表接口
        @POST("api/get_list_comments")
        @FormUrlEncoded
        suspend fun getPostComments(
            @Field("appid") appid: Int = 1,
            @Field("postid") postId: Long,
            @Field("limit") limit: Int,
            @Field("page") page: Int,
            @Field("sort") sort: String = "time",
            @Field("sortOrder") sortOrder: String = "desc"
        ): Response<models.CommentListResponse>
      
        // 创建帖子接口（使用新BaseResponse）
        @POST("api/post")
        @FormUrlEncoded
        suspend fun createPost(
            @Field("appid") appid: Int = 1,
            @Field("usertoken") token: String,
            @Field("title") title: String,
            @Field("content") content: String,
            @Field("subsectionid") sectionId: Int,
            @Field("network_picture") imageUrls: String? = null,
            @Field("paid_reading") paidReading: Int = 0,
            @Field("file_download_method") downloadMethod: Int = 0
        ): Response<models.BaseResponse>
      
        // ======== 新增的应用列表接口 ========
        @POST("api/get_apps_list")
        @FormUrlEncoded
        suspend fun getAppsList(
            @Field("appid") appid: Int = 1,
            @Field("limit") limit: Int,
            @Field("page") page: Int,
            @Field("sort") sort: String = "update_time",
            @Field("sortOrder") sortOrder: String = "desc",
            @Field("category_id") categoryId: Int? = null,
            @Field("appname") appName: String? = null
        ): Response<models.AppListResponse>
        
        // 应用详情接口
        @POST("api/get_apps_information")
        @FormUrlEncoded
        suspend fun getAppsInformation(
            @Field("appid") appid: Int = 1,
            @Field("usertoken") token: String,
            @Field("apps_id") appsId: Long,
            @Field("apps_version_id") appsVersionId: Long
        ): Response<models.AppDetailResponse>

        // 应用评论接口
        @POST("api/get_apps_comment_list")
        @FormUrlEncoded
        suspend fun getAppsCommentList(
            @Field("appid") appid: Int = 1,
            @Field("apps_id") appsId: Long,
            @Field("apps_version_id") appsVersionId: Long,
            @Field("limit") limit: Int,
            @Field("page") page: Int,
            @Field("sortOrder") sortOrder: String = "asc"
        ): Response<models.AppCommentListResponse>
        
        @POST("api/post_comment")
        @FormUrlEncoded
        suspend fun postComment(
            @Field("appid") appid: Int = 1,
            @Field("usertoken") token: String,
            @Field("content") content: String,
            @Field("postid") postId: Long? = null,
            @Field("parentid") parentId: Long? = null,
            @Field("img") imageUrl: String? = null  // 添加图片参数
        ): Response<models.BaseResponse>
        
        // 在 RetrofitClient.ApiService 中添加
        @POST("api/get_message_notifications")
        @FormUrlEncoded
        suspend fun getMessageNotifications(
            @Field("appid") appid: Int = 1,
            @Field("usertoken") token: String,
            @Field("limit") limit: Int,
            @Field("page") page: Int
        ): Response<models.MessageNotificationResponse>
        
        // 在RetrofitClient.ApiService中添加接口方法
        @POST("api/browse_history")
        @FormUrlEncoded
        suspend fun getBrowseHistory(
            @Field("appid") appid: Int = 1,
            @Field("usertoken") token: String,
            @Field("limit") limit: Int = 10,
            @Field("page") page: Int
        ): Response<RetrofitClient.models.BrowseHistoryResponse>
        
        @POST("api/get_likes_records")
        @FormUrlEncoded
        suspend fun getLikesRecords(
            @Field("appid") appid: Int = 1,
            @Field("usertoken") token: String,
            @Field("limit") limit: Int = 10,
            @Field("page") page: Int
        ): Response<RetrofitClient.models.PostListResponse>
        
        @POST("api/get_posts_list")
        @FormUrlEncoded
        suspend fun searchPosts(
            @Field("appid") appid: Int = 1,
            @Field("keyword") query: String,
            @Field("limit") limit: Int = 10,
            @Field("page") page: Int
        ): Response<RetrofitClient.models.PostListResponse>
        
        // 在ApiService接口中添加
        @POST("api/like_posts")
        @FormUrlEncoded
        suspend fun likePost(
            @Field("appid") appid: Int = 1,
            @Field("usertoken") token: String,        
            @Field("postid") postId: Long
        ): Response<models.BaseResponse>

        @POST("api/delete_post")
        @FormUrlEncoded
        suspend fun deletePost(
            @Field("appid") appid: Int = 1,
            @Field("usertoken") token: String,           
            @Field("postid") postId: Long
        ): Response<models.BaseResponse>
        
        // 获取粉丝列表
        @POST("api/get_fan_list")
        @FormUrlEncoded
        suspend fun getFanList(
            @Field("appid") appid: Int = 1,
            @Field("usertoken") token: String,
            @Field("limit") limit: Int = 10,
            @Field("page") page: Int
        ): Response<models.UserListResponse>

    // 获取关注列表
        @POST("api/get_follow_list")
        @FormUrlEncoded
        suspend fun getFollowList(
            @Field("appid") appid: Int = 1,
            @Field("usertoken") token: String,
            @Field("limit") limit: Int = 10,
            @Field("page") page: Int
        ): Response<models.UserListResponse>
        
        @POST("api/get_user_information")
        @FormUrlEncoded
        suspend fun getUserInformation(
            @Field("appid") appid: Int = 1,
            @Field("userid") userId: Long,
            @Field("usertoken") token: String
        ): Response<RetrofitClient.models.UserInformationResponse>

        
    }
}