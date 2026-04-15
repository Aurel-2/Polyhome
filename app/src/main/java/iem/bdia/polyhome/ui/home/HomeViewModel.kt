package iem.bdia.polyhome.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import iem.bdia.polyhome.domain.model.House
import iem.bdia.polyhome.domain.repository.HouseRepository
import iem.bdia.polyhome.utils.Result
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(private val houseRepository: HouseRepository) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _mainHouse = MutableStateFlow<House?>(null)
    val mainHouse: StateFlow<House?> = _mainHouse.asStateFlow()

    private val _sharedHouses = MutableStateFlow<List<House>>(emptyList())
    val sharedHouses: StateFlow<List<House>> = _sharedHouses.asStateFlow()

    fun loadHouses(token: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            when (val result = houseRepository.getHouses(token)) {
                is Result.Success -> {
                    val allHouses = result.data

                    val ownerHouse = allHouses.find { it.owner }

                    if (ownerHouse != null) {
                        _mainHouse.value = ownerHouse
                        _sharedHouses.value = allHouses.filter {
                            it.houseId != ownerHouse.houseId
                        }
                    } else {
                        _mainHouse.value = allHouses.firstOrNull()
                        _sharedHouses.value = if (allHouses.isNotEmpty()) {
                            allHouses.drop(1)
                        } else {
                            emptyList()
                        }
                    }
                }

                is Result.Error -> {
                    _error.value = result.message
                }
            }

            _isLoading.value = false
        }
    }
}