package com.contacts.android.contacts.data.mapper

import com.contacts.android.contacts.data.local.entity.*
import com.contacts.android.contacts.data.local.relation.GroupWithContactCount
import com.contacts.android.contacts.domain.model.*

fun ContactWithDetails.toDomain(): Contact {
    return Contact(
        id = contact.id,
        prefix = contact.prefix,
        firstName = contact.firstName,
        middleName = contact.middleName,
        lastName = contact.lastName,
        suffix = contact.suffix,
        nickname = contact.nickname,
        photoUri = contact.photoUri,
        phoneNumbers = phoneNumbers.map { it.toDomain() },
        emails = emails.map { it.toDomain() },
        addresses = addresses.map { it.toDomain() },
        organization = contact.organization,
        title = contact.title,
        notes = contact.notes,
        birthday = contact.birthday,
        ringtone = contact.ringtone,
        isFavorite = contact.isFavorite,
        groups = groups.map { it.toDomain() },
        source = contact.source,
        accountName = contact.accountName,
        accountType = contact.accountType,
        createdAt = contact.createdAt,
        updatedAt = contact.updatedAt
    )
}

fun Contact.toEntity(): ContactEntity {
    return ContactEntity(
        id = id,
        prefix = prefix,
        firstName = firstName,
        middleName = middleName,
        lastName = lastName,
        suffix = suffix,
        nickname = nickname,
        photoUri = photoUri,
        organization = organization,
        title = title,
        notes = notes,
        birthday = birthday,
        ringtone = ringtone,
        isFavorite = isFavorite,
        source = source,
        accountName = accountName,
        accountType = accountType,
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
        createdAt = createdAt,
        isSystemGroup = isSystemGroup,
        systemId = systemId,
        accountName = accountName,
        accountType = accountType
    )
}

fun GroupWithContactCount.toDomain(): Group {
    return Group(
        id = id,
        name = name,
        contactCount = contactCount,
        createdAt = createdAt,
        isSystemGroup = isSystemGroup,
        systemId = systemId,
        accountName = accountName,
        accountType = accountType
    )
}

fun Group.toEntity(): GroupEntity {
    return GroupEntity(
        id = id,
        name = name,
        createdAt = createdAt,
        isSystemGroup = isSystemGroup,
        systemId = systemId,
        accountName = accountName,
        accountType = accountType
    )
}
