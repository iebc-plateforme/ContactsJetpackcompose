package com.contacts.android.contacts.presentation.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.contacts.android.contacts.R
import com.contacts.android.contacts.domain.model.*

/**
 * Extension functions to get localized display strings for domain model enums
 */

@Composable
fun AddressType.toLocalizedString(): String = when (this) {
    AddressType.HOME -> stringResource(R.string.type_home)
    AddressType.WORK -> stringResource(R.string.type_work)
    AddressType.OTHER -> stringResource(R.string.type_other)
    AddressType.CUSTOM -> stringResource(R.string.type_custom)
}

@Composable
fun PhoneType.toLocalizedString(): String = when (this) {
    PhoneType.MOBILE -> stringResource(R.string.type_mobile)
    PhoneType.HOME -> stringResource(R.string.type_home)
    PhoneType.WORK -> stringResource(R.string.type_work)
    PhoneType.FAX -> stringResource(R.string.type_fax)
    PhoneType.PAGER -> stringResource(R.string.type_pager)
    PhoneType.OTHER -> stringResource(R.string.type_other)
    PhoneType.CUSTOM -> stringResource(R.string.type_custom)
}

@Composable
fun EmailType.toLocalizedString(): String = when (this) {
    EmailType.HOME -> stringResource(R.string.type_home)
    EmailType.WORK -> stringResource(R.string.type_work)
    EmailType.OTHER -> stringResource(R.string.type_other)
    EmailType.CUSTOM -> stringResource(R.string.type_custom)
}
