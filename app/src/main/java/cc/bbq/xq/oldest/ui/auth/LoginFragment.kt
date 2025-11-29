package cc.bbq.xq.oldest.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import cc.bbq.xq.oldest.AuthManager
import cc.bbq.xq.oldest.MainActivity
import cc.bbq.xq.oldest.R
import cc.bbq.xq.oldest.RetrofitClient
import cc.bbq.xq.oldest.databinding.FragmentLoginBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        binding.btnLogin.setOnClickListener {
            val username = binding.etUsername.text.toString()
            val password = binding.etPassword.text.toString()
            
            if (username.isBlank() || password.isBlank()) {
                binding.tilUsername.error = "请输入用户名"
                binding.tilPassword.error = "请输入密码"
                return@setOnClickListener
            }
            
            binding.progressBar.visibility = View.VISIBLE
            binding.btnLogin.isEnabled = false
            
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val deviceId = AuthManager.getDeviceId(requireContext())
                    val response = RetrofitClient.instance.login(
                        username = username,
                        password = password,
                        device = deviceId
                    )
                    
                    withContext(Dispatchers.Main) {
                        binding.progressBar.visibility = View.GONE
                        binding.btnLogin.isEnabled = true
                        
                        if (response.isSuccessful) {
                            val body = response.body()
                            if (body != null) {
                                when (body.code) {
                                    1 -> {
                                        // 登录成功
                                        val loginData = body.data
                                        if (loginData != null) {
                                            // 保存凭证并导航
                                            AuthManager.saveCredentials(
                                                requireContext(),
                                                username,
                                                password,
                                                loginData.usertoken,
                                                loginData.id // 添加第五个参数：userid
                                            )
                                            (activity as? MainActivity)?.startHeartbeatService(loginData.usertoken)
                                            findNavController().popBackStack()
                                        } else {
                                            // 登录数据解析失败
                                            Snackbar.make(
                                                binding.root, 
                                                "登录成功但数据解析错误", 
                                                Snackbar.LENGTH_LONG
                                            ).show()
                                        }
                                    }
                                    0 -> {
                                        // 登录失败时直接显示服务器消息
                                        Snackbar.make(binding.root, body.msg, Snackbar.LENGTH_LONG).show()
                                    }
                                    else -> {
                                        // 其他未知状态码
                                        Snackbar.make(
                                            binding.root, 
                                            "未知响应: ${body.code} - ${body.msg}", 
                                            Snackbar.LENGTH_LONG
                                        ).show()
                                    }
                                }
                            } else {
                                // 响应体为空
                                Snackbar.make(binding.root, "服务器未返回数据", Snackbar.LENGTH_LONG).show()
                            }
                        } else {
                            // HTTP错误状态处理
                            val errorMsg = when (response.code()) {
                                429 -> "请求太频繁，请稍后再试"
                                500 -> "服务器内部错误"
                                else -> "网络错误: ${response.code()}"
                            }
                            Snackbar.make(binding.root, errorMsg, Snackbar.LENGTH_LONG).show()
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        binding.progressBar.visibility = View.GONE
                        binding.btnLogin.isEnabled = true
                        // 显示详细的异常信息
                        Snackbar.make(
                            binding.root, 
                            "网络错误: ${e.message}",
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
        
        binding.btnRegister.setOnClickListener {
            Snackbar.make(binding.root, "注册功能开发中", Snackbar.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}