package iem.bdia.polyhome.ui.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import iem.bdia.polyhome.R
import iem.bdia.polyhome.data.local.TokenStorage
import iem.bdia.polyhome.data.remote.api.RetrofitInstance
import iem.bdia.polyhome.data.remote.repository.HouseRepoImpl
import iem.bdia.polyhome.ui.devices.DevicesActivity
import iem.bdia.polyhome.ui.house_users.HouseUsersActivity
import iem.bdia.polyhome.ui.login.LoginActivity
import kotlinx.coroutines.launch

class HomeActivity : AppCompatActivity() {

    private lateinit var viewModel: HomeViewModel
    private lateinit var houseAdapter: HouseAdapter
    private lateinit var tokenStorage: TokenStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        tokenStorage = TokenStorage(this)

        val repo = HouseRepoImpl(RetrofitInstance.apiService)
        viewModel = HomeViewModel(repo)

        setupListView()
        observeViewModel()

        lifecycleScope.launch {
            val token = tokenStorage.getToken()
            Log.d("HomeActivity", "Token: $token")
            if (token != null) {
                viewModel.loadHouses(token)
            } else {
                Toast.makeText(this@HomeActivity, "Veuillez vous connecter", Toast.LENGTH_SHORT)
                    .show()
                finish()
            }
        }
    }

    private fun setupListView() {
        val listView = findViewById<ListView>(R.id.housesListView)
        houseAdapter = HouseAdapter(
            context = this,
            onDevicesClick = { house ->
                val intent = Intent(this, DevicesActivity::class.java)
                intent.putExtra("houseId", house.houseId.toString())
                startActivity(intent)
            },
            onUsersClick = { house ->
                val intent = Intent(this, HouseUsersActivity::class.java)
                intent.putExtra("houseId", house.houseId.toString())
                startActivity(intent)
            }
        )
        listView.adapter = houseAdapter
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.mainHouse.collect { mainHouse ->
                        val tvMyHouseTitle = findViewById<TextView>(R.id.tvMyHouseTitle)
                        val btnManageDevices = findViewById<Button>(R.id.btnManageDevices)
                        val btnManageAccess = findViewById<Button>(R.id.btnManageAccess)
                        val btnLogout = findViewById<Button>(R.id.btnLogout)

                        if (mainHouse != null) {
                            tvMyHouseTitle.text = "Maison #${mainHouse.houseId}"

                            btnManageAccess.visibility = if (mainHouse.owner) View.VISIBLE else View.GONE


                            btnManageDevices.setOnClickListener {
                                val intent = Intent(this@HomeActivity, DevicesActivity::class.java)
                                intent.putExtra("houseId", mainHouse.houseId.toString())
                                startActivity(intent)
                            }

                            btnManageAccess.setOnClickListener {
                                val intent = Intent(this@HomeActivity, HouseUsersActivity::class.java)
                                intent.putExtra("houseId", mainHouse.houseId.toString())
                                startActivity(intent)
                            }

                            btnLogout.setOnClickListener {
                                lifecycleScope.launch {
                                    tokenStorage.clearToken()
                                    startActivity(Intent(this@HomeActivity,LoginActivity::class.java))
                                }
                            }

                        } else {
                            tvMyHouseTitle.text = "Aucune maison"
                        }
                    }
                }

                launch {
                    viewModel.sharedHouses.collect { houses ->
                        houseAdapter.submitList(houses)
                    }
                }

                launch {
                    viewModel.error.collect { error ->
                        if (error != null) {
                            Toast.makeText(this@HomeActivity, error, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }
}