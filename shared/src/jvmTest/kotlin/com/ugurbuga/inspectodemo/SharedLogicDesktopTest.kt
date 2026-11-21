package io.github.ugurbuga.inspectodemo

import io.github.ugurbuga.inspectodemo.ui.SeriesStatus
import io.github.ugurbuga.inspectodemo.util.getTodayLocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class SharedLogicDesktopTest {

    @Test
    fun testGetTodayLocalDate() {
        val today = getTodayLocalDate()
        assertNotNull(today)
        assertTrue(today.year >= 2025)
        assertTrue(today.monthNumber in 1..12)
        assertTrue(today.dayOfMonth in 1..31)
    }

    @Test
    fun testSeriesStatusCombinations() {
        // Yeni Bölüm combinations
        assertEquals(SeriesStatus.YENI_BOLUM, SeriesStatus.from("Yeni Bölüm"))
        assertEquals(SeriesStatus.YENI_BOLUM, SeriesStatus.from("YENİ BÖLÜM"))
        assertEquals(SeriesStatus.YENI_BOLUM, SeriesStatus.from("YENI BOLUM"))
        assertEquals(SeriesStatus.YENI_BOLUM, SeriesStatus.from("yeni bolum"))
        assertEquals(SeriesStatus.YENI_BOLUM, SeriesStatus.from("yeni bölüm"))
        assertEquals(SeriesStatus.YENI_BOLUM, SeriesStatus.from("YENİ BÖLÜM (14. Bölüm)"))
        assertEquals(SeriesStatus.YENI_BOLUM, SeriesStatus.from("YENİ BÖLÜM / CANLI"))

        // Yeni Dizi combinations
        assertEquals(SeriesStatus.YENI_DIZI, SeriesStatus.from("Yeni Dizi"))
        assertEquals(SeriesStatus.YENI_DIZI, SeriesStatus.from("YENİ DİZİ"))
        assertEquals(SeriesStatus.YENI_DIZI, SeriesStatus.from("YENI DIZI"))
        assertEquals(SeriesStatus.YENI_DIZI, SeriesStatus.from("yeni dizi"))
        assertEquals(SeriesStatus.YENI_DIZI, SeriesStatus.from("İLK BÖLÜM"))
        assertEquals(SeriesStatus.YENI_DIZI, SeriesStatus.from("ILK BOLUM"))
        assertEquals(SeriesStatus.YENI_DIZI, SeriesStatus.from("YENİ BAŞLIYOR"))

        // Yeni Sezon combinations
        assertEquals(SeriesStatus.YENI_SEZON, SeriesStatus.from("Yeni Sezon"))
        assertEquals(SeriesStatus.YENI_SEZON, SeriesStatus.from("YENİ SEZON"))
        assertEquals(SeriesStatus.YENI_SEZON, SeriesStatus.from("YENI SEZON"))
        assertEquals(SeriesStatus.YENI_SEZON, SeriesStatus.from("yeni sezon"))
        assertEquals(SeriesStatus.YENI_SEZON, SeriesStatus.from("SEZON PRÖMİYERİ"))
        assertEquals(SeriesStatus.YENI_SEZON, SeriesStatus.from("YENİ SEZON BAŞLIYOR"))

        // Final combinations
        assertEquals(SeriesStatus.FINAL, SeriesStatus.from("Final"))
        assertEquals(SeriesStatus.FINAL, SeriesStatus.from("FİNAL"))
        assertEquals(SeriesStatus.FINAL, SeriesStatus.from("FINAL"))
        assertEquals(SeriesStatus.FINAL, SeriesStatus.from("final"))
        assertEquals(SeriesStatus.FINAL, SeriesStatus.from("BÜYÜK FİNAL"))
        assertEquals(SeriesStatus.FINAL, SeriesStatus.from("SEZON FİNALİ"))
        assertEquals(SeriesStatus.FINAL, SeriesStatus.from("SEZON FINALI"))
        assertEquals(SeriesStatus.FINAL, SeriesStatus.from("VEDA FİNALİ"))

        // Bu Hafta Yok combinations
        assertEquals(SeriesStatus.BU_HAFTA_YOK, SeriesStatus.from("Bu hafta yok"))
        assertEquals(SeriesStatus.BU_HAFTA_YOK, SeriesStatus.from("BU HAFTA YOK"))
        assertEquals(SeriesStatus.BU_HAFTA_YOK, SeriesStatus.from("bu hafta yok"))
        assertEquals(SeriesStatus.BU_HAFTA_YOK, SeriesStatus.from("BU HAFTA YOKTUR"))
        assertEquals(SeriesStatus.BU_HAFTA_YOK, SeriesStatus.from("YAYINLANMAYACAK"))
        assertEquals(SeriesStatus.BU_HAFTA_YOK, SeriesStatus.from("YAYIN DIŞI"))
        assertEquals(SeriesStatus.BU_HAFTA_YOK, SeriesStatus.from("TEKRAR BÖLÜMÜ"))
        assertEquals(SeriesStatus.BU_HAFTA_YOK, SeriesStatus.from("ÖZET"))
        assertEquals(SeriesStatus.BU_HAFTA_YOK, SeriesStatus.from("ARA VERDİ"))

        // Unknown / Empty fallback
        assertEquals(SeriesStatus.UNKNOWN, SeriesStatus.from(null))
        assertEquals(SeriesStatus.UNKNOWN, SeriesStatus.from(""))
        assertEquals(SeriesStatus.UNKNOWN, SeriesStatus.from("   "))
    }
}
