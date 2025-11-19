package com.contacts.android.contacts.presentation.screens.groupdetail

sealed class GroupDetailEvent {
    data class LoadGroup(val groupId: Long) : GroupDetailEvent()
    data class RemoveContactFromGroup(val contactId: Long) : GroupDetailEvent()
    data class AddContactsToGroup(val contactIds: List<Long>) : GroupDetailEvent()
    data class UpdateGroupName(val newName: String) : GroupDetailEvent()
    object DeleteGroup : GroupDetailEvent()
    object ShowAddContactsDialog : GroupDetailEvent()
    object HideAddContactsDialog : GroupDetailEvent()
    object ShowDeleteGroupDialog : GroupDetailEvent()
    object HideDeleteGroupDialog : GroupDetailEvent()
    object ShowEditGroupDialog : GroupDetailEvent()
    object HideEditGroupDialog : GroupDetailEvent()
    object ClearError : GroupDetailEvent()
}
