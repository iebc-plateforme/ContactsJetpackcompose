package com.contacts.android.contactsjetpackcompose.data.mapper

import com.contacts.android.contactsjetpackcompose.data.local.entity.*
import com.contacts.android.contactsjetpackcompose.data.local.relation.GroupWithContactCount
import com.contacts.android.contactsjetpackcompose.domain.model.*

fun ContactWithDetails.toDomain(): Contact {
    return Contact(
        id = contact.id,
        firstName = contact.firstName,
        lastName = contact.lastName,
        photoUri = contact.photoUri,
        phoneNumbers = phoneNumbers.map { it.toDomain() },
        emails = emails.map { it.toDomain() },
        addresses = addresses.map { it.toDomain() },
        organization = contact.organization,
        title = contact.title,
        notes = contact.notes,
        isFavorite = contact.isFavorite,
        groups = groups.map { it.toDomain() },
        createdAt = contact.createdAt,
        updatedAt = contact.updatedAt
    )
}

fun Contact.toEntity(): ContactEntity {
    return ContactEntity(
        id = id,
        firstName = firstName,
        lastName = lastName,
        photoUri = photoUri,
        organization = organization,
        title = title,
        notes = notes,
        isFavorite = isFavorite,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun PhoneNumberEntity.toDomain(): PhoneNumber {
    return PhoneNumber(
        id = id,
        number = number,
        type = type,
        label = label
    )
}

fun PhoneNumber.toEntity(contactId: Long): PhoneNumberEntity {
    return PhoneNumberEntity(
        id = id,
        contactId = contactId,
        number = number,
        type = type,
        label = label
    )
}

fun EmailEntity.toDomain(): Email {
    return Email(
        id = id,
        email = email,
        type = type,
        label = label
    )
}

fun Email.toEntity(contactId: Long): EmailEntity {
    return EmailEntity(
        id = id,
        contactId = contactId,
        email = email,
        type = type,
        label = label
    )
}

fun AddressEntity.toDomain(): Address {
    return Address(
        id = id,
        street = street,
        city = city,
        state = state,
        postalCode = postalCode,
        country = country,
        type = type,
        label = label
    )
}

fun Address.toEntity(contactId: Long): AddressEntity {
    return AddressEntity(
        id = id,
        contactId = contactId,
        street = street,
        city = city,
        state = state,
        postalCode = postalCode,
        country = country,
        type = type,
        label = label
    )
}

fun GroupEntity.toDomain(contactCount: Int = 0): Group {
    return Group(
        id = id,
        name = name,
        contactCount = contactCount,
        createdAt = createdAt
    )
}

fun GroupWithContactCount.toDomain(): Group {
    return Group(
        id = id,
        name = name,
        contactCount = contactCount,
        createdAt = createdAt
    )
}

fun Group.toEntity(): GroupEntity {
    return GroupEntity(
        id = id,
        name = name,
        createdAt = createdAt
    )
}
