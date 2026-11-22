package com.ugurbuga.inspecto.ui.previews

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.ugurbuga.inspecto.InspectoListDarkPreview
import com.ugurbuga.inspecto.InspectoListLightPreview
import com.ugurbuga.inspecto.ui.bottomsheets.FilterBottomSheetPreview
import com.ugurbuga.inspecto.ui.bottomsheets.SortBottomSheetPreview
import com.ugurbuga.inspecto.ui.components.HeaderStatsTablePreview
import com.ugurbuga.inspecto.ui.components.TransactionCardGet200Preview
import com.ugurbuga.inspecto.ui.detail.DetailRowPreview
import com.ugurbuga.inspecto.ui.detail.HeadersAndBodyTabContentPreview
import com.ugurbuga.inspecto.ui.detail.InspectoDetailDarkPreview
import com.ugurbuga.inspecto.ui.detail.InspectoDetailLightPreview
import com.ugurbuga.inspecto.ui.detail.OverviewTabContentPreview

@Preview(name = "Inspecto List - Light")
@Composable
internal fun InspectoPreviews_InspectoListLightPreview() {
    InspectoListLightPreview()
}

@Preview(name = "Inspecto List - Dark")
@Composable
internal fun InspectoPreviews_InspectoListDarkPreview() {
    InspectoListDarkPreview()
}

@Preview(name = "Inspecto Detail - Light")
@Composable
internal fun InspectoPreviews_InspectoDetailLightPreview() {
    InspectoDetailLightPreview()
}

@Preview(name = "Inspecto Detail - Dark")
@Composable
internal fun InspectoPreviews_InspectoDetailDarkPreview() {
    InspectoDetailDarkPreview()
}

@Preview(name = "Transaction Card - Light")
@Composable
internal fun InspectoPreviews_TransactionCardPreview() {
    TransactionCardGet200Preview()
}

@Preview(name = "Header Stats Table - Light")
@Composable
internal fun InspectoPreviews_HeaderStatsTablePreview() {
    HeaderStatsTablePreview()
}

@Preview(name = "Filter Bottom Sheet - Light")
@Composable
internal fun InspectoPreviews_FilterBottomSheetPreview() {
    FilterBottomSheetPreview()
}

@Preview(name = "Sort Bottom Sheet - Light")
@Composable
internal fun InspectoPreviews_SortBottomSheetPreview() {
    SortBottomSheetPreview()
}

@Preview(name = "Detail Row - Light")
@Composable
internal fun InspectoPreviews_DetailRowPreview() {
    DetailRowPreview()
}

@Preview(name = "Overview Tab Content - Light")
@Composable
internal fun InspectoPreviews_OverviewTabContentPreview() {
    OverviewTabContentPreview()
}

@Preview(name = "Headers And Body Tab Content - Light")
@Composable
internal fun InspectoPreviews_HeadersAndBodyTabContentPreview() {
    HeadersAndBodyTabContentPreview()
}
