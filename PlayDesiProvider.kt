package com.lagradost.cloudstream3

import org.jsoup.Jsoup

class PlayDesiProvider : MainAPI() {
    override val mainUrl = "https://playdesi.net"
    override val name = "PlayDesi"
    override val hasMainPage = true
    override val hasQuickSearch = true

    // ====== Homepage ======
    override suspend fun getMainPage(page: Int, request: MainPageRequest): HomePageResponse {
        val doc = Jsoup.connect(mainUrl).get()
        val items = doc.select("article.item").mapNotNull { 
            MovieSearchResponse(
                name = it.selectFirst("h2.entry-title")?.text() ?: "",
                url = it.selectFirst("a")?.attr("href") ?: return@mapNotNull null,
                posterUrl = it.selectFirst("img")?.attr("data-src") ?: "",
                type = TYPE_MOVIE
            )
        }
        return HomePageResponse(listOf(HomePageList("Latest Content", items)))
    }

    // ====== Search ======
    override suspend fun search(query: String): List<SearchResponse> {
        val searchDoc = Jsoup.connect("$mainUrl/?s=$query").get()
        return searchDoc.select("article.item").map {
            MovieSearchResponse(
                name = it.selectFirst("h2.entry-title")?.text() ?: "",
                url = it.selectFirst("a")?.attr("href") ?: "",
                posterUrl = it.selectFirst("img")?.attr("data-src") ?: ""
            )
        }
    }

    // ====== Video Links ======
    override suspend fun load(url: String): LoadResponse {
        val doc = Jsoup.connect(url).get()
        val iframeUrl = doc.selectFirst("div.player-embed iframe")?.attr("src") 
            ?: throw ErrorLoadingException("No video found")
        
        // Use Hydrax extractor (replace with actual logic)
        val sources = listOf(
            ExtractorLink(
                name = "Hydrax",
                url = iframeUrl,
                referer = "$mainUrl/",
                quality = Qualities.Unknown.value,
                isM3u8 = iframeUrl.contains("m3u8")
            )
        )
        
        return MovieLoadResponse(
            name = doc.selectFirst("h1.entry-title")?.text() ?: "",
            url = url,
            posterUrl = doc.selectFirst("div.post-thumbnail img")?.attr("src"),
            plot = doc.selectFirst("div.entry-content")?.text(),
            links = sources
        )
    }
}
