package iem.bdia.polyhome.domain.model

data class UnifiedUser(
    val login: String,
    val isMember: Boolean,
    val isOwner: Boolean = false,
    val houseUser: HouseUser? = null,
    val user: User? = null
)