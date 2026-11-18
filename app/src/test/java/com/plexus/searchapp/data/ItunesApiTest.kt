package com.plexus.searchapp.data

import com.plexus.searchapp.data.remote.createItunesApi
import com.plexus.searchapp.data.remote.dto.toDomain
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ItunesApiTest {
    private val server = MockWebServer()

    @After
    fun tearDown() = server.shutdown()

    @Test
    fun `parses the iTunes payload and sends the expected query`() = runTest {
        // iTunes serves JSON as text/javascript and includes fields we do not model.
        server.enqueue(
            MockResponse()
                .setHeader("Content-Type", "text/javascript; charset=utf-8")
                .setBody(
                    """
                    {"resultCount":2,"results":[
                      {"wrapperType":"track","trackId":10,"trackName":"Better Together",
                       "artistName":"Jack Johnson","collectionName":"In Between Dreams",
                       "primaryGenreName":"Rock","releaseDate":"2005-03-01T08:00:00Z",
                       "trackTimeMillis":207679,"artworkUrl100":"https://img/100.jpg"},
                      {"wrapperType":"collection","collectionName":"No track id"}
                    ]}
                    """.trimIndent(),
                ),
        )
        val api = createItunesApi(baseUrl = server.url("/").toString())

        val response = api.search("jack johnson")

        val request = server.takeRequest()
        assertEquals("jack johnson", request.requestUrl?.queryParameter("term"))
        assertEquals("music", request.requestUrl?.queryParameter("media"))

        val tracks = response.results.mapNotNull { it.toDomain() }
        assertEquals(1, tracks.size)
        assertEquals("Better Together", tracks[0].name)
        assertEquals("Jack Johnson", tracks[0].artist)
        assertEquals(207679L, tracks[0].durationMillis)
        assertNull(response.results[1].toDomain())
    }
}
