package io.github.ugurbuga.inspectodemo

import io.github.ugurbuga.inspectodemo.data.ChannelItem
import io.github.ugurbuga.inspectodemo.data.SeriesItem
import io.github.ugurbuga.inspectodemo.data.SeriesRepository
import io.github.ugurbuga.inspectodemo.domain.SeriesViewModel
import io.github.ugurbuga.inspectodemo.util.CryptoUtils
import kotlinx.datetime.DayOfWeek
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SeriesViewModelTest {

    @Test
    fun testProcessAndGroupSeriesExactDate() {
        val series = listOf(
            SeriesItem(name = "Dizi A", channel = "atv", date = "2026-11-09", day = "Pazartesi", time = "20:00"),
            SeriesItem(name = "Dizi B", channel = "Show TV", date = "2026-11-09", day = "Pazartesi", time = "21:00"),
            SeriesItem(name = "Dizi C", channel = "TRT 1", date = "2026-11-10", day = "Salı", time = "20:00")
        )

        val groups = SeriesViewModel.processAndGroupSeries(series, "2026-11-09")

        assertEquals(2, groups.size)
        assertEquals("2026-11-09", groups[0].date)
        assertTrue(groups[0].isToday)
        assertEquals(2, groups[0].series.size)

        assertEquals("2026-11-10", groups[1].date)
        assertFalse(groups[1].isToday)
        assertEquals(1, groups[1].series.size)
    }

    @Test
    fun testProcessAndGroupSeriesDayNameFallback() {
        val series = listOf(
            SeriesItem(name = "Dizi A", channel = "atv", date = "2026-11-09", day = "Pazartesi", time = "20:00"),
            SeriesItem(name = "Dizi C", channel = "TRT 1", date = "2026-11-10", day = "Salı", time = "20:00")
        )

        // Date is not matching, but dayName "Salı" matches group[1]
        val groups = SeriesViewModel.processAndGroupSeries(series, "2025-05-10", "Salı")

        assertEquals(2, groups.size)
        assertFalse(groups[0].isToday)
        assertTrue(groups[1].isToday)
    }

    @Test
    fun testCalculateTodayScrollIndex() {
        val series = listOf(
            SeriesItem(name = "Dizi A", channel = "atv", date = "2026-11-08", day = "Pazar"),
            SeriesItem(name = "Dizi B", channel = "Show TV", date = "2026-11-09", day = "Pazartesi"),
            SeriesItem(name = "Dizi C", channel = "TRT 1", date = "2026-11-09", day = "Pazartesi")
        )

        val groups = SeriesViewModel.processAndGroupSeries(series, "2026-11-09")
        val scrollIndex = SeriesViewModel.calculateTodayScrollIndex(groups)

        assertEquals(2, scrollIndex)
    }

    @Test
    fun testGetTurkishDayName() {
        assertEquals("Pazartesi", SeriesViewModel.getTurkishDayName(DayOfWeek.MONDAY))
        assertEquals("Salı", SeriesViewModel.getTurkishDayName(DayOfWeek.TUESDAY))
        assertEquals("Çarşamba", SeriesViewModel.getTurkishDayName(DayOfWeek.WEDNESDAY))
        assertEquals("Perşembe", SeriesViewModel.getTurkishDayName(DayOfWeek.THURSDAY))
        assertEquals("Cuma", SeriesViewModel.getTurkishDayName(DayOfWeek.FRIDAY))
        assertEquals("Cumartesi", SeriesViewModel.getTurkishDayName(DayOfWeek.SATURDAY))
        assertEquals("Pazar", SeriesViewModel.getTurkishDayName(DayOfWeek.SUNDAY))
    }

    @Test
    fun testFormatFullUrlAndFindChannelLogo() {
        val relative = "assets/series/anne-yarisi.jpg"
        val expected = "https://raw.githubusercontent.com/ugurbuga/turkish-tv-tracker/main/assets/series/anne-yarisi.jpg"
        assertEquals(expected, SeriesRepository.formatFullUrl(relative))

        val fullUrl = "https://example.com/image.png"
        assertEquals(fullUrl, SeriesRepository.formatFullUrl(fullUrl))

        val channels = listOf(
            ChannelItem(id = "atv", name = "atv", logo = "assets/channel-logos/atv.png"),
            ChannelItem(id = "show-tv", name = "Show TV", logo = "assets/channel-logos/show-tv.png")
        )

        assertEquals("assets/channel-logos/atv.png", SeriesRepository.findChannelLogo("atv", channels))
        assertEquals("assets/channel-logos/show-tv.png", SeriesRepository.findChannelLogo("Show TV", channels))
    }

    @Test
    fun testXorDecrypt() {
        val encrypted = "HAEGGxpJRwJFRh1FFggZAkscSEYfVl8OHF8KBQcBAAEFWQFfCBAfBBxPR1w="
        val decrypted = CryptoUtils.xorDecrypt(encrypted)
        assertTrue(decrypted != null && decrypted.isNotEmpty())
    }
}
