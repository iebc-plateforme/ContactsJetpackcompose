# 📱 Guide Détaillé : Configuration Premium sur Google Play Console

## Informations de votre application
- **Package Name**: `com.contacts.android.contacts`
- **Version actuelle**: 1.5.9 (versionCode: 150)
- **Produits à configurer**:
  1. `premium_lifetime` - Achat unique (Type: INAPP)
  2. `premium_annual` - Abonnement annuel (Type: SUBS)

---

# 🛒 PARTIE 1 : CRÉER LE PRODUIT IN-APP "premium_lifetime"

## Étape 1 : Accéder à la section Produits In-App

1. **Connectez-vous à Play Console** : https://play.google.com/console
2. **Sélectionnez votre application** : `com.contacts.android.contacts`
3. Dans le **menu latéral gauche**, faites défiler jusqu'à **"Monétisation"**
4. Cliquez sur **"Produits"** → **"Produits In-App"**

> **Note** : Si vous ne voyez pas cette option, assurez-vous que :
> - Votre application a été créée sur Play Console
> - Vous avez les permissions nécessaires (compte propriétaire ou admin)
> - Vous avez uploadé au moins une version de l'app (même en brouillon)

---

## Étape 2 : Créer un nouveau produit

1. En haut à droite, cliquez sur le bouton **"Créer un produit"** (ou "Create product")
2. Une fenêtre modale s'ouvre avec le formulaire de création

---

## Étape 3 : Remplir les informations de base

### 3.1 - ID du produit (Product ID)
```
premium_lifetime
```

**⚠️ ATTENTION CRITIQUE** :
- L'ID doit être **EXACTEMENT** `premium_lifetime` (tout en minuscules, avec underscore)
- Cet ID est **PERMANENT** et ne peut **JAMAIS** être modifié
- Il doit correspondre exactement à celui dans votre code : `Premium.kt:19`
- Ne mettez pas d'espaces, de tirets, ou de caractères spéciaux autres que underscore

### 3.2 - Nom (Name)
**Anglais** (par défaut) :
```
Premium Lifetime
```

**💡 Conseil** : Vous pouvez ajouter des traductions plus tard. Pour l'instant, l'anglais suffit.

### 3.3 - Description (Description)
**Anglais** (par défaut) :
```
Unlock all premium features forever with a one-time purchase. No ads, exclusive themes, and lifetime access to all future premium features.
```

**Français** (optionnel, à ajouter après création) :
```
Débloquez toutes les fonctionnalités premium à vie avec un achat unique. Sans publicités, thèmes exclusifs et accès à vie à toutes les futures fonctionnalités premium.
```

---

## Étape 4 : Configurer le prix par défaut

### 4.1 - Prix de base (Base plan)

1. Dans la section **"Prix"** ou **"Pricing"**
2. Cliquez sur **"Ajouter un prix"** ou **"Add a price"**

### 4.2 - Choisir la stratégie de prix

Vous avez 2 options :

#### Option A : Prix manuel par pays (Recommandé pour contrôle total)
1. Sélectionnez **"Définir les prix individuellement"**
2. Une liste de tous les pays apparaît
3. Remplissez manuellement les prix pour chaque pays important

#### Option B : Prix automatique (Recommandé pour démarrer rapidement)
1. Sélectionnez **"Définir les prix pour tous les pays"**
2. Choisissez une devise de base (ex: USD)
3. Entrez le prix de base : **9.99** USD
4. Play Console calculera automatiquement les prix équivalents

### 4.3 - Prix recommandés par région

Voici une liste complète des prix suggérés pour `premium_lifetime` :

| Pays / Région | Code devise | Prix recommandé | Notes |
|---------------|-------------|-----------------|-------|
| **🇺🇸 États-Unis** | USD | $9.99 | Prix de référence |
| **🇪🇺 Zone Euro** | EUR | 9,99 € | |
| **🇬🇧 Royaume-Uni** | GBP | £8.99 | |
| **🇨🇦 Canada** | CAD | $12.99 | |
| **🇦🇺 Australie** | AUD | $14.99 | |
| **🇯🇵 Japon** | JPY | ¥1,200 | |
| **🇰🇷 Corée du Sud** | KRW | ₩12,000 | |
| **🇮🇳 Inde** | INR | ₹799 | Marché sensible au prix |
| **🇧🇷 Brésil** | BRL | R$ 49,90 | |
| **🇲🇽 Mexique** | MXN | $199 | |
| **🇷🇺 Russie** | RUB | 899 ₽ | |
| **🇨🇳 Chine** | CNY | ¥68 | |
| **🇹🇷 Turquie** | TRY | 299 ₺ | Marché sensible au prix |
| **🇦🇷 Argentine** | ARS | $7,999 | Inflation élevée |
| **🇿🇦 Afrique du Sud** | ZAR | R179 | |
| **🇸🇬 Singapour** | SGD | $13.99 | |
| **🇨🇭 Suisse** | CHF | 9.90 | |
| **🇸🇪 Suède** | SEK | 109 kr | |
| **🇳🇴 Norvège** | NOK | 109 kr | |
| **🇩🇰 Danemark** | DKK | 74 kr | |
| **🇵🇱 Pologne** | PLN | 44,99 zł | |

**💡 Astuce** : Si vous utilisez les prix automatiques avec 9.99 USD comme base, Play Console appliquera les conversions suivantes :
- Conversion au taux de change actuel
- Ajustement selon la parité de pouvoir d'achat
- Arrondissement aux valeurs "psychologiques" (9.99, 4.99, etc.)

---

## Étape 5 : Paramètres avancés (Optionnels)

### 5.1 - Période d'essai gratuit
❌ **Non disponible pour les produits In-App** (uniquement pour les abonnements)

### 5.2 - Taxes
- Play Console gère automatiquement les taxes selon les réglementations locales
- Vous n'avez rien à configurer ici

### 5.3 - Statut
- **Laissez sur "Inactif"** pour l'instant
- Nous l'activerons plus tard après vérification

---

## Étape 6 : Enregistrer le produit

1. Vérifiez tous les champs
2. Cliquez sur **"Enregistrer"** en bas à droite
3. Le produit `premium_lifetime` est maintenant créé mais **inactif**

---

# 📅 PARTIE 2 : CRÉER L'ABONNEMENT "premium_annual"

## Étape 7 : Accéder à la section Abonnements

1. Dans le **menu latéral gauche**, sous **"Monétisation"**
2. Cliquez sur **"Abonnements"** (et non "Produits In-App")
3. Cliquez sur **"Créer un abonnement"**

---

## Étape 8 : Configurer l'abonnement de base

### 8.1 - ID de l'abonnement (Subscription ID)
```
premium_annual
```

**⚠️ ATTENTION CRITIQUE** :
- L'ID doit être **EXACTEMENT** `premium_annual`
- Cet ID est **PERMANENT** et ne peut **JAMAIS** être modifié
- Il doit correspondre à votre code : `Premium.kt:20`

### 8.2 - Nom de l'abonnement
**Anglais** :
```
Premium Annual Subscription
```

**Français** (à ajouter après) :
```
Abonnement Premium Annuel
```

### 8.3 - Description
**Anglais** :
```
Unlock all premium features with an annual subscription. Cancel anytime. Includes ad-free experience, exclusive themes, and priority support.
```

**Français** :
```
Débloquez toutes les fonctionnalités premium avec un abonnement annuel. Annulez à tout moment. Inclut une expérience sans publicité, des thèmes exclusifs et un support prioritaire.
```

---

## Étape 9 : Créer le plan de base (Base Plan)

### 9.1 - Créer un plan
1. Dans la section **"Plans de base"** ou **"Base plans"**
2. Cliquez sur **"Ajouter un plan de base"**

### 9.2 - ID du plan de base
Play Console génère automatiquement un ID. Vous pouvez le laisser tel quel ou le personnaliser :
```
annual-base-plan
```
ou simplement
```
base
```

### 9.3 - Période de facturation
- **Sélectionnez** : **"1 an"** ou **"Annual"** ou **"12 mois"**
- C'est la période de renouvellement automatique

### 9.4 - Mode de renouvellement
- **Sélectionnez** : **"Renouvellement automatique"**
- L'utilisateur sera facturé automatiquement chaque année jusqu'à annulation

---

## Étape 10 : Configurer les prix de l'abonnement

### 10.1 - Prix de base recommandés

| Pays / Région | Code devise | Prix annuel recommandé | Prix mensuel équivalent | Économie vs mensuel |
|---------------|-------------|------------------------|-------------------------|---------------------|
| **🇺🇸 États-Unis** | USD | $19.99/an | $1.67/mois | ~60% |
| **🇪🇺 Zone Euro** | EUR | 19,99 €/an | 1,67 €/mois | ~60% |
| **🇬🇧 Royaume-Uni** | GBP | £17.99/an | £1.50/mois | ~60% |
| **🇨🇦 Canada** | CAD | $24.99/an | $2.08/mois | ~60% |
| **🇦🇺 Australie** | AUD | $29.99/an | $2.50/mois | ~60% |
| **🇯🇵 Japon** | JPY | ¥2,400/an | ¥200/mois | ~60% |
| **🇰🇷 Corée du Sud** | KRW | ₩24,000/an | ₩2,000/mois | ~60% |
| **🇮🇳 Inde** | INR | ₹1,499/an | ₹125/mois | ~60% |
| **🇧🇷 Brésil** | BRL | R$ 99,90/an | R$ 8,33/mois | ~60% |
| **🇲🇽 Mexique** | MXN | $399/an | $33/mois | ~60% |
| **🇷🇺 Russie** | RUB | 1,799 ₽/an | 150 ₽/mois | ~60% |
| **🇨🇳 Chine** | CNY | ¥138/an | ¥11.50/mois | ~60% |
| **🇹🇷 Turquie** | TRY | 599 ₺/an | 50 ₺/mois | ~60% |
| **🇦🇷 Argentine** | ARS | $15,999/an | $1,333/mois | ~60% |
| **🇿🇦 Afrique du Sud** | ZAR | R349/an | R29/mois | ~60% |

### 10.2 - Méthode de configuration des prix

**Option A : Prix automatique (Recommandé)**
1. Sélectionnez **"Utiliser les prix automatiques"**
2. Choisissez USD comme devise de base
3. Entrez **19.99** USD
4. Play Console calculera les autres devises

**Option B : Prix manuel**
1. Sélectionnez **"Définir les prix individuellement"**
2. Remplissez manuellement chaque pays selon le tableau ci-dessus

---

## Étape 11 : Période d'essai gratuit (Optionnel mais RECOMMANDÉ)

### 11.1 - Pourquoi offrir un essai gratuit ?
- ✅ Augmente le taux de conversion de 40-60%
- ✅ Permet aux utilisateurs de tester sans risque
- ✅ Les utilisateurs sont plus susceptibles de continuer après l'essai

### 11.2 - Configuration de l'essai

1. Dans la section **"Offres"** ou **"Offers"**
2. Cliquez sur **"Ajouter une offre"**
3. Sélectionnez **"Essai gratuit"** ou **"Free trial"**

### 11.3 - Durée de l'essai recommandée
- **7 jours** : Bon équilibre (recommandé)
- **14 jours** : Plus généreux, meilleure conversion
- **3 jours** : Trop court, peu de conversion

**Configuration suggérée** :
```
Type d'offre : Essai gratuit
Durée : 7 jours
Éligibilité : Nouveaux abonnés uniquement
Après l'essai : Facturation automatique de 19.99 USD
```

### 11.4 - Phase d'essai dans le code

⚠️ **Important** : L'essai gratuit est géré automatiquement par Google Play Billing. Votre code actuel dans `BillingManager.kt` le gère déjà correctement :
- Ligne 148 : `subscriptionOfferDetails` récupère automatiquement l'offre d'essai
- Ligne 216-219 : `offerToken` inclut l'essai gratuit si disponible

Aucune modification de code n'est nécessaire !

---

## Étape 12 : Offre de lancement (Optionnel)

### 12.1 - Promotion de lancement

Vous pouvez offrir une réduction pour les premiers abonnés :

**Exemple** :
```
Type : Prix réduit
Durée : 1er mois à 50% de réduction
Prix : $9.99 pour le 1er mois, puis $19.99/an
Éligibilité : Nouveaux abonnés uniquement
Limite de temps : 3 premiers mois après le lancement
```

### 12.2 - Configuration
1. Cliquez sur **"Ajouter une offre"**
2. Sélectionnez **"Prix réduit"**
3. Configurez selon vos besoins

---

## Étape 13 : Paramètres de renouvellement

### 13.1 - Période de grâce
**Recommandation** : **3 jours**

Quand l'activer :
- ✅ Le paiement échoue (carte expirée, fonds insuffisants)
- ✅ L'utilisateur garde l'accès premium pendant 3 jours
- ✅ Google Play tente de facturer à nouveau

Configuration :
```
Activer la période de grâce : OUI
Durée : 3 jours
```

### 13.2 - Nouvelle tentative de facturation
Google Play essaiera automatiquement de facturer :
- Jour 1 : Immédiatement après l'échec
- Jour 3 : Deuxième tentative
- Jour 7 : Troisième tentative
- Après 7 jours : Annulation de l'abonnement

### 13.3 - Rétablissement de l'abonnement
**Recommandation** : **60 jours**

Configuration :
```
Activer le rétablissement : OUI
Période de rétablissement : 60 jours
```

Cela permet à l'utilisateur de réabonner sans perdre son historique.

---

## Étape 14 : Enregistrer l'abonnement

1. Vérifiez toutes les informations
2. Cliquez sur **"Enregistrer"**
3. L'abonnement `premium_annual` est créé mais **inactif**

---

# ✅ PARTIE 3 : ACTIVER LES PRODUITS

## Étape 15 : Conditions préalables à l'activation

Avant de pouvoir activer les produits, vous devez :

### 15.1 - ✅ Avoir une version de l'app publiée
- Au minimum en **test interne** (Internal Testing)
- L'app doit être signée avec votre keystore release
- Le package name doit correspondre : `com.contacts.android.contacts`

**Statut actuel de votre app** : À vérifier sur Play Console

### 15.2 - ✅ Avoir configuré un compte marchand
- Allez dans **Paramètres** → **Compte marchand**
- Liez votre compte bancaire pour recevoir les paiements
- Configurez les informations fiscales

### 15.3 - ✅ Avoir accepté les conditions du programme de paiement
- Play Console vous demandera d'accepter les conditions
- C'est automatique lors de la première activation de produits

---

## Étape 16 : Activer "premium_lifetime"

1. Allez dans **Monétisation** → **Produits** → **Produits In-App**
2. Trouvez le produit **"premium_lifetime"**
3. Cliquez sur le produit pour ouvrir les détails
4. En haut, vous verrez le **Statut actuel** : **"Inactif"**
5. Cliquez sur le bouton **"Activer"** ou changez le statut en **"Actif"**
6. Une confirmation apparaît : **"Êtes-vous sûr de vouloir activer ce produit ?"**
7. Cliquez sur **"Activer"**

✅ Le produit est maintenant **ACTIF** et disponible pour les achats !

---

## Étape 17 : Activer "premium_annual"

1. Allez dans **Monétisation** → **Abonnements**
2. Trouvez l'abonnement **"premium_annual"**
3. Cliquez sur l'abonnement pour ouvrir les détails
4. Changez le statut de **"Inactif"** à **"Actif"**
5. Confirmez l'activation

✅ L'abonnement est maintenant **ACTIF** !

---

# 🧪 PARTIE 4 : CONFIGURATION DES TESTS

## Étape 18 : Ajouter des testeurs avec licence

### 18.1 - Pourquoi des testeurs avec licence ?
- Ils peuvent effectuer des achats **sans être facturés**
- Les achats sont traités comme réels mais annulés automatiquement
- Permet de tester tout le flux sans dépenser d'argent

### 18.2 - Ajouter des testeurs

1. Allez dans **Paramètres** → **Gestion des licences** (ou **License Testing**)
2. Dans la section **"Testeurs avec licence"** ou **"License testers"**
3. Ajoutez des adresses Gmail (séparées par des virgules) :
```
votre.email@gmail.com, testeur1@gmail.com, testeur2@gmail.com
```
4. Cliquez sur **"Enregistrer"**

### 18.3 - Réponse de test pour les licences

**Recommandation** : Sélectionnez **"RÉPONDRE"** (ou **"RESPOND_NORMALLY"**)

Options disponibles :
- **RÉPONDRE** : Simule des achats réels (recommandé)
- **TOUJOURS REFUSER** : Tous les achats échouent (pour tester les erreurs)

---

## Étape 19 : Configurer les testeurs internes

1. Allez dans **Test** → **Tests internes** (Internal Testing)
2. Créez une nouvelle version de test ou utilisez une existante
3. Cliquez sur **"Testeurs"**
4. Créez une liste de testeurs ou ajoutez des emails individuels
5. Partagez le lien de test avec vos testeurs

**Le lien ressemble à** :
```
https://play.google.com/apps/internaltest/[CODE_UNIQUE]
```

---

# 📊 PARTIE 5 : VÉRIFICATIONS FINALES

## Étape 20 : Checklist finale

Vérifiez que tout est correctement configuré :

### ✅ Produit In-App "premium_lifetime"
- [ ] ID du produit : `premium_lifetime` (exact)
- [ ] Type : Produit géré (Managed product / INAPP)
- [ ] Prix configuré pour toutes les régions importantes
- [ ] Statut : **ACTIF**
- [ ] Nom et description en anglais (minimum)

### ✅ Abonnement "premium_annual"
- [ ] ID de l'abonnement : `premium_annual` (exact)
- [ ] Type : Abonnement (Subscription / SUBS)
- [ ] Période : 1 an (12 mois)
- [ ] Plan de base créé avec prix
- [ ] Prix configuré pour toutes les régions
- [ ] Essai gratuit : 7 jours (optionnel mais recommandé)
- [ ] Période de grâce : 3 jours
- [ ] Statut : **ACTIF**

### ✅ Configuration du compte
- [ ] Compte marchand configuré
- [ ] Informations bancaires ajoutées
- [ ] Testeurs avec licence ajoutés
- [ ] Version de test publiée (interne minimum)

### ✅ Correspondance avec le code
- [ ] Les IDs dans Play Console correspondent exactement au code
- [ ] `premium_lifetime` = `Premium.kt:19`
- [ ] `premium_annual` = `Premium.kt:20`

---

# 🧪 PARTIE 6 : TESTER LES ACHATS

## Étape 21 : Tester "premium_lifetime"

1. **Installez l'app** depuis le lien de test interne
2. **Connectez-vous** avec un compte testeur
3. **Naviguez** vers l'écran Premium
4. **Cliquez** sur "Premium Lifetime"
5. **Vérifiez** que le prix s'affiche correctement
6. **Procédez** à l'achat
7. **Un message apparaît** : "Ceci est un achat test, vous ne serez pas facturé"
8. **Confirmez** l'achat
9. **Vérifiez** que :
   - [ ] Les publicités disparaissent
   - [ ] Les thèmes exclusifs sont débloqués
   - [ ] Le statut premium est sauvegardé

## Étape 22 : Tester "premium_annual"

1. **Sur l'écran Premium**, cliquez sur "Premium Annual"
2. **Vérifiez** que l'essai gratuit s'affiche (si configuré)
3. **Procédez** à l'abonnement
4. **Message test** : "Ceci est un achat test"
5. **Confirmez**
6. **Vérifiez** que :
   - [ ] Le statut premium est actif
   - [ ] Les fonctionnalités premium fonctionnent
   - [ ] L'abonnement apparaît dans les paramètres Google Play

## Étape 23 : Tester la restauration des achats

1. **Désinstallez** l'application
2. **Réinstallez** l'application
3. **Sur l'écran Premium**, cliquez sur "Restaurer les achats"
4. **Vérifiez** que le statut premium est restauré automatiquement

---

# 🐛 PARTIE 7 : RÉSOLUTION DE PROBLÈMES

## Problème 1 : "Le produit n'existe pas"

**Causes possibles** :
- ❌ Le produit n'est pas activé sur Play Console
- ❌ L'ID dans le code ne correspond pas exactement
- ❌ L'app n'est pas publiée (même en test interne)
- ❌ Le package name ne correspond pas

**Solutions** :
1. Vérifiez que les produits sont **ACTIFS** sur Play Console
2. Comparez les IDs : `premium_lifetime` et `premium_annual`
3. Publiez au moins en test interne
4. Attendez 2-3 heures pour la propagation

## Problème 2 : "Erreur de facturation"

**Causes possibles** :
- ❌ Le compte marchand n'est pas configuré
- ❌ Les conditions de paiement ne sont pas acceptées
- ❌ Le compte testeur n'est pas ajouté

**Solutions** :
1. Configurez le compte marchand
2. Acceptez toutes les conditions sur Play Console
3. Ajoutez votre compte Gmail comme testeur avec licence

## Problème 3 : "Le prix ne s'affiche pas"

**Causes possibles** :
- ❌ Les prix ne sont pas configurés pour la région de l'utilisateur
- ❌ Le produit n'est pas encore propagé (jusqu'à 24h)

**Solutions** :
1. Configurez les prix pour toutes les régions
2. Attendez quelques heures
3. Utilisez les prix automatiques

## Problème 4 : L'essai gratuit n'apparaît pas

**Causes possibles** :
- ❌ L'utilisateur a déjà utilisé un essai gratuit
- ❌ L'offre n'est pas configurée correctement
- ❌ L'offre n'est pas active

**Solutions** :
1. Testez avec un nouveau compte
2. Vérifiez la configuration de l'offre d'essai
3. Assurez-vous que l'offre est active

---

# 📞 SUPPORT ET RESSOURCES

## Ressources officielles
- **Documentation Google Play Billing** : https://developer.android.com/google/play/billing
- **Support Play Console** : https://support.google.com/googleplay/android-developer
- **Centre d'aide Billing** : https://support.google.com/googleplay/android-developer/topic/9857897

## Code source de référence
- **BillingManager.kt** : `app/src/main/java/com/contacts/android/contacts/data/billing/BillingManager.kt`
- **Premium.kt** : `app/src/main/java/com/contacts/android/contacts/domain/model/Premium.kt`
- **PremiumRepositoryImpl.kt** : `app/src/main/java/com/contacts/android/contacts/data/repository/PremiumRepositoryImpl.kt`

---

# ✅ RÉCAPITULATIF : CE QU'IL FAUT FAIRE

1. ✅ Créer `premium_lifetime` (Produit In-App)
2. ✅ Créer `premium_annual` (Abonnement)
3. ✅ Configurer les prix pour toutes les régions
4. ✅ Configurer un essai gratuit de 7 jours (optionnel)
5. ✅ Activer les deux produits
6. ✅ Ajouter des testeurs avec licence
7. ✅ Publier une version de test (interne)
8. ✅ Tester les deux types d'achats
9. ✅ Vérifier que les fonctionnalités premium fonctionnent
10. ✅ Tester la restauration des achats

---

**Temps estimé pour la configuration complète** : 30-45 minutes

**Bon courage pour la configuration !**
