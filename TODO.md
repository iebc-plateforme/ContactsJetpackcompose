# Contacts App - Complete Development TODO

## Phase 1: Project Setup & Configuration

### 1.1 Initial Setup
- [ ] Create new Android Studio project with "Empty Activity (Compose)"
- [ ] Set minimum SDK to 24 (Android 7.0)
- [ ] Target SDK to 34 (Android 14)
- [ ] Configure project name and package (e.g., `com.yourname.contacts`)
- [ ] Initialize Git repository
- [ ] Create `.gitignore` file

### 1.2 Gradle Configuration
- [ ] Update `libs.versions.toml` with all dependencies
  - [ ] Compose BOM (latest stable)
  - [ ] Material3
  - [ ] Navigation Compose
  - [ ] Hilt 2.50+
  - [ ] Room 2.6.1+
  - [ ] Kotlin Coroutines 1.8.0+
  - [ ] Lifecycle Runtime Compose
  - [ ] Coil Compose 2.5.0+
  - [ ] DataStore Preferences 1.0.0+
  - [ ] Accompanist Permissions
  - [ ] JUnit, Mockk, Turbine (testing)
- [ ] Configure `build.gradle.kts` (project level)
  - [ ] Add Hilt plugin
  - [ ] Add KSP plugin
  - [ ] Add Kotlin serialization plugin
- [ ] Configure `build.gradle.kts` (app level)
  - [ ] Apply plugins (Hilt, KSP, Kotlin)
  - [ ] Configure compose options
  - [ ] Configure build features
  - [ ] Add all dependencies
  - [ ] Configure packaging options
- [ ] Sync Gradle and resolve any issues

### 1.3 Android Manifest Configuration
- [ ] Add required permissions
  - [ ] `READ_CONTACTS`
  - [ ] `WRITE_CONTACTS`
  - [ ] `READ_EXTERNAL_STORAGE`
  - [ ] `WRITE_EXTERNAL_STORAGE`
- [ ] Configure application name and icon
- [ ] Add FileProvider for Android 11+ file access
- [ ] Configure backup rules

### 1.4 ProGuard/R8 Configuration
- [ ] Create `proguard-rules.pro` with rules for:
  - [ ] Room
  - [ ] Hilt
  - [ ] Kotlin Coroutines
  - [ ] Serialization

---

## Phase 2: Project Structure Setup

### 2.1 Create Package Structure
```
app/src/main/java/com/yourname/contacts/
├── data/
│   ├── local/
│   │   ├── dao/
│   │   ├── entity/
│   │   └── database/
│   ├── repository/
│   ├── mapper/
│   └── preferences/
├── domain/
│   ├── model/
│   ├── repository/
│   └── usecase/
├── presentation/
│   ├── screens/
│   │   ├── contactlist/
│   │   ├── contactdetail/
│   │   ├── editcontact/
│   │   ├── groups/
│   │   └── settings/
│   ├── components/
│   ├── navigation/
│   ├── theme/
│   └── util/
├── di/
└── util/
```

- [ ] Create all package directories
- [ ] Create placeholder `.gitkeep` files if needed

### 2.2 Setup Dependency Injection
- [ ] Create `di/` package
- [ ] Create `AppModule.kt` for app-level dependencies
- [ ] Create `DatabaseModule.kt` for Room database
- [ ] Create `RepositoryModule.kt` for repositories
- [ ] Create `UseCaseModule.kt` for use cases
- [ ] Create `@HiltAndroidApp` application class
- [ ] Update AndroidManifest.xml with custom Application class

---

## Phase 3: Data Layer Implementation

### 3.1 Define Enums and Constants
- [ ] Create `domain/model/PhoneType.kt`
  ```kotlin
  enum class PhoneType { MOBILE, HOME, WORK, FAX, PAGER, OTHER, CUSTOM }
  ```
- [ ] Create `domain/model/EmailType.kt`
  ```kotlin
  enum class EmailType { HOME, WORK, OTHER, CUSTOM }
  ```
- [ ] Create `domain/model/AddressType.kt`
  ```kotlin
  enum class AddressType { HOME, WORK, OTHER, CUSTOM }
  ```
- [ ] Create `util/Constants.kt` for database name, preferences keys, etc.

### 3.2 Create Room Entities
- [ ] Create `data/local/entity/ContactEntity.kt`
  - [ ] Add all fields (id, firstName, lastName, photoUri, isFavorite, etc.)
  - [ ] Add timestamps (createdAt, updatedAt)
  - [ ] Add @Entity annotation with table name
- [ ] Create `data/local/entity/PhoneNumberEntity.kt`
  - [ ] Add fields (id, contactId, number, type, label)
  - [ ] Add foreign key relationship to ContactEntity
- [ ] Create `data/local/entity/EmailEntity.kt`
  - [ ] Add fields (id, contactId, email, type, label)
  - [ ] Add foreign key relationship
- [ ] Create `data/local/entity/AddressEntity.kt`
  - [ ] Add fields (id, contactId, street, city, state, postalCode, country, type)
  - [ ] Add foreign key relationship
- [ ] Create `data/local/entity/GroupEntity.kt`
  - [ ] Add fields (id, name, createdAt)
- [ ] Create `data/local/entity/ContactGroupCrossRef.kt`
  - [ ] Many-to-many relationship between Contact and Group
- [ ] Create `data/local/entity/ContactWithDetails.kt`
  - [ ] Relation class with @Embedded ContactEntity
  - [ ] @Relation for phoneNumbers, emails, addresses, groups

### 3.3 Create Type Converters
- [ ] Create `data/local/converter/TypeConverters.kt`
  - [ ] Add converters for PhoneType, EmailType, AddressType
  - [ ] Add date/time converters if needed

### 3.4 Create DAOs
- [ ] Create `data/local/dao/ContactDao.kt`
  - [ ] `getAllContactsFlow(): Flow<List<ContactWithDetails>>`
  - [ ] `getContactById(id: Long): ContactWithDetails?`
  - [ ] `insertContact(contact: ContactEntity): Long`
  - [ ] `updateContact(contact: ContactEntity)`
  - [ ] `deleteContact(contact: ContactEntity)`
  - [ ] `getFavoriteContacts(): Flow<List<ContactWithDetails>>`
  - [ ] `searchContacts(query: String): Flow<List<ContactWithDetails>>`
  - [ ] `toggleFavorite(id: Long, isFavorite: Boolean)`
- [ ] Create `data/local/dao/PhoneNumberDao.kt`
  - [ ] `insertPhoneNumber(phone: PhoneNumberEntity): Long`
  - [ ] `insertPhoneNumbers(phones: List<PhoneNumberEntity>)`
  - [ ] `updatePhoneNumber(phone: PhoneNumberEntity)`
  - [ ] `deletePhoneNumber(phone: PhoneNumberEntity)`
  - [ ] `deletePhoneNumbersByContactId(contactId: Long)`
  - [ ] `getPhoneNumbersByContactId(contactId: Long): List<PhoneNumberEntity>`
- [ ] Create `data/local/dao/EmailDao.kt`
  - [ ] Similar methods for emails
- [ ] Create `data/local/dao/AddressDao.kt`
  - [ ] Similar methods for addresses
- [ ] Create `data/local/dao/GroupDao.kt`
  - [ ] `getAllGroups(): Flow<List<GroupEntity>>`
  - [ ] `getGroupById(id: Long): GroupEntity?`
  - [ ] `insertGroup(group: GroupEntity): Long`
  - [ ] `updateGroup(group: GroupEntity)`
  - [ ] `deleteGroup(group: GroupEntity)`
  - [ ] `getGroupsForContact(contactId: Long): List<GroupEntity>`
- [ ] Create `data/local/dao/ContactGroupDao.kt`
  - [ ] `insertContactGroup(crossRef: ContactGroupCrossRef)`
  - [ ] `deleteContactGroup(crossRef: ContactGroupCrossRef)`
  - [ ] `getContactsByGroupId(groupId: Long): List<ContactWithDetails>`
  - [ ] `deleteContactGroupsByContactId(contactId: Long)`

### 3.5 Create Database
- [ ] Create `data/local/database/ContactsDatabase.kt`
  - [ ] Extend RoomDatabase
  - [ ] Add @Database annotation with all entities
  - [ ] Set version = 1
  - [ ] Add abstract functions for all DAOs
  - [ ] Add TypeConverters annotation
  - [ ] Implement database callback for prepopulating if needed

### 3.6 Create Domain Models
- [ ] Create `domain/model/Contact.kt`
  - [ ] All contact fields
  - [ ] Computed property `displayName`
  - [ ] Computed property `initials`
  - [ ] Lists for phoneNumbers, emails, addresses, groups
- [ ] Create `domain/model/PhoneNumber.kt`
- [ ] Create `domain/model/Email.kt`
- [ ] Create `domain/model/Address.kt`
  - [ ] Computed property `fullAddress`
- [ ] Create `domain/model/Group.kt`
  - [ ] Add `contactCount` property

### 3.7 Create Mappers
- [ ] Create `data/mapper/ContactMapper.kt`
  - [ ] `ContactEntity.toDomain(): Contact`
  - [ ] `Contact.toEntity(): ContactEntity`
  - [ ] `ContactWithDetails.toDomain(): Contact`
- [ ] Create `data/mapper/PhoneNumberMapper.kt`
  - [ ] Bidirectional mapping functions
- [ ] Create `data/mapper/EmailMapper.kt`
  - [ ] Bidirectional mapping functions
- [ ] Create `data/mapper/AddressMapper.kt`
  - [ ] Bidirectional mapping functions
- [ ] Create `data/mapper/GroupMapper.kt`
  - [ ] Bidirectional mapping functions

### 3.8 Create Repository Interfaces
- [ ] Create `domain/repository/ContactRepository.kt` interface
  - [ ] Define all CRUD methods
  - [ ] Define search and filter methods
- [ ] Create `domain/repository/GroupRepository.kt` interface
  - [ ] Define group management methods

### 3.9 Create Repository Implementations
- [ ] Create `data/repository/ContactRepositoryImpl.kt`
  - [ ] Inject DAOs and mappers
  - [ ] Implement all interface methods
  - [ ] Handle transactions for complex operations
  - [ ] Map between entities and domain models
- [ ] Create `data/repository/GroupRepositoryImpl.kt`
  - [ ] Implement group operations
  - [ ] Handle contact-group relationships

### 3.10 Setup DataStore for Preferences
- [ ] Create `data/preferences/UserPreferences.kt`
  - [ ] Define preference keys (theme, sort order, etc.)
  - [ ] Create preference data class
- [ ] Create `data/preferences/UserPreferencesRepository.kt`
  - [ ] Implement DataStore operations
  - [ ] Expose Flow<UserPreferences>
  - [ ] Methods to update preferences

---

## Phase 4: Domain Layer Implementation

### 4.1 Create Use Cases - Contact Operations
- [ ] Create `domain/usecase/contact/GetAllContactsUseCase.kt`
- [ ] Create `domain/usecase/contact/GetContactByIdUseCase.kt`
- [ ] Create `domain/usecase/contact/SaveContactUseCase.kt`
  - [ ] Handle both insert and update
  - [ ] Include validation logic
- [ ] Create `domain/usecase/contact/DeleteContactUseCase.kt`
- [ ] Create `domain/usecase/contact/DeleteMultipleContactsUseCase.kt`
- [ ] Create `domain/usecase/contact/SearchContactsUseCase.kt`
- [ ] Create `domain/usecase/contact/GetFavoriteContactsUseCase.kt`
- [ ] Create `domain/usecase/contact/ToggleFavoriteUseCase.kt`
- [ ] Create `domain/usecase/contact/GetContactsCountUseCase.kt`

### 4.2 Create Use Cases - Group Operations
- [ ] Create `domain/usecase/group/GetAllGroupsUseCase.kt`
- [ ] Create `domain/usecase/group/GetGroupByIdUseCase.kt`
- [ ] Create `domain/usecase/group/CreateGroupUseCase.kt`
- [ ] Create `domain/usecase/group/UpdateGroupUseCase.kt`
- [ ] Create `domain/usecase/group/DeleteGroupUseCase.kt`
- [ ] Create `domain/usecase/group/AddContactToGroupUseCase.kt`
- [ ] Create `domain/usecase/group/RemoveContactFromGroupUseCase.kt`
- [ ] Create `domain/usecase/group/GetContactsByGroupUseCase.kt`

### 4.3 Create Use Cases - Import/Export
- [ ] Create VCF helper class `data/vcf/VcfParser.kt`
  - [ ] Parse VCF 2.1 and 3.0 format
  - [ ] Extract contact fields
- [ ] Create VCF builder class `data/vcf/VcfBuilder.kt`
  - [ ] Build VCF 3.0 format
  - [ ] Support all contact fields
- [ ] Create `domain/usecase/vcf/ExportContactsToVcfUseCase.kt`
  - [ ] Accept list of contacts and output URI
  - [ ] Use VcfBuilder to generate VCF content
  - [ ] Write to URI using ContentResolver
- [ ] Create `domain/usecase/vcf/ImportContactsFromVcfUseCase.kt`
  - [ ] Accept input URI
  - [ ] Parse VCF file
  - [ ] Convert to domain models
  - [ ] Save to database
- [ ] Create `domain/usecase/vcf/ExportSingleContactUseCase.kt`

### 4.4 Create Use Cases - Validation
- [ ] Create `domain/usecase/validation/ValidateContactUseCase.kt`
  - [ ] Validate name is not empty
  - [ ] Validate at least one phone/email exists
  - [ ] Validate phone number format
  - [ ] Validate email format
- [ ] Create `domain/usecase/validation/ValidatePhoneNumberUseCase.kt`
- [ ] Create `domain/usecase/validation/ValidateEmailUseCase.kt`

---

## Phase 5: Presentation Layer - Theme & Components

### 5.1 Setup Material3 Theme
- [ ] Create `presentation/theme/Color.kt`
  - [ ] Define light color scheme
  - [ ] Define dark color scheme
  - [ ] Define custom colors if needed
- [ ] Create `presentation/theme/Typography.kt`
  - [ ] Define Material3 typography scale
  - [ ] Use appropriate fonts
- [ ] Create `presentation/theme/Shape.kt`
  - [ ] Define shape schema
- [ ] Create `presentation/theme/Theme.kt`
  - [ ] Create ContactsTheme composable
  - [ ] Support dynamic color (Android 12+)
  - [ ] Support dark/light theme
  - [ ] Apply color scheme, typography, shapes

### 5.2 Create Common UI Components
- [ ] Create `presentation/components/ContactAvatar.kt`
  - [ ] Show photo if available
  - [ ] Show initials in colored circle if no photo
  - [ ] Different sizes (small, medium, large)
- [ ] Create `presentation/components/EmptyState.kt`
  - [ ] Generic empty state with icon and message
- [ ] Create `presentation/components/LoadingIndicator.kt`
  - [ ] Full-screen loading indicator
- [ ] Create `presentation/components/ErrorMessage.kt`
  - [ ] Error display with retry option
- [ ] Create `presentation/components/SectionHeader.kt`
  - [ ] Section title with divider
- [ ] Create `presentation/components/ContactListItem.kt`
  - [ ] Avatar, name, phone preview
  - [ ] Favorite indicator
  - [ ] Swipe actions (delete, favorite)
- [ ] Create `presentation/components/SearchBar.kt`
  - [ ] Material3 search bar
  - [ ] Clear button
  - [ ] Search icon
- [ ] Create `presentation/components/ConfirmationDialog.kt`
  - [ ] Generic confirmation dialog
  - [ ] Customizable title, message, actions
- [ ] Create `presentation/components/InputDialog.kt`
  - [ ] Dialog with text input
  - [ ] For renaming groups, etc.

### 5.3 Create Contact Detail Components
- [ ] Create `presentation/components/ContactHeader.kt`
  - [ ] Large avatar
  - [ ] Full name
  - [ ] Quick action buttons (call, message, email)
- [ ] Create `presentation/components/ContactInfoItem.kt`
  - [ ] Icon + label + value
  - [ ] Action button (call, message, map)
- [ ] Create `presentation/components/PhoneNumberItem.kt`
  - [ ] Phone number display with type
  - [ ] Call and message actions
- [ ] Create `presentation/components/EmailItem.kt`
  - [ ] Email display with type
  - [ ] Email action
- [ ] Create `presentation/components/AddressItem.kt`
  - [ ] Full address display
  - [ ] Map action
- [ ] Create `presentation/components/GroupChips.kt`
  - [ ] Horizontal chip row for groups

### 5.4 Create Edit Contact Components
- [ ] Create `presentation/components/PhotoPicker.kt`
  - [ ] Show current photo or placeholder
  - [ ] Camera and gallery options
  - [ ] Remove photo option
- [ ] Create `presentation/components/PhoneNumberField.kt`
  - [ ] Text field for phone number
  - [ ] Type dropdown
  - [ ] Remove button
- [ ] Create `presentation/components/EmailField.kt`
  - [ ] Text field for email
  - [ ] Type dropdown
  - [ ] Remove button
- [ ] Create `presentation/components/AddressField.kt`
  - [ ] Multiple text fields (street, city, state, zip, country)
  - [ ] Type dropdown
  - [ ] Remove button
- [ ] Create `presentation/components/AddFieldButton.kt`
  - [ ] Button to add new phone/email/address
- [ ] Create `presentation/components/GroupSelector.kt`
  - [ ] Multi-select dialog for groups
  - [ ] Create new group option

---

## Phase 6: Presentation Layer - Screens & Navigation

### 6.1 Setup Navigation
- [ ] Create `presentation/navigation/Screen.kt` sealed class
  - [ ] Define all screen routes
  - [ ] Define navigation arguments
  - [ ] Helper functions for route creation
- [ ] Create `presentation/navigation/ContactsNavGraph.kt`
  - [ ] Setup NavHost
  - [ ] Define all composable destinations
  - [ ] Configure navigation animations
  - [ ] Pass navigation callbacks

### 6.2 Contact List Screen
- [ ] Create `presentation/screens/contactlist/ContactListState.kt`
  - [ ] Define UI state data class
- [ ] Create `presentation/screens/contactlist/ContactListEvent.kt`
  - [ ] Define user events (sealed class)
- [ ] Create `presentation/screens/contactlist/ContactListViewModel.kt`
  - [ ] Inject use cases
  - [ ] Implement state management
  - [ ] Implement search functionality
  - [ ] Implement sorting
  - [ ] Implement delete functionality
  - [ ] Handle favorites toggle
- [ ] Create `presentation/screens/contactlist/ContactListScreen.kt`
  - [ ] Scaffold with TopAppBar
  - [ ] Search bar
  - [ ] Floating action button (add contact)
  - [ ] LazyColumn with sections
  - [ ] Favorites section (if any)
  - [ ] Grouped contacts by first letter
  - [ ] Fast scroll support
  - [ ] Pull to refresh
  - [ ] Empty state
  - [ ] Loading state
  - [ ] Error state
- [ ] Create `presentation/screens/contactlist/ContactListTopBar.kt`
  - [ ] Title
  - [ ] Search icon
  - [ ] Menu (settings, import/export, select all)
- [ ] Create `presentation/screens/contactlist/components/ContactListItemSwipeable.kt`
  - [ ] Swipe to delete
  - [ ] Swipe to favorite

### 6.3 Contact Detail Screen
- [ ] Create `presentation/screens/contactdetail/ContactDetailState.kt`
- [ ] Create `presentation/screens/contactdetail/ContactDetailEvent.kt`
- [ ] Create `presentation/screens/contactdetail/ContactDetailViewModel.kt`
  - [ ] Load contact by ID
  - [ ] Handle delete
  - [ ] Handle toggle favorite
  - [ ] Handle share contact
- [ ] Create `presentation/screens/contactdetail/ContactDetailScreen.kt`
  - [ ] TopAppBar with back, edit, more actions
  - [ ] Contact header with photo and name
  - [ ] Phone numbers section
  - [ ] Emails section
  - [ ] Addresses section
  - [ ] Groups section
  - [ ] Notes section
  - [ ] Quick actions (call, message, email)
  - [ ] Handle contact deletion with confirmation
- [ ] Create `presentation/screens/contactdetail/ContactDetailTopBar.kt`
- [ ] Create `presentation/screens/contactdetail/components/QuickActionBar.kt`
  - [ ] Call, message, email, video call buttons

### 6.4 Edit Contact Screen
- [ ] Create `presentation/screens/editcontact/EditContactState.kt`
  - [ ] All editable fields
  - [ ] Validation state
  - [ ] Loading state
- [ ] Create `presentation/screens/editcontact/EditContactEvent.kt`
- [ ] Create `presentation/screens/editcontact/EditContactViewModel.kt`
  - [ ] Load contact if editing
  - [ ] Initialize empty state if creating
  - [ ] Handle field changes
  - [ ] Add/remove phone numbers
  - [ ] Add/remove emails
  - [ ] Add/remove addresses
  - [ ] Select groups
  - [ ] Validate form
  - [ ] Save contact (insert or update)
  - [ ] Handle photo selection
- [ ] Create `presentation/screens/editcontact/EditContactScreen.kt`
  - [ ] TopAppBar with cancel and save
  - [ ] Photo picker
  - [ ] Name fields (first, middle, last)
  - [ ] Phone numbers section (dynamic list)
  - [ ] Emails section (dynamic list)
  - [ ] Addresses section (dynamic list)
  - [ ] Organization field
  - [ ] Birthday field (date picker)
  - [ ] Notes field
  - [ ] Groups selection
  - [ ] Validation errors display
  - [ ] Loading state on save
- [ ] Implement photo picker functionality
  - [ ] Camera permission handling
  - [ ] Storage permission handling
  - [ ] Image cropping
  - [ ] Image compression

### 6.5 Groups Screen
- [ ] Create `presentation/screens/groups/GroupListState.kt`
- [ ] Create `presentation/screens/groups/GroupListEvent.kt`
- [ ] Create `presentation/screens/groups/GroupListViewModel.kt`
  - [ ] Load all groups
  - [ ] Create new group
  - [ ] Rename group
  - [ ] Delete group
  - [ ] Get contact count per group
- [ ] Create `presentation/screens/groups/GroupListScreen.kt`
  - [ ] TopAppBar with back and add group
  - [ ] List of groups with contact count
  - [ ] Long press to rename/delete
  - [ ] Click to view group details
  - [ ] Empty state for no groups
- [ ] Create `presentation/screens/groups/GroupDetailScreen.kt`
  - [ ] Group name
  - [ ] List of contacts in group
  - [ ] Remove contact from group
  - [ ] Add contacts to group
  - [ ] Batch actions (send SMS, email to all)

### 6.6 Settings Screen
- [ ] Create `presentation/screens/settings/SettingsState.kt`
- [ ] Create `presentation/screens/settings/SettingsViewModel.kt`
  - [ ] Load current preferences
  - [ ] Update theme preference
  - [ ] Update sort preference
  - [ ] Update display preferences
- [ ] Create `presentation/screens/settings/SettingsScreen.kt`
  - [ ] TopAppBar with back button
  - [ ] Theme selection (Light/Dark/System)
  - [ ] Dynamic color toggle (Android 12+)
  - [ ] Sort order preference
  - [ ] Display preferences (show/hide fields)
  - [ ] Import/Export section
  - [ ] About section (version, licenses)
  - [ ] Clear data option (with confirmation)
- [ ] Create settings preference items
  - [ ] Switch preference
  - [ ] Single choice preference
  - [ ] Action preference

---

## Phase 7: Additional Features

### 7.1 Permissions Handling
- [ ] Create `presentation/util/PermissionHandler.kt`
- [ ] Create `presentation/components/PermissionDialog.kt`
  - [ ] Explanation for why permission is needed
  - [ ] Button to open app settings
- [ ] Implement permission requests in screens
  - [ ] Contacts permission for main screen
  - [ ] Camera permission for photo picker
  - [ ] Storage permission for import/export

### 7.2 Import/Export Features
- [ ] Create `presentation/screens/importexport/ImportExportScreen.kt`
  - [ ] Export all contacts button
  - [ ] Export selected contacts
  - [ ] Import from file button
  - [ ] File picker integration
  - [ ] Progress indicator during import/export
  - [ ] Success/error messages
- [ ] Implement file picker for import
- [ ] Implement file save for export
- [ ] Handle large file imports with progress

### 7.3 Search & Filter
- [ ] Implement search in ContactListViewModel
  - [ ] Search by name
  - [ ] Search by phone number
  - [ ] Search by email
  - [ ] Debounce search input
- [ ] Create filter options
  - [ ] Filter by group
  - [ ] Filter by favorite
  - [ ] Filter by has phone/email
- [ ] Create `presentation/components/FilterBottomSheet.kt`

### 7.4 Sorting
- [ ] Implement sort options in ViewModel
  - [ ] Sort by first name
  - [ ] Sort by last name
  - [ ] Sort by recently added
  - [ ] Sort by recently updated
- [ ] Save sort preference in DataStore
- [ ] Create sort selection UI

### 7.5 Batch Operations
- [ ] Implement multi-select mode in ContactListScreen
  - [ ] Selection mode toggle
  - [ ] Select all / deselect all
  - [ ] Selection counter in TopAppBar
- [ ] Implement batch delete
- [ ] Implement batch add to group
- [ ] Implement batch export

### 7.6 Contact Actions
- [ ] Implement phone dialer intent
  - [ ] Check if dialer app exists
  - [ ] Handle permission
- [ ] Implement SMS intent
  - [ ] Check if SMS app exists
- [ ] Implement email intent
  - [ ] Check if email app exists
- [ ] Implement map intent for addresses
  - [ ] Format address for maps
- [ ] Implement share contact
  - [ ] Share as VCF file
  - [ ] Share via system share sheet

### 7.7 Backup & Restore
- [ ] Create automatic backup to Documents folder
  - [ ] Schedule periodic backups
  - [ ] Store in VCF format
- [ ] Create manual restore option
  - [ ] Browse backup files
  - [ ] Preview before restore
  - [ ] Option to merge or replace

---

## Phase 8: Testing

### 8.1 Unit Tests - Data Layer
- [ ] Create `ContactDaoTest.kt`
  - [ ] Test all CRUD operations
  - [ ] Test relationships
  - [ ] Test queries with search
  - [ ] Use in-memory database
- [ ] Create `PhoneNumberDaoTest.kt`
- [ ] Create `EmailDaoTest.kt`
- [ ] Create `AddressDaoTest.kt`
- [ ] Create `GroupDaoTest.kt`
- [ ] Create `ContactRepositoryImplTest.kt`
  - [ ] Mock DAOs
  - [ ] Test all repository methods
  - [ ] Test error handling

### 8.2 Unit Tests - Domain Layer
- [ ] Create tests for each use case
  - [ ] Mock repositories
  - [ ] Test business logic
  - [ ] Test validation
  - [ ] Test error scenarios
- [ ] Create `ValidateContactUseCaseTest.kt`
- [ ] Create `SaveContactUseCaseTest.kt`
- [ ] Create `ExportContactsUseCaseTest.kt`
- [ ] Create `ImportContactsUseCaseTest.kt`

### 8.3 Unit Tests - Presentation Layer
- [ ] Create `ContactListViewModelTest.kt`
  - [ ] Mock use cases
  - [ ] Test state updates
  - [ ] Test search functionality
  - [ ] Test delete operations
  - [ ] Use Turbine for Flow testing
- [ ] Create `ContactDetailViewModelTest.kt`
- [ ] Create `EditContactViewModelTest.kt`
  - [ ] Test validation logic
  - [ ] Test save operations
- [ ] Create `GroupListViewModelTest.kt`
- [ ] Create `SettingsViewModelTest.kt`

### 8.4 UI Tests - Compose
- [ ] Create `ContactListScreenTest.kt`
  - [ ] Test contact list rendering
  - [ ] Test search functionality
  - [ ] Test navigation to detail
  - [ ] Test FAB click
  - [ ] Test empty state
- [ ] Create `ContactDetailScreenTest.kt`
  - [ ] Test contact info display
  - [ ] Test action buttons
  - [ ] Test navigation
- [ ] Create `EditContactScreenTest.kt`
  - [ ] Test form input
  - [ ] Test add/remove fields
  - [ ] Test save button
  - [ ] Test validation errors
- [ ] Create navigation tests
  - [ ] Test all navigation flows
  - [ ] Test back navigation
  - [ ] Test deep links

### 8.5 Integration Tests
- [ ] Create end-to-end tests
  - [ ] Create contact flow
  - [ ] Edit contact flow
  - [ ] Delete contact flow
  - [ ] Search flow
  - [ ] Group management flow

### 8.6 Instrumented Tests
- [ ] Test database migrations
- [ ] Test file operations
- [ ] Test permissions handling
- [ ] Test camera/gallery integration

---

## Phase 9: Polish & Optimization

### 9.1 Performance Optimization
- [ ] Profile app with Android Profiler
  - [ ] Check for memory leaks
  - [ ] Check CPU usage
  - [ ] Check frame drops
- [ ] Optimize database queries
  - [ ] Add indices where needed
  - [ ] Optimize complex queries
- [ ] Optimize recompositions
  - [ ] Use remember and derivedStateOf appropriately
  - [ ] Use keys in LazyColumn
  - [ ] Profile with Layout Inspector
- [ ] Optimize image loading
  - [ ] Use Coil caching effectively
  - [ ] Compress large images
- [ ] Implement pagination for large contact lists
  - [ ] Use Paging 3 library

### 9.2 Accessibility
- [ ] Add content descriptions to all icons
- [ ] Test with TalkBack
- [ ] Ensure proper touch target sizes (48dp minimum)
- [ ] Support dynamic text scaling
- [ ] Add semantic properties
- [ ] Test color contrast ratios
- [ ] Support screen readers for all flows

### 9.3 Animations & Transitions
- [ ] Add screen transition animations
- [ ] Add FAB rotation animation
- [ ] Add item animations in LazyColumn
- [ ] Add swipe gesture animations
- [ ] Add loading animations
- [ ] Add success/error animations
- [ ] Use shared element transitions

### 9.4 Error Handling
- [ ] Implement global error handling
- [ ] Add user-friendly error messages
- [ ] Handle network errors (if sync feature added)
- [ ] Handle database errors
- [ ] Handle file I/O errors
- [ ] Add crash reporting (Firebase Crashlytics optional)
- [ ] Add logging for debugging

### 9.5 Edge Cases
- [ ] Handle empty database state
- [ ] Handle very long names/fields
- [ ] Handle special characters in names
- [ ] Handle contacts with no name
- [ ] Handle duplicate contacts
- [ ] Handle invalid phone/email formats
- [ ] Handle device rotation
- [ ] Handle process death and restoration
- [ ] Handle low storage scenarios
- [ ] Handle large contact lists (1000+ contacts)

### 9.6 UI/UX Polish
- [ ] Add haptic feedback for actions
- [ ] Add sound effects (optional)
- [ ] Polish all spacing and padding
- [ ] Ensure consistent design language
- [ ] Add subtle shadows and elevations
- [ ] Polish icon set
- [ ] Add app icon (adaptive icon)
- [ ] Add splash screen (Android 12+)
- [ ] Design empty states for all screens
- [ ] Add loading skeletons

---

## Phase 10: Documentation & Preparation

### 10.1 Code Documentation
- [ ] Add KDoc comments to all public classes
- [ ] Add KDoc to all public functions
- [ ] Document complex algorithms
- [ ] Add code examples where helpful
- [ ] Generate KDoc HTML documentation

### 10.2 Project Documentation
- [ ] Update README.md
  - [ ] Project description
  - [ ] Features list
  - [ ] Screenshots
  - [ ] Technologies used
  - [ ] Setup instructions
  - [ ] Architecture diagram
- [ ] Create CONTRIBUTING.md
- [ ] Create LICENSE file
- [ ] Create CHANGELOG.md
- [ ] Document architecture decisions (ADR)

### 10.3 Build Configuration
- [ ] Configure ProGuard/R8 for release
  - [ ] Test minified build
  - [ ] Verify no crashes in release build
- [ ] Configure signing config
  - [ ] Create keystore
  - [ ] Configure signing in build.gradle
- [ ] Setup build variants
  - [ ] Debug build
  - [ ] Release build
  - [ ] Different package names if needed
- [ ] Optimize APK size
  - [ ] Enable code shrinking
  - [ ] Enable resource shrinking
  - [ ] Use vector drawables
  - [ ] Remove unused resources

### 10.4 Quality Assurance
- [ ] Manual testing on different devices
  - [ ] Phone (different screen sizes)
  - [ ] Tablet
  - [ ] Foldable
- [ ] Test on different Android versions
  - [ ] Minimum SDK (API 24)
  - [ ] Latest SDK (API 34)
  - [ ] Popular versions in between
- [ ] Test dark mode thoroughly
- [ ] Test all user flows
- [ ] Test error scenarios
- [ ] Test offline scenarios
- [ ] Create test checklist
- [ ] Fix all critical bugs
- [ ] Fix all high-priority bugs

### 10.5 Localization (Optional)
- [ ] Extract all strings to strings.xml
- [ ] Add support for RTL languages
- [ ] Add translations for target languages
- [ ] Test with different locales
- [ ] Format dates/numbers according to locale

---

## Phase 11: Release Preparation

### 11.1 Pre-Release Checklist
- [ ] All tests passing
- [ ] No critical bugs
- [ ] App icon set
- [ ] Splash screen configured
- [ ] App name finalized
- [ ] Version code and version name set
- [ ] Release notes prepared
- [ ] Screenshots prepared (phone + tablet)
- [ ] Feature graphic prepared
- [ ] Privacy policy prepared (if needed)

### 11.2 Google Play Store Preparation (if publishing)
- [ ] Create Google Play Console account
- [ ] Create app listing
  - [ ] App title
  - [ ] Short description
  - [ ] Full description
  - [ ] Screenshots (minimum 2)
  - [ ] Feature graphic
  - [ ] App icon
  - [ ] Category selection
  - [ ] Content rating questionnaire
  - [ ] Privacy policy URL
- [ ] Configure pricing & distribution
- [ ] Setup closed testing track
- [ ] Upload release AAB
- [ ] Submit for review

### 11.3 Alternative Distribution (F-Droid, etc.)
- [ ] Remove proprietary dependencies
- [ ] Ensure fully open-source
- [ ] Add F-Droid metadata
- [ ] Submit to F-Droid repository

### 11.4 Post-Release
- [ ] Monitor crash reports
- [ ] Monitor user reviews
- [ ] Respond to user feedback
- [ ] Plan future updates
- [ ] Track analytics (if implemented)
- [ ] Create bug fix releases as needed

---

## Optional Features (Future Enhancements)

### Advanced Features
- [ ] Contact merge functionality
  - [ ] Detect duplicates
  - [ ] UI to merge contacts
- [ ] Contact sync with Google Contacts
  - [ ] Implement sync adapter
  - [ ] Handle conflict resolution
- [ ] Call log integration
  - [ ] Show recent calls in contact detail
- [ ] SMS history integration
  - [ ] Show recent messages in contact detail
- [ ] Birthday reminders
  - [ ] Notification system
  - [ ] Widget for upcoming birthdays
- [ ] Contact widgets
  - [ ] Favorites widget
  - [ ] Quick dial widget
- [ ] QR code for contacts
  - [ ] Generate QR from contact
  - [ ] Scan QR to add contact
- [ ] NFC contact sharing
  - [ ] Android Beam support
- [ ] Dark mode scheduling
  - [ ] Time-based or location-based
- [ ] Contact blocking
  - [ ] Mark contacts as blocked
  - [ ] Integration with system call blocking
- [ ] Custom fields support
  - [ ] Allow users to add custom fields
- [ ] Contact tags (in addition to groups)
  - [ ] Multiple tags per contact
  - [ ] Color-coded tags
- [ ] Advanced search filters
  - [ ] Search by custom fields
  - [ ] Boolean operators in search
- [ ] Contact notes with rich text
  - [ ] Markdown support
  - [ ] Attachment support
- [ ] Contact analytics
  - [ ] Most contacted
  - [ ] Contact frequency
  - [ ] Growth over time

---

## Notes & Best Practices

### Development Guidelines
- Follow SOLID principles
- Use meaningful variable/function names
- Keep functions small and focused
- Write self-documenting code
- Use Kotlin idioms (extension functions, data classes, etc.)
- Prefer immutability
- Use coroutines for async operations
- Collect flows in lifecycle-aware manner
- Handle configuration changes properly
- Test edge cases thoroughly

### Git Workflow
- Commit frequently with meaningful messages
- Use feature branches
- Write descriptive commit messages
- Tag releases
- Keep main/master branch stable

### Code Review Checklist
- Code follows project conventions
- No hardcoded strings
- No memory leaks
- Proper error handling
- Tests included
- Documentation updated
- Performance considered
- Accessibility considered

---

## Project Timeline Estimate

- **Phase 1-2:** 1 week (Setup & Structure)
- **Phase 3:** 2 weeks (Data Layer)
- **Phase 4:** 1 week (Domain Layer)
- **Phase 5:** 1 week (Theme & Components)
- **Phase 6:** 2-3 weeks (Screens & Navigation)
- **Phase 7:** 1-2 weeks (Additional Features)
- **Phase 8:** 1-2 weeks (Testing)
- **Phase 9:** 1 week (Polish)
- **Phase 10-11:** 1 week (Documentation & Release)

**Total Estimated Time:** 10-13 weeks for full development

---

## Resources & References

### Official Documentation
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Material 3 Design](https://m3.material.io/)
- [Android Architecture Components](https://developer.android.com/topic/architecture)
- [Room Database](https://developer.android.com/training/data-storage/room)
- [Hilt Dependency Injection](https://developer.android.com/training/dependency-injection/hilt-android)

### Sample Projects
- [Now in Android](https://github.com/android/nowinandroid)
- [Compose Samples](https://github.com/android/compose-samples)

### Fossify Contacts Reference
- [GitHub Repository](https://github.com/FossifyOrg/Contacts)

---

**Last Updated:** 2025-11-11
**Version:** 1.0
