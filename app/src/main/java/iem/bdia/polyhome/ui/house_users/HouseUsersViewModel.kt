package iem.bdia.polyhome.ui.house_users

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import iem.bdia.polyhome.domain.model.HouseUser
import iem.bdia.polyhome.domain.model.UnifiedUser
import iem.bdia.polyhome.domain.model.User
import iem.bdia.polyhome.domain.repository.HouseRepository
import iem.bdia.polyhome.domain.repository.UserRepository
import iem.bdia.polyhome.utils.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


class HouseUsersViewModel(
    private val userRepository: UserRepository,
    private val houseRepository: HouseRepository
) : ViewModel() {

    enum class FILTER { ALL, ACCESS, NOACCESS }

    private val _users = MutableStateFlow<List<User>>(emptyList())
    private val _houseUsers = MutableStateFlow<List<HouseUser>>(emptyList())
    private val _searchQuery = MutableStateFlow("")
    private val _filterState = MutableStateFlow(FILTER.ALL)

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    val unifiedUsers: StateFlow<List<UnifiedUser>> = combine(
        _users, _houseUsers, _searchQuery, _filterState
    ) { all, members, query, filter ->
        val memberMap = members.associateBy { it.userLogin }

        all.map { u ->
            val m = memberMap[u.login]
            UnifiedUser(u.login, m != null, m?.owner == 1, m, u)
        }.filter { user ->
            val matchesQuery = user.login.contains(query, ignoreCase = true)
            val matchesFilter = when (filter) {
                FILTER.ALL -> true
                FILTER.ACCESS -> user.isMember
                FILTER.NOACCESS -> !user.isMember
            }
            matchesQuery && matchesFilter
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateSearchQuery(q: String) {
        _searchQuery.value = q
    }


    fun refresh(token: String, houseId: String) {
        viewModelScope.launch {
            val r1 = userRepository.getUsers()
            val r2 = houseRepository.getHouseUsers(houseId, token)
            if (r1 is Result.Success) _users.value = r1.data
            if (r2 is Result.Success) _houseUsers.value = r2.data
            _error.value = (r1 as? Result.Error)?.message ?: (r2 as? Result.Error)?.message
        }
    }

    fun togglePermission(token: String, houseId: String, item: UnifiedUser) {
        viewModelScope.launch {
            val res = if (item.isMember) {
                item.houseUser?.let { houseRepository.removePermission(houseId, token, it) }
            } else {
                item.user?.let {
                    houseRepository.givePermission(
                        houseId,
                        token,
                        HouseUser(it.login, 0)
                    )
                }
            }
            if (res is Result.Success) {
                refresh(token, houseId)
            } else if (res is Result.Error) {
                _error.value = res.message
            }
        }
    }

    fun setFilter(filter: FILTER) {
        _filterState.value = filter
    }
}