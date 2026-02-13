# Guide de test pour l'abonnement Premium

## Problème : L'abonnement n'apparaît pas dans certains pays

### Étapes de diagnostic :

#### 1. Vérifier Google Play Console (Côté serveur)

**Accès :**
```
Google Play Console → Monétiser → Abonnements → premium_annual
```

**Checklist :**
- [ ] Statut = "Actif" (pas "Brouillon")
- [ ] Section "Prix et disponibilité" :
  - [ ] Tous les pays souhaités sont sélectionnés
  - [ ] Un prix est défini pour chaque pays
  - [ ] Aucun message d'erreur ou d'avertissement
- [ ] L'abonnement est associé à l'application correcte
- [ ] Le Product ID correspond : `premium_annual`

#### 2. Vider le cache Google Play (Côté client)

**Sur l'appareil Android :**
```
Paramètres → Applications → Google Play Store
→ Stockage → Vider le cache
→ Redémarrer l'appareil
```

Ou via ADB :
```bash
adb shell pm clear com.android.vending
```

#### 3. Tester avec un compte de test

**Créer un compte testeur :**
```
Google Play Console → Configuration → Testeurs avec licence
→ Ajouter des adresses e-mail de test
```

**Avantages :**
- Pas de vrais paiements
- Tests illimités
- Voit tous les produits actifs

#### 4. Vérifier les logs de l'application

**Activer le mode debug dans votre app :**
```kotlin
// Dans PremiumViewModel.kt, ajoutez des logs
private suspend fun loadProducts() {
    val result = getAvailableProductsUseCase()
    Log.d("PremiumDebug", "Products loaded: ${result.getOrNull()}")
    // ...
}
```

**Via Logcat :**
```bash
adb logcat | grep -i "billing\|premium\|subscription"
```

#### 5. Tester le code de facturation

**Vérifiez que BillingClient récupère les produits :**

```kotlin
// Dans BillingManager, ajoutez des logs pour voir quels produits sont retournés
val productList = queryProductDetailsParams.build()
Log.d("BillingDebug", "Querying products: $productList")
```

### Délais de propagation Google Play

| Action | Délai |
|--------|-------|
| Création d'un nouvel abonnement | 24-48h |
| Modification du prix | 2-4h |
| Ajout de nouveaux pays | 12-24h |
| Changement de statut (Brouillon → Actif) | 1-2h |

### Problèmes courants

#### ❌ Pays non disponibles
**Symptôme :** Certains pays ne voient pas l'abonnement
**Causes :**
- Compte marchand non configuré pour ces pays
- Restrictions légales/fiscales
- Prix non défini pour la devise locale

**Solution :**
```
Play Console → Paramètres du compte → Compte marchand
→ Vérifier les pays activés pour les paiements
```

#### ❌ Produit non trouvé (ITEM_UNAVAILABLE)
**Symptôme :** Erreur "Product not found" dans l'app
**Causes :**
- Product ID incorrect dans le code
- Abonnement en brouillon
- Abonnement non publié

**Solution :**
- Vérifier que `premium_annual` est exactement le même dans :
  - `Premium.kt:19`
  - Google Play Console
- Publier l'abonnement (statut = Actif)

#### ❌ Billing API retourne une liste vide
**Symptôme :** Aucun produit n'est chargé
**Causes :**
- Application signée avec mauvaise clé
- Version de l'app non uploadée sur Play Console
- Délai de propagation

**Solution :**
```bash
# Vérifier la signature de l'APK
keytool -printcert -jarfile app-release.apk

# Comparer avec la clé dans Play Console
Play Console → Configuration → Intégrité de l'application
```

### Tester avec différents pays

#### Option 1 : VPN (Non recommandé)
⚠️ Ne fonctionne pas toujours car Google Play détecte le pays selon :
- La carte SIM
- Le compte Google Play
- L'historique de localisation

#### Option 2 : Compte de test multilingue
✅ Créez des comptes Gmail dans différents pays :
```
Compte test US : test.us@gmail.com
Compte test FR : test.fr@gmail.com
Compte test BR : test.br@gmail.com
```

#### Option 3 : Testeurs internes internationaux
✅ Ajoutez des testeurs dans différents pays :
```
Play Console → Tests → Test interne
→ Inviter des testeurs dans différentes régions
```

### Commandes ADB utiles

```bash
# Voir les achats In-App disponibles
adb shell dumpsys activity com.contacts.android.contacts | grep -i billing

# Forcer la mise à jour du cache Google Play
adb shell pm clear com.android.vending
adb shell am start -a android.intent.action.VIEW -d "market://details?id=com.contacts.android.contacts"

# Installer l'APK signé pour tester
adb install -r app-release.apk
```

### Mode test vs Mode production

**Mode Test (BuildConfig.USE_PREMIUM_TEST_MODE = true) :**
- ✅ Produits mockés localement
- ✅ Pas besoin de Google Play Console
- ✅ Tests illimités gratuits
- ❌ Ne teste pas la vraie intégration Google Play

**Mode Production (BuildConfig.USE_PREMIUM_TEST_MODE = false) :**
- ✅ Teste la vraie intégration Google Play
- ✅ Vérifie les prix réels
- ❌ Nécessite configuration complète Google Play Console
- ⚠️ Utiliser des comptes de test pour éviter les vrais paiements

### Vérification finale

**Avant de publier en production :**

- [ ] L'abonnement est "Actif" dans Play Console
- [ ] Tous les pays cibles ont un prix défini
- [ ] Tests effectués avec plusieurs comptes de test
- [ ] Tests effectués dans plusieurs pays/devises
- [ ] L'application est signée avec la clé de production
- [ ] La version de l'app est uploadée sur Play Console (au moins en test interne)
- [ ] Le compte marchand est activé et vérifié
- [ ] Les taxes sont configurées pour tous les pays

### Ressources

- [Documentation Google Play Billing](https://developer.android.com/google/play/billing)
- [Tester les achats](https://developer.android.com/google/play/billing/test)
- [Gérer les abonnements](https://support.google.com/googleplay/android-developer/answer/140504)
