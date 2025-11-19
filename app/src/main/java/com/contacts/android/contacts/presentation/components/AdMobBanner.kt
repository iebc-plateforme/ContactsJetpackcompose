package com.contacts.android.contacts.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.contacts.android.contacts.ads.AdMobManager
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

/**
 * AdMob Banner Ad Component for Jetpack Compose
 * Non-intrusive placement at list footers
 */
@Composable
fun AdMobBanner(
    modifier: Modifier = Modifier,
    adUnitId: String = AdMobManager.BANNER_AD_UNIT_ID
) {
    val isInPreview = LocalInspectionMode.current
    val context = androidx.compose.ui.platform.LocalContext.current

    if (isInPreview) {
        // Show placeholder in preview
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(50.dp),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.material3.Text("Ad Banner Placeholder")
        }
    } else {
        AndroidView(
            modifier = modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            factory = { ctx ->
                AdView(ctx).apply {
                    setAdSize(AdSize.BANNER)
                    this.adUnitId = adUnitId
                    loadAd(AdMobManager(context).createAdRequest())
                }
            }
        )
    }
}

/**
 * Smart Banner that adapts to screen size
 */
@Composable
fun AdMobSmartBanner(
    modifier: Modifier = Modifier,
    adUnitId: String = AdMobManager.BANNER_AD_UNIT_ID
) {
    val isInPreview = LocalInspectionMode.current
    val context = androidx.compose.ui.platform.LocalContext.current

    if (isInPreview) {
        // Show placeholder in preview
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(50.dp),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.material3.Text("Smart Banner Placeholder")
        }
    } else {
        AndroidView(
            modifier = modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            factory = { ctx ->
                AdView(ctx).apply {
                    setAdSize(AdSize.SMART_BANNER)
                    this.adUnitId = adUnitId
                    loadAd(AdMobManager(context).createAdRequest())
                }
            }
        )
    }
}
