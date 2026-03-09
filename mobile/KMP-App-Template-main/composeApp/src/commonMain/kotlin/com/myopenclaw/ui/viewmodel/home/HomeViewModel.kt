package com.myopenclaw.ui.viewmodel.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.myopenclaw.domain.models.InsiderTrade
import com.myopenclaw.domain.models.CongressTrade
import com.myopenclaw.domain.models.OptionsFlow
import com.myopenclaw.domain.models.TradeIdea
import com.myopenclaw.domain.usecase.marketdata.GetInsiderTradingUseCase
import com.myopenclaw.domain.usecase.marketdata.GetCongressTradingUseCase
import com.myopenclaw.domain.usecase.marketdata.GetOptionsFlowUseCase
import com.myopenclaw.domain.usecase.marketdata.GetTradeIdeasUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll

data class DashboardData(
    val recentInsiderTrades: List<InsiderTrade> = emptyList(),
    val recentCongressTrades: List<CongressTrade> = emptyList(),
    val unusualOptionsFlow: List<OptionsFlow> = emptyList(),
    val tradeIdeas: List<TradeIdea> = emptyList()
)

sealed class HomeState {
    data object Idle : HomeState()
    data object Loading : HomeState()
    data class Success(val data: DashboardData) : HomeState()
    data class Error(val message: String) : HomeState()
}

sealed class TradeIdeasState {
    data object Idle : TradeIdeasState()
    data object Loading : TradeIdeasState()
    data class Success(val ideas: List<TradeIdea>) : TradeIdeasState()
    data class Error(val message: String) : TradeIdeasState()
}

class HomeViewModel(
    private val getInsiderTradingUseCase: GetInsiderTradingUseCase,
    private val getCongressTradingUseCase: GetCongressTradingUseCase,
    private val getOptionsFlowUseCase: GetOptionsFlowUseCase,
    private val getTradeIdeasUseCase: GetTradeIdeasUseCase
) : ViewModel() {

    private val _homeState = MutableStateFlow<HomeState>(HomeState.Idle)
    val homeState: StateFlow<HomeState> = _homeState.asStateFlow()

    private val _tradeIdeasState = MutableStateFlow<TradeIdeasState>(TradeIdeasState.Idle)
    val tradeIdeasState: StateFlow<TradeIdeasState> = _tradeIdeasState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        loadDashboardData()
    }

    fun loadDashboardData() {
        // println("HomeViewModel: loadDashboardData() called")
        viewModelScope.launch {
            // println("HomeViewModel: Setting state to Loading")
            _homeState.value = HomeState.Loading

            try {
                // println("HomeViewModel: Starting parallel API calls...")
                // Load all market data in parallel
                val insiderDeferred = async {
                    // println("HomeViewModel: Calling getInsiderTradingUseCase(limit=10, days=7)")
                    getInsiderTradingUseCase(limit = 10, days = 7)
                }
                val congressDeferred = async {
                    // println("HomeViewModel: Calling getCongressTradingUseCase(limit=10)")
                    getCongressTradingUseCase(limit = 10)
                }
                val optionsDeferred = async {
                    // println("HomeViewModel: Calling getOptionsFlowUseCase(limit=10, action=unusual)")
                    getOptionsFlowUseCase(limit = 10, action = "unusual")
                }

                // println("HomeViewModel: Awaiting insider trading result...")
                val insiderResult = insiderDeferred.await()
                // println("HomeViewModel: Insider trading result - success=${insiderResult.isSuccess}, data size=${insiderResult.getOrNull()?.size ?: 0}")

                // println("HomeViewModel: Awaiting congress trading result...")
                val congressResult = congressDeferred.await()
                // println("HomeViewModel: Congress trading result - success=${congressResult.isSuccess}, data size=${congressResult.getOrNull()?.size ?: 0}")

                // println("HomeViewModel: Awaiting options flow result...")
                val optionsResult = optionsDeferred.await()
                // println("HomeViewModel: Options flow result - success=${optionsResult.isSuccess}, data size=${optionsResult.getOrNull()?.size ?: 0}")

                // Combine results
                val dashboardData = DashboardData(
                    recentInsiderTrades = insiderResult.getOrDefault(emptyList()),
                    recentCongressTrades = congressResult.getOrDefault(emptyList()),
                    unusualOptionsFlow = optionsResult.getOrDefault(emptyList())
                )

                // println("HomeViewModel: Dashboard data loaded - insider=${dashboardData.recentInsiderTrades.size}, congress=${dashboardData.recentCongressTrades.size}, options=${dashboardData.unusualOptionsFlow.size}")
                _homeState.value = HomeState.Success(dashboardData)
                // println("HomeViewModel: State set to Success")
            } catch (e: Exception) {
                // println("HomeViewModel: ERROR loading dashboard data - ${e.message}")
                e.printStackTrace()
                _homeState.value = HomeState.Error(
                    e.message ?: "Failed to load dashboard data"
                )
            }
        }
    }

    fun loadTradeIdeas() {
        viewModelScope.launch {
            _tradeIdeasState.value = TradeIdeasState.Loading

            getTradeIdeasUseCase()
                .onSuccess { ideas ->
                    _tradeIdeasState.value = TradeIdeasState.Success(ideas)
                }
                .onFailure { exception ->
                    _tradeIdeasState.value = TradeIdeasState.Error(
                        exception.message ?: "Failed to load trade ideas"
                    )
                }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            loadDashboardData()
            _isRefreshing.value = false
        }
    }

    // Quick access counts for dashboard summary
    fun getInsiderTradesCount(): Int {
        return when (val state = _homeState.value) {
            is HomeState.Success -> state.data.recentInsiderTrades.size
            else -> 0
        }
    }

    fun getCongressTradesCount(): Int {
        return when (val state = _homeState.value) {
            is HomeState.Success -> state.data.recentCongressTrades.size
            else -> 0
        }
    }

    fun getOptionsFlowCount(): Int {
        return when (val state = _homeState.value) {
            is HomeState.Success -> state.data.unusualOptionsFlow.size
            else -> 0
        }
    }
}
