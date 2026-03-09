package com.myopenclaw.data.local.cache

import com.myopenclaw.domain.models.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Implementation of CacheManager using SQLDelight database.
 */
class CacheManagerImpl(
    private val database: MyOpenClawDatabase
) : CacheManager {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val queries get() = database.signalWhisperDatabaseQueries

    // ==================== Cache Metadata Operations ====================

    override suspend fun isCacheFresh(cacheKey: String): Boolean = withContext(Dispatchers.IO) {
        val metadata = queries.getCacheMetadata(cacheKey).executeAsOneOrNull()
        if (metadata == null) return@withContext false

        val currentTime = Clock.System.now().toEpochMilliseconds()
        metadata.expires_at > currentTime
    }

    override suspend fun getCacheAge(cacheKey: String): Long? = withContext(Dispatchers.IO) {
        val metadata = queries.getCacheMetadata(cacheKey).executeAsOneOrNull()
            ?: return@withContext null

        Clock.System.now().toEpochMilliseconds() - metadata.cached_at
    }

    override suspend fun updateCacheMetadata(cacheKey: String, durationMs: Long) = withContext(Dispatchers.IO) {
        val currentTime = Clock.System.now().toEpochMilliseconds()
        queries.insertOrReplaceCacheMetadata(
            cache_key = cacheKey,
            cached_at = currentTime,
            expires_at = currentTime + durationMs
        )
    }

    // ==================== User Profile ====================

    override suspend fun getUserProfile(): UserProfileData? = withContext(Dispatchers.IO) {
        val cached = queries.getUserProfile().executeAsOneOrNull() ?: return@withContext null

        UserProfileData(
            id = cached.id,
            email = cached.email,
            fullName = cached.full_name,
            joinedDate = cached.joined_date,
            subscription = SubscriptionInfoData(
                type = cached.subscription_type,
                status = cached.subscription_status,
                planType = cached.plan_type,
                billingCycle = cached.billing_cycle,
                nextBillingDate = cached.next_billing_date,
                trialEndsIn = cached.trial_ends_in?.toInt(),
                renewalFrequency = cached.renewal_frequency
            ),
            preferences = UserPreferencesData(
                language = cached.language,
                notificationsEnabled = cached.notifications_enabled == 1L,
                marketAlertsEnabled = cached.market_alerts_enabled == 1L,
                theme = cached.theme
            )
        )
    }

    override suspend fun saveUserProfile(profile: UserProfileData) = withContext(Dispatchers.IO) {
        queries.insertOrReplaceUserProfile(
            id = profile.id,
            email = profile.email,
            full_name = profile.fullName,
            joined_date = profile.joinedDate,
            subscription_type = profile.subscription.type,
            subscription_status = profile.subscription.status,
            plan_type = profile.subscription.planType,
            billing_cycle = profile.subscription.billingCycle,
            next_billing_date = profile.subscription.nextBillingDate,
            trial_ends_in = profile.subscription.trialEndsIn?.toLong(),
            renewal_frequency = profile.subscription.renewalFrequency,
            language = profile.preferences.language,
            notifications_enabled = if (profile.preferences.notificationsEnabled) 1L else 0L,
            market_alerts_enabled = if (profile.preferences.marketAlertsEnabled) 1L else 0L,
            theme = profile.preferences.theme,
            cached_at = Clock.System.now().toEpochMilliseconds()
        )
        updateCacheMetadata(CachePolicy.Keys.USER_PROFILE, CachePolicy.USER_PROFILE)
    }

    override suspend fun clearUserProfile() = withContext(Dispatchers.IO) {
        queries.deleteUserProfile()
        queries.deleteCacheMetadata(CachePolicy.Keys.USER_PROFILE)
    }

    // ==================== Market Signals ====================

    override suspend fun getMarketSignals(): List<MarketSignalData> = withContext(Dispatchers.IO) {
        queries.getAllMarketSignals().executeAsList().map { cached ->
            MarketSignalData(
                id = cached.id,
                pair = cached.pair,
                type = cached.signal_type,
                priceFrom = cached.price_from,
                priceTo = cached.price_to,
                action = cached.action.takeIf { it.isNotBlank() } ?: "SELL", // Default to SELL if empty
                iconLetter = cached.icon_letter,
                iconColor = cached.icon_color,
                actionTextColor = cached.action_text_color,
                actionBgColor = cached.action_bg_color,
                timestamp = cached.timestamp,
                entryPrice = cached.entry_price,
                closingPrice = cached.closing_price,
                totalPL = cached.total_pl,
                percentReturn = cached.percent_return,
                takeProfit = cached.take_profit,
                stopLoss = cached.stop_loss,
                confidenceScore = cached.confidence_score,
                analystNotes = cached.analyst_notes,
                generatedTime = cached.generated_time,
                closedTime = cached.closed_time,
                status = cached.status
            )
        }
    }

    override suspend fun saveMarketSignals(signals: List<MarketSignalData>, dateGroup: String) = withContext(Dispatchers.IO) {
        val currentTime = Clock.System.now().toEpochMilliseconds()
        queries.deleteAllMarketSignals()

        signals.forEach { signal ->
            queries.insertOrReplaceMarketSignal(
                id = signal.id,
                pair = signal.pair,
                signal_type = signal.type,
                price_from = signal.priceFrom,
                price_to = signal.priceTo,
                action = signal.action,
                icon_letter = signal.iconLetter,
                icon_color = signal.iconColor,
                action_text_color = signal.actionTextColor,
                action_bg_color = signal.actionBgColor,
                timestamp = signal.timestamp,
                entry_price = signal.entryPrice,
                closing_price = signal.closingPrice,
                total_pl = signal.totalPL,
                percent_return = signal.percentReturn,
                take_profit = signal.takeProfit,
                stop_loss = signal.stopLoss,
                confidence_score = signal.confidenceScore,
                analyst_notes = signal.analystNotes,
                generated_time = signal.generatedTime,
                closed_time = signal.closedTime,
                status = signal.status,
                date_group = dateGroup,
                cached_at = currentTime
            )
        }
        updateCacheMetadata(CachePolicy.Keys.MARKET_SIGNALS, CachePolicy.MARKET_SIGNALS)
    }

    override suspend fun getMarketSignalsGrouped(): List<SignalGroupData> = withContext(Dispatchers.IO) {
        val dateGroups = queries.getDistinctDateGroups().executeAsList()

        dateGroups.map { dateGroup ->
            val signals = queries.getMarketSignalsByDateGroup(dateGroup).executeAsList().map { cached ->
                MarketSignalData(
                    id = cached.id,
                    pair = cached.pair,
                    type = cached.signal_type,
                    priceFrom = cached.price_from,
                    priceTo = cached.price_to,
                    action = cached.action.takeIf { it.isNotBlank() } ?: "SELL", // Default to SELL if empty
                    iconLetter = cached.icon_letter,
                    iconColor = cached.icon_color,
                    actionTextColor = cached.action_text_color,
                    actionBgColor = cached.action_bg_color,
                    timestamp = cached.timestamp,
                    entryPrice = cached.entry_price,
                    closingPrice = cached.closing_price,
                    totalPL = cached.total_pl,
                    percentReturn = cached.percent_return,
                    takeProfit = cached.take_profit,
                    stopLoss = cached.stop_loss,
                    confidenceScore = cached.confidence_score,
                    analystNotes = cached.analyst_notes,
                    generatedTime = cached.generated_time,
                    closedTime = cached.closed_time,
                    status = cached.status
                )
            }
            SignalGroupData(dateLabel = dateGroup, signals = signals)
        }
    }

    override suspend fun saveMarketSignalsGrouped(groups: List<SignalGroupData>) = withContext(Dispatchers.IO) {
        val currentTime = Clock.System.now().toEpochMilliseconds()
        queries.deleteAllMarketSignals()

        groups.forEach { group ->
            group.signals.forEach { signal ->
                queries.insertOrReplaceMarketSignal(
                    id = signal.id,
                    pair = signal.pair,
                    signal_type = signal.type,
                    price_from = signal.priceFrom,
                    price_to = signal.priceTo,
                    action = signal.action,
                    icon_letter = signal.iconLetter,
                    icon_color = signal.iconColor,
                    action_text_color = signal.actionTextColor,
                    action_bg_color = signal.actionBgColor,
                    timestamp = signal.timestamp,
                    entry_price = signal.entryPrice,
                    closing_price = signal.closingPrice,
                    total_pl = signal.totalPL,
                    percent_return = signal.percentReturn,
                    take_profit = signal.takeProfit,
                    stop_loss = signal.stopLoss,
                    confidence_score = signal.confidenceScore,
                    analyst_notes = signal.analystNotes,
                    generated_time = signal.generatedTime,
                    closed_time = signal.closedTime,
                    status = signal.status,
                    date_group = group.dateLabel,
                    cached_at = currentTime
                )
            }
        }
        updateCacheMetadata(CachePolicy.Keys.MARKET_SIGNALS_GROUPED, CachePolicy.MARKET_SIGNALS)
    }

    override suspend fun clearMarketSignals() = withContext(Dispatchers.IO) {
        queries.deleteAllMarketSignals()
        queries.deleteCacheMetadata(CachePolicy.Keys.MARKET_SIGNALS)
        queries.deleteCacheMetadata(CachePolicy.Keys.MARKET_SIGNALS_GROUPED)
    }

    // ==================== Insider Trading ====================

    override suspend fun getInsiderTrades(): List<InsiderTrade> = withContext(Dispatchers.IO) {
        queries.getAllInsiderTrades().executeAsList().map { cached ->
            InsiderTrade(
                ticker = cached.ticker,
                company = cached.company,
                insiderName = cached.insider_name,
                insiderTitle = cached.insider_title,
                transactionType = cached.transaction_type,
                shares = cached.shares,
                pricePerShare = cached.price_per_share, // Database stores as NOT NULL, defaults to 0.0
                totalValue = cached.total_value,
                transactionDate = cached.transaction_date,
                filingDate = cached.filing_date,
                formType = cached.form_type,
                secFilingUrl = cached.sec_filing_url,
                performance = cached.performance
            )
        }
    }

    override suspend fun saveInsiderTrades(trades: List<InsiderTrade>) = withContext(Dispatchers.IO) {
        val currentTime = Clock.System.now().toEpochMilliseconds()
        queries.deleteAllInsiderTrades()

        trades.forEach { trade ->
            queries.insertInsiderTrade(
                ticker = trade.ticker,
                company = trade.company,
                insider_name = trade.insiderName,
                insider_title = trade.insiderTitle,
                transaction_type = trade.transactionType,
                shares = trade.shares,
                price_per_share = trade.pricePerShare ?: 0.0, // Handle null from API
                total_value = trade.totalValue,
                transaction_date = trade.transactionDate,
                filing_date = trade.filingDate,
                form_type = trade.formType,
                sec_filing_url = trade.secFilingUrl,
                performance = trade.performance,
                cached_at = currentTime
            )
        }
        updateCacheMetadata(CachePolicy.Keys.INSIDER_TRADING, CachePolicy.INSIDER_TRADING)
    }

    override suspend fun clearInsiderTrades() = withContext(Dispatchers.IO) {
        queries.deleteAllInsiderTrades()
        queries.deleteCacheMetadata(CachePolicy.Keys.INSIDER_TRADING)
    }

    // ==================== Congress Trading ====================

    override suspend fun getCongressTrades(): List<CongressTrade> = withContext(Dispatchers.IO) {
        queries.getAllCongressTrades().executeAsList().map { cached ->
            CongressTrade(
                politician = cached.politician,
                party = cached.party,
                state = cached.state,
                position = cached.position,
                ticker = cached.ticker,
                company = cached.company,
                transactionType = cached.transaction_type,
                amount = cached.amount,
                transactionDate = cached.transaction_date,
                filingDate = cached.filing_date,
                disclosureUrl = cached.disclosure_url,
                performance = cached.performance
            )
        }
    }

    override suspend fun saveCongressTrades(trades: List<CongressTrade>) = withContext(Dispatchers.IO) {
        val currentTime = Clock.System.now().toEpochMilliseconds()
        queries.deleteAllCongressTrades()

        trades.forEach { trade ->
            queries.insertCongressTrade(
                politician = trade.politician,
                party = trade.party,
                state = trade.state,
                position = trade.position,
                ticker = trade.ticker,
                company = trade.company,
                transaction_type = trade.transactionType,
                amount = trade.amount,
                transaction_date = trade.transactionDate,
                filing_date = trade.filingDate,
                disclosure_url = trade.disclosureUrl,
                performance = trade.performance,
                cached_at = currentTime
            )
        }
        updateCacheMetadata(CachePolicy.Keys.CONGRESS_TRADING, CachePolicy.CONGRESS_TRADING)
    }

    override suspend fun clearCongressTrades() = withContext(Dispatchers.IO) {
        queries.deleteAllCongressTrades()
        queries.deleteCacheMetadata(CachePolicy.Keys.CONGRESS_TRADING)
    }

    // ==================== Options Flow ====================

    override suspend fun getOptionsFlow(): List<OptionsFlow> = withContext(Dispatchers.IO) {
        queries.getAllOptionsFlow().executeAsList().map { cached ->
            OptionsFlow(
                ticker = cached.ticker,
                tradeDate = cached.trade_date,
                expirationDate = cached.expiration_date,
                strike = cached.strike,
                optionType = cached.option_type,
                volume = cached.volume.toInt(),
                openInterest = cached.open_interest.toInt(),
                premium = cached.premium,
                sentiment = cached.sentiment,
                isUnusual = cached.is_unusual == 1L
            )
        }
    }

    override suspend fun saveOptionsFlow(options: List<OptionsFlow>) = withContext(Dispatchers.IO) {
        val currentTime = Clock.System.now().toEpochMilliseconds()
        queries.deleteAllOptionsFlow()

        options.forEach { option ->
            queries.insertOptionsFlow(
                ticker = option.ticker,
                trade_date = option.tradeDate,
                expiration_date = option.expirationDate,
                strike = option.strike,
                option_type = option.optionType,
                volume = option.volume.toLong(),
                open_interest = option.openInterest.toLong(),
                premium = option.premium,
                sentiment = option.sentiment,
                is_unusual = if (option.isUnusual) 1L else 0L,
                cached_at = currentTime
            )
        }
        updateCacheMetadata(CachePolicy.Keys.OPTIONS_FLOW, CachePolicy.OPTIONS_FLOW)
    }

    override suspend fun clearOptionsFlow() = withContext(Dispatchers.IO) {
        queries.deleteAllOptionsFlow()
        queries.deleteCacheMetadata(CachePolicy.Keys.OPTIONS_FLOW)
    }

    // ==================== Social Sentiment ====================

    override suspend fun getSocialSentiment(): List<SocialSentiment> = withContext(Dispatchers.IO) {
        queries.getAllSocialSentiment().executeAsList().map { cached ->
            val topPosts: List<SocialPost> = try {
                json.decodeFromString(cached.top_posts_json)
            } catch (e: Exception) {
                emptyList()
            }

            SocialSentiment(
                ticker = cached.ticker,
                company = cached.company.takeIf { it.isNotBlank() }, // Convert empty string to null
                platform = cached.platform,
                mentionsCount = cached.mentions_count.toInt(),
                sentimentScore = cached.sentiment_score,
                sentimentLabel = cached.sentiment_label,
                volumeChange = cached.volume_change,
                topPosts = topPosts,
                timestamp = cached.timestamp
            )
        }
    }

    override suspend fun saveSocialSentiment(sentiments: List<SocialSentiment>) = withContext(Dispatchers.IO) {
        val currentTime = Clock.System.now().toEpochMilliseconds()
        queries.deleteAllSocialSentiment()

        sentiments.forEach { sentiment ->
            queries.insertSocialSentiment(
                ticker = sentiment.ticker,
                company = sentiment.company ?: "",
                platform = sentiment.platform,
                mentions_count = sentiment.mentionsCount.toLong(),
                sentiment_score = sentiment.sentimentScore,
                sentiment_label = sentiment.sentimentLabel,
                volume_change = sentiment.volumeChange,
                top_posts_json = json.encodeToString(sentiment.topPosts),
                timestamp = sentiment.timestamp,
                cached_at = currentTime
            )
        }
        updateCacheMetadata(CachePolicy.Keys.SOCIAL_SENTIMENT, CachePolicy.SOCIAL_SENTIMENT)
    }

    override suspend fun clearSocialSentiment() = withContext(Dispatchers.IO) {
        queries.deleteAllSocialSentiment()
        queries.deleteCacheMetadata(CachePolicy.Keys.SOCIAL_SENTIMENT)
    }

    // ==================== Dark Pool ====================

    override suspend fun getDarkPoolData(): List<DarkPoolData> = withContext(Dispatchers.IO) {
        queries.getAllDarkPool().executeAsList().map { cached ->
            val topVenues: List<DarkPoolVenue> = try {
                json.decodeFromString(cached.top_venues_json)
            } catch (e: Exception) {
                emptyList()
            }

            DarkPoolData(
                ticker = cached.ticker,
                company = cached.company,
                totalVolume = cached.total_volume, // Database stores as NOT NULL, defaults to 0
                darkPoolVolume = cached.dark_pool_volume,
                darkPoolPercent = cached.dark_pool_percent,
                shortVolume = cached.short_volume,
                shortPercent = cached.short_percent,
                topVenues = topVenues,
                date = cached.date
            )
        }
    }

    override suspend fun saveDarkPoolData(data: List<DarkPoolData>) = withContext(Dispatchers.IO) {
        val currentTime = Clock.System.now().toEpochMilliseconds()
        queries.deleteAllDarkPool()

        data.forEach { item ->
            queries.insertDarkPool(
                ticker = item.ticker,
                company = item.company,
                total_volume = item.totalVolume ?: 0L, // Handle null from API
                dark_pool_volume = item.darkPoolVolume,
                dark_pool_percent = item.darkPoolPercent,
                short_volume = item.shortVolume,
                short_percent = item.shortPercent,
                top_venues_json = json.encodeToString(item.topVenues),
                date = item.date,
                cached_at = currentTime
            )
        }
        updateCacheMetadata(CachePolicy.Keys.DARK_POOL, CachePolicy.DARK_POOL)
    }

    override suspend fun clearDarkPoolData() = withContext(Dispatchers.IO) {
        queries.deleteAllDarkPool()
        queries.deleteCacheMetadata(CachePolicy.Keys.DARK_POOL)
    }

    // ==================== Government Contracts ====================

    override suspend fun getGovernmentContracts(): List<GovernmentContract> = withContext(Dispatchers.IO) {
        queries.getAllGovernmentContracts().executeAsList().map { cached ->
            GovernmentContract(
                contractId = cached.contract_id,
                company = cached.company,
                ticker = cached.ticker,
                agency = cached.agency,
                description = cached.description,
                amount = cached.amount,
                awardType = cached.award_type,
                awardDate = cached.award_date,
                startDate = cached.start_date,
                endDate = cached.end_date,
                sector = cached.sector,
                status = cached.status,
                naicsCode = cached.naics_code,
                contractUrl = cached.contract_url
            )
        }
    }

    override suspend fun saveGovernmentContracts(contracts: List<GovernmentContract>) = withContext(Dispatchers.IO) {
        val currentTime = Clock.System.now().toEpochMilliseconds()
        queries.deleteAllGovernmentContracts()

        contracts.forEach { contract ->
            queries.insertOrReplaceGovernmentContract(
                contract_id = contract.contractId,
                company = contract.company,
                ticker = contract.ticker,
                agency = contract.agency,
                description = contract.description,
                amount = contract.amount,
                award_type = contract.awardType,
                award_date = contract.awardDate,
                start_date = contract.startDate,
                end_date = contract.endDate,
                sector = contract.sector,
                status = contract.status,
                naics_code = contract.naicsCode,
                contract_url = contract.contractUrl,
                cached_at = currentTime
            )
        }
        updateCacheMetadata(CachePolicy.Keys.GOVERNMENT_CONTRACTS, CachePolicy.GOVERNMENT_CONTRACTS)
    }

    override suspend fun clearGovernmentContracts() = withContext(Dispatchers.IO) {
        queries.deleteAllGovernmentContracts()
        queries.deleteCacheMetadata(CachePolicy.Keys.GOVERNMENT_CONTRACTS)
    }

    // ==================== Lobbyist Activity ====================

    override suspend fun getLobbyistActivity(): List<LobbyistActivity> = withContext(Dispatchers.IO) {
        queries.getAllLobbyistActivity().executeAsList().map { cached ->
            val issueAreas: List<String>? = cached.issue_areas_json?.let {
                try {
                    json.decodeFromString(it)
                } catch (e: Exception) {
                    null
                }
            }

            LobbyistActivity(
                company = cached.company,
                ticker = cached.ticker,
                lobbyistName = cached.lobbyist_name,
                clientName = cached.client_name,
                amount = cached.amount,
                issueAreas = issueAreas,
                specificIssues = cached.specific_issues,
                reportYear = cached.report_year.toInt(),
                filingDate = cached.filing_date
            )
        }
    }

    override suspend fun saveLobbyistActivity(activities: List<LobbyistActivity>) = withContext(Dispatchers.IO) {
        val currentTime = Clock.System.now().toEpochMilliseconds()
        queries.deleteAllLobbyistActivity()

        activities.forEach { activity ->
            queries.insertLobbyistActivity(
                company = activity.company,
                ticker = activity.ticker,
                lobbyist_name = activity.lobbyistName,
                client_name = activity.clientName,
                amount = activity.amount,
                issue_areas_json = activity.issueAreas?.let { json.encodeToString(it) },
                specific_issues = activity.specificIssues,
                report_year = activity.reportYear.toLong(),
                filing_date = activity.filingDate,
                cached_at = currentTime
            )
        }
        updateCacheMetadata(CachePolicy.Keys.LOBBYIST_ACTIVITY, CachePolicy.LOBBYIST_ACTIVITY)
    }

    override suspend fun clearLobbyistActivity() = withContext(Dispatchers.IO) {
        queries.deleteAllLobbyistActivity()
        queries.deleteCacheMetadata(CachePolicy.Keys.LOBBYIST_ACTIVITY)
    }

    // ==================== Political Donations ====================

    override suspend fun getPoliticalDonations(): List<PoliticalDonation> = withContext(Dispatchers.IO) {
        queries.getAllPoliticalDonations().executeAsList().map { cached ->
            PoliticalDonation(
                company = cached.company,
                ticker = cached.ticker,
                donorName = cached.donor_name,
                recipientName = cached.recipient_name,
                recipientType = cached.recipient_type,
                recipientParty = cached.recipient_party,
                donationType = cached.donation_type,
                amount = cached.amount,
                electionYear = cached.election_year.toInt(),
                filingDate = cached.filing_date
            )
        }
    }

    override suspend fun savePoliticalDonations(donations: List<PoliticalDonation>) = withContext(Dispatchers.IO) {
        val currentTime = Clock.System.now().toEpochMilliseconds()
        queries.deleteAllPoliticalDonations()

        donations.forEach { donation ->
            queries.insertPoliticalDonation(
                company = donation.company,
                ticker = donation.ticker,
                donor_name = donation.donorName,
                recipient_name = donation.recipientName,
                recipient_type = donation.recipientType,
                recipient_party = donation.recipientParty,
                donation_type = donation.donationType,
                amount = donation.amount,
                election_year = donation.electionYear.toLong(),
                filing_date = donation.filingDate,
                cached_at = currentTime
            )
        }
        updateCacheMetadata(CachePolicy.Keys.POLITICAL_DONATIONS, CachePolicy.POLITICAL_DONATIONS)
    }

    override suspend fun clearPoliticalDonations() = withContext(Dispatchers.IO) {
        queries.deleteAllPoliticalDonations()
        queries.deleteCacheMetadata(CachePolicy.Keys.POLITICAL_DONATIONS)
    }

    // ==================== Analysis History ====================

    override suspend fun getAnalysisHistory(): List<ChartAnalysisData> = withContext(Dispatchers.IO) {
        queries.getAllAnalyses().executeAsList().map { it.toChartAnalysisData() }
    }

    override suspend fun saveAnalysisHistory(analyses: List<ChartAnalysisData>, dateGroup: String) = withContext(Dispatchers.IO) {
        val currentTime = Clock.System.now().toEpochMilliseconds()
        // Only clear non-saved analyses
        queries.deleteAllAnalyses()

        analyses.forEach { analysis ->
            saveAnalysisToDb(analysis, dateGroup, isSaved = false, currentTime)
        }
        updateCacheMetadata(CachePolicy.Keys.ANALYSIS_HISTORY, CachePolicy.ANALYSIS_HISTORY)
    }

    override suspend fun getAnalysisHistoryGrouped(): List<AnalysisGroupData> = withContext(Dispatchers.IO) {
        val dateGroups = queries.getDistinctAnalysisDateGroups().executeAsList()

        dateGroups.map { dateGroup ->
            val analyses = queries.getAnalysesByDateGroup(dateGroup).executeAsList()
                .map { it.toChartAnalysisData() }
            AnalysisGroupData(dateLabel = dateGroup, analyses = analyses)
        }
    }

    override suspend fun saveAnalysisHistoryGrouped(groups: List<AnalysisGroupData>) = withContext(Dispatchers.IO) {
        val currentTime = Clock.System.now().toEpochMilliseconds()
        queries.deleteAllAnalyses()

        groups.forEach { group ->
            group.analyses.forEach { analysis ->
                saveAnalysisToDb(analysis, group.dateLabel, isSaved = false, currentTime)
            }
        }
        updateCacheMetadata(CachePolicy.Keys.ANALYSIS_HISTORY_GROUPED, CachePolicy.ANALYSIS_HISTORY)
    }

    override suspend fun clearAnalysisHistory() = withContext(Dispatchers.IO) {
        queries.deleteAllAnalyses()
        queries.deleteCacheMetadata(CachePolicy.Keys.ANALYSIS_HISTORY)
        queries.deleteCacheMetadata(CachePolicy.Keys.ANALYSIS_HISTORY_GROUPED)
    }

    // ==================== Saved Analyses ====================

    override suspend fun getSavedAnalyses(): List<ChartAnalysisData> = withContext(Dispatchers.IO) {
        queries.getSavedAnalyses().executeAsList().map { it.toChartAnalysisData() }
    }

    override suspend fun saveSavedAnalyses(analyses: List<ChartAnalysisData>) = withContext(Dispatchers.IO) {
        val currentTime = Clock.System.now().toEpochMilliseconds()
        queries.deleteSavedAnalyses()

        analyses.forEach { analysis ->
            saveAnalysisToDb(analysis, "saved", isSaved = true, currentTime)
        }
        updateCacheMetadata(CachePolicy.Keys.SAVED_ANALYSES, CachePolicy.SAVED_ANALYSES)
    }

    override suspend fun clearSavedAnalyses() = withContext(Dispatchers.IO) {
        queries.deleteSavedAnalyses()
        queries.deleteCacheMetadata(CachePolicy.Keys.SAVED_ANALYSES)
    }

    private fun saveAnalysisToDb(
        analysis: ChartAnalysisData,
        dateGroup: String,
        isSaved: Boolean,
        currentTime: Long
    ) {
        queries.insertOrReplaceAnalysis(
            id = analysis.id,
            order_id = analysis.oderId,
            asset = analysis.asset,
            analysis_type = analysis.type,
            confidence_score = analysis.confidenceScore,
            chart_image_url = analysis.chartImageUrl,
            chart_image_base64 = analysis.chartImageBase64,
            chart_mime_type = analysis.chartMimeType,
            key_insights_json = analysis.keyInsights?.let { json.encodeToString(it) },
            gameplan_json = analysis.gameplan?.let { json.encodeToString(it) },
            additional_details_json = analysis.additionalDetails?.let { json.encodeToString(it) },
            timestamp = analysis.timestamp,
            created_at = analysis.createdAt,
            date_group = dateGroup,
            is_saved = if (isSaved) 1L else 0L,
            cached_at = currentTime
        )
    }

    private fun CachedAnalysis.toChartAnalysisData(): ChartAnalysisData {
        return ChartAnalysisData(
            id = id,
            oderId = order_id,
            asset = asset,
            type = analysis_type,
            confidenceScore = confidence_score,
            chartImageUrl = chart_image_url,
            chartImageBase64 = chart_image_base64,
            chartMimeType = chart_mime_type,
            keyInsights = key_insights_json?.let {
                try { json.decodeFromString(it) } catch (e: Exception) { null }
            },
            gameplan = gameplan_json?.let {
                try { json.decodeFromString(it) } catch (e: Exception) { null }
            },
            additionalDetails = additional_details_json?.let {
                try { json.decodeFromString(it) } catch (e: Exception) { null }
            },
            timestamp = timestamp,
            createdAt = created_at
        )
    }

    // ==================== Trade Ideas ====================

    override suspend fun getTradeIdeas(): List<TradeIdea> = withContext(Dispatchers.IO) {
        queries.getAllTradeIdeas().executeAsList().map { cached ->
            val catalysts: List<String>? = cached.catalysts_json?.let {
                try { json.decodeFromString(it) } catch (e: Exception) { null }
            }
            val risks: List<String>? = cached.risks_json?.let {
                try { json.decodeFromString(it) } catch (e: Exception) { null }
            }

            TradeIdea(
                ticker = cached.ticker,
                company = cached.company,
                recommendation = cached.recommendation,
                confidence = cached.confidence,
                reasoning = cached.reasoning,
                priceTarget = cached.price_target,
                stopLoss = cached.stop_loss,
                timeframe = cached.timeframe,
                catalysts = catalysts,
                risks = risks
            )
        }
    }

    override suspend fun saveTradeIdeas(ideas: List<TradeIdea>) = withContext(Dispatchers.IO) {
        val currentTime = Clock.System.now().toEpochMilliseconds()
        queries.deleteAllTradeIdeas()

        ideas.forEach { idea ->
            queries.insertTradeIdea(
                ticker = idea.ticker,
                company = idea.company,
                recommendation = idea.recommendation,
                confidence = idea.confidence,
                reasoning = idea.reasoning,
                price_target = idea.priceTarget,
                stop_loss = idea.stopLoss,
                timeframe = idea.timeframe,
                catalysts_json = idea.catalysts?.let { json.encodeToString(it) },
                risks_json = idea.risks?.let { json.encodeToString(it) },
                cached_at = currentTime
            )
        }
        updateCacheMetadata(CachePolicy.Keys.TRADE_IDEAS, CachePolicy.TRADE_IDEAS)
    }

    override suspend fun clearTradeIdeas() = withContext(Dispatchers.IO) {
        queries.deleteAllTradeIdeas()
        queries.deleteCacheMetadata(CachePolicy.Keys.TRADE_IDEAS)
    }

    // ==================== Subscription Pricing ====================

    override suspend fun getSubscriptionPricing(): List<SubscriptionPricingData> = withContext(Dispatchers.IO) {
        queries.getAllSubscriptionPricing().executeAsList().map { cached ->
            val features: List<PremiumFeatureData> = try {
                json.decodeFromString(cached.features_json)
            } catch (e: Exception) {
                emptyList()
            }

            SubscriptionPricingData(
                planType = cached.plan_type,
                price = cached.price,
                currency = cached.currency,
                billingFrequency = cached.billing_frequency,
                trialDays = cached.trial_days?.toInt(),
                discount = cached.discount?.toInt(),
                originalPrice = cached.original_price,
                priceId = cached.price_id,
                features = features
            )
        }
    }

    override suspend fun saveSubscriptionPricing(pricing: List<SubscriptionPricingData>) = withContext(Dispatchers.IO) {
        val currentTime = Clock.System.now().toEpochMilliseconds()
        queries.deleteAllSubscriptionPricing()

        pricing.forEach { plan ->
            queries.insertOrReplaceSubscriptionPricing(
                plan_type = plan.planType,
                price = plan.price,
                currency = plan.currency,
                billing_frequency = plan.billingFrequency,
                trial_days = plan.trialDays?.toLong(),
                discount = plan.discount?.toLong(),
                original_price = plan.originalPrice,
                price_id = plan.priceId,
                features_json = json.encodeToString(plan.features),
                cached_at = currentTime
            )
        }
        updateCacheMetadata(CachePolicy.Keys.SUBSCRIPTION_PRICING, CachePolicy.SUBSCRIPTION_PRICING)
    }

    override suspend fun clearSubscriptionPricing() = withContext(Dispatchers.IO) {
        queries.deleteAllSubscriptionPricing()
        queries.deleteCacheMetadata(CachePolicy.Keys.SUBSCRIPTION_PRICING)
    }

    // ==================== Global Cache Operations ====================

    override suspend fun clearAllCache() = withContext(Dispatchers.IO) {
        queries.clearAllCacheMetadata()
        queries.deleteUserProfile()
        queries.deleteAllMarketSignals()
        queries.deleteAllInsiderTrades()
        queries.deleteAllCongressTrades()
        queries.deleteAllOptionsFlow()
        queries.deleteAllSocialSentiment()
        queries.deleteAllDarkPool()
        queries.deleteAllGovernmentContracts()
        queries.deleteAllLobbyistActivity()
        queries.deleteAllPoliticalDonations()
        queries.deleteAllAnalyses()
        queries.deleteAllTradeIdeas()
        queries.deleteAllSubscriptionPricing()
    }

    override suspend fun clearExpiredCache() = withContext(Dispatchers.IO) {
        val currentTime = Clock.System.now().toEpochMilliseconds()
        queries.deleteExpiredCache(currentTime)
    }
}
