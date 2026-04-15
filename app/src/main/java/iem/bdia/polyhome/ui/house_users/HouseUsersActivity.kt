package iem.bdia.polyhome.ui.house_users

import android.os.Bundle
import android.view.View
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.chip.ChipGroup
import iem.bdia.polyhome.R
import iem.bdia.polyhome.data.local.TokenStorage
import iem.bdia.polyhome.data.remote.api.RetrofitInstance
import iem.bdia.polyhome.data.remote.repository.HouseRepoImpl
import iem.bdia.polyhome.data.remote.repository.UserRepoImpl
import kotlinx.coroutines.launch

class HouseUsersActivity : AppCompatActivity() {
    private lateinit var viewModel: HouseUsersViewModel
    private lateinit var tokenStorage: TokenStorage
    private lateinit var adapter: HouseUsersAdapter
    private var houseId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_house_users)

        tokenStorage = TokenStorage(this)
        houseId = intent.getStringExtra("houseId") ?: ""

        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }

        val listView: ListView = findViewById(R.id.usersListView)
        val tvEmpty: TextView = findViewById(R.id.tvEmptyMessage)
        listView.emptyView = tvEmpty

        viewModel = HouseUsersViewModel(
            UserRepoImpl(RetrofitInstance.apiService),
            HouseRepoImpl(RetrofitInstance.apiService)
        )
        adapter = HouseUsersAdapter(this) { item ->
            lifecycleScope.launch {
                tokenStorage.getToken()?.let {
                    viewModel.togglePermission(it, houseId, item)
                }
            }
        }
        listView.adapter = adapter

        findViewById<ChipGroup>(R.id.chipGroupFilters).setOnCheckedStateChangeListener { _, checkedIds ->
            val filter = when (checkedIds.firstOrNull()) {
                R.id.chipLights -> HouseUsersViewModel.FILTER.ACCESS
                R.id.chipShutters -> HouseUsersViewModel.FILTER.NOACCESS
                else -> HouseUsersViewModel.FILTER.ALL
            }
            viewModel.setFilter(filter)
        }

        (findViewById<View>(R.id.searchView) as SearchView).setOnQueryTextListener(object :
            SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(q: String?) = false
            override fun onQueryTextChange(q: String?): Boolean {
                viewModel.updateSearchQuery(q ?: "")
                return true
            }
        })

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.unifiedUsers.collect {
                        adapter.submitList(it)
                    }
                }
                launch {
                    viewModel.error.collect {
                        it?.let {
                            Toast.makeText(this@HouseUsersActivity, it, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }

        lifecycleScope.launch { tokenStorage.getToken()?.let { viewModel.refresh(it, houseId) } }
    }
}