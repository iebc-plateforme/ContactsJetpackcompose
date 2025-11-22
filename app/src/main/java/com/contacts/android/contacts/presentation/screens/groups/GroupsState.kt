package com.contacts.android.contacts.presentation.screens.groups

import com.contacts.android.contacts.domain.model.Contact
import com.contacts.android.contacts.domain.model.Group

data class GroupsState(
    // Liste des groupes
    val groups: List<Group> = emptyList(),
    val filteredGroups: List<Group> = emptyList(),
    val searchQuery: String = "", // Recherche de groupes

    // État de chargement et messages
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,

    // Dialogues
    val showAddGroupDialog: Boolean = false,
    val showEditGroupDialog: Boolean = false,
    val showDeleteDialog: Boolean = false,
    val showContactSelectionDialog: Boolean = false,

    // Données en cours d'édition
    val selectedGroup: Group? = null,
    val groupNameInput: String = "",

    // Sélection de contacts (Amélioré avec recherche)
    val availableContacts: List<Contact> = emptyList(), // Tous les contacts possibles
    val filteredAvailableContacts: List<Contact> = emptyList(), // Contacts filtrés par la recherche
    val contactSearchQuery: String = "", // Recherche DANS le dialogue de sélection
    val selectedContactIds: Set<Long> = emptySet()
)