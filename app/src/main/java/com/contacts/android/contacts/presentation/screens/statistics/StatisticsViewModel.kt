package com.contacts.android.contacts.presentation.screens.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.contacts.android.contacts.domain.repository.ContactRepository
import com.contacts.android.contacts.domain.repository.GroupRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val contactRepository: ContactRepository,
    private val groupRepository: GroupRepository
) : ViewModel() {

    val statistics: StateFlow<ContactStatistics> = combine(
        contactRepository.getAllContacts(),
        groupRepository.getAllGroups()
    ) { contacts, groups ->
        ContactStatistics(
            totalContacts = contacts.size,
            favoritesCount = contacts.count { it.isFavorite },
            groupsCount = groups.size,
            contactsWithPhone = contacts.count { it.phoneNumbers.isNotEmpty() },
            contactsWithEmail = contacts.count { it.emails.isNotEmpty() },
            contactsWithAddress = contacts.count { it.addresses.isNotEmpty() },
            contactsWithBirthday = contacts.count { it.events.any { event -> event.type == com.contacts.android.contacts.domain.model.EventType.BIRTHDAY } },
            contactsWithOrganization = contacts.count { !it.organization.isNullOrBlank() },
            accountDistribution = contacts.groupingBy { it.accountName ?: "" }
                .eachCount()
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ContactStatistics()
    )
}

data class ContactStatistics(
    val totalContacts: Int = 0,
    val favoritesCount: Int = 0,
    val groupsCount: Int = 0,
    val contactsWithPhone: Int = 0,
    val contactsWithEmail: Int = 0,
    val contactsWithAddress: Int = 0,
    val contactsWithBirthday: Int = 0,
    val contactsWithOrganization: Int = 0,
    val accountDistribution: Map<String, Int> = emptyMap()
)
