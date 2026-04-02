package org.nikol.roasti.ui.features.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.nikol.roasti.feature.auth.domain.model.User
import org.nikol.roasti.feature.auth.domain.repository.AuthRepository
import org.nikol.roasti.feature.likes.domain.LikesRepository
import org.nikol.roasti.feature.recipe.data.paging.PagingRecipeRepository
import org.nikol.roasti.ui.features.recipelist.mapper.toUiModel
import org.nikol.roasti.utils.stateInWhileSubscribe

private const val SessionExpiredMessage = "Your session has ended. Please sign in again."

sealed interface ProfileUiState {
    data object Loading : ProfileUiState
    data class Error(val message: String) : ProfileUiState
    data class Content(
        val user: User,
        val isRefreshing: Boolean,
        val isLoggingOut: Boolean,
    ) : ProfileUiState
}

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModel(
    private val authRepository: AuthRepository,
    private val pagingRecipeRepository: PagingRecipeRepository,
    private val likesRepository: LikesRepository,
) : ViewModel(), ProfileRowListener {

    private val isRefreshing = MutableStateFlow(false)
    private val isLoggingOut = MutableStateFlow(false)

    private val userStatisticsState: StateFlow<ProfileStatisticsUiModel> =
        MutableStateFlow(ProfileStatisticsUiModel.empty()).asStateFlow()

    private val userState = authRepository.getUser()
        .filterNotNull()
        .map { it.toUi() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProfileUserUiModel.empty())

    private val favoritesState = authRepository.getUser().flatMapLatest { user ->
        favoriteRecipesFlow(user?.id)
    }
        .stateInWhileSubscribe(ProfileFavoritesBlock.Loading)

    private fun favoriteRecipesFlow(userId: String?) = flow {
        if (userId == null) {
            emit(ProfileFavoritesBlock.Empty)
            return@flow
        }

        val itemsLimit = 20
        val maxVisibleLimit = itemsLimit - 1
        val result = likesRepository.getLikedRecipes(userId = userId, limit = itemsLimit, page = 1)

        val likes = result.getOrNull()
        if (!likes?.items.isNullOrEmpty()) {
            emit(
                ProfileFavoritesBlock.Content(
                    items = likes.items.map { it.recipe.toUiModel() }.take(maxVisibleLimit),
                    showMoreBlock = likes.items.size > maxVisibleLimit
                )
            )
        } else {
            emit(ProfileFavoritesBlock.Empty)
        }
    }

    val state: StateFlow<ProfileState> =
        combine(userStatisticsState, userState, favoritesState) { statistics, user, favorites ->
            ProfileState(user, statistics, favorites)
        }.stateInWhileSubscribe(ProfileState.empty())


    private fun logout() {
        if (isLoggingOut.value) return
        viewModelScope.launch {
            isLoggingOut.value = true
            authRepository.logout()
            isLoggingOut.value = false
        }
    }

    override fun onSettingsClick() {
        // to be implemented
    }

    override fun onEditClick() {
        // to be implemented
    }

    override fun onImageClick() {
        // to be implemented
    }

    override fun onLogoutClick() {
        logout()
    }
}

interface ProfileRowListener {
    fun onImageClick()
    fun onEditClick()
    fun onSettingsClick()

    fun onLogoutClick()
}

private fun User.toUi() = ProfileUserUiModel(
    imageId = this.avatarId,
    nickname = this.username,
    bio = this.bio,
    email = this.email,
)